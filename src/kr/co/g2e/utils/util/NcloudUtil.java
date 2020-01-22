package kr.co.g2e.utils.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * 네이버 비지니스 플랫폼(Ncloud) 유틸리티 클래스
 */
public final class NcloudUtil {

	/**
	 * SMS API
	 */
	private static final String SMS_API_URL = "https://sens.apigw.ntruss.com/sms/v2";

	/**
	 * 라인 구분자
	 */
	private static final String NEW_LINE = "\n";

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private NcloudUtil() {
	}

	public static Map<String, Object> sendSms(String serviceId, String accessKey, String secretKey) {
		/* 요청 파라미터
		{
		    "type":"(SMS | LMS | MMS)",
		    "contentType":"(COMM | AD)",
		    "countryCode":"string",
		    "from":"string",
		    "subject":"string",
		    "content":"string",
		    "messages":[
		        {
		            "to":"string",
		            "subject":"string",
		            "content":"string"
		        }
		    ],
		    "files":[
		        {
		            "name":"string",
		            "body":"string"
		        }
		    ],
		    "reserveTime": "yyyy-MM-dd HH:mm",
		    "reserveTimeZone": "string",
		    "scheduleCode": "string"
		}
		 */
		// 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		String url = SMS_API_URL + "/services/" + serviceId + "/messages";
		long timestamp = new Date().getTime();
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("x-ncp-apigw-timestamp", String.valueOf(timestamp));
		headerMap.put("x-ncp-iam-access-key", accessKey);
		headerMap.put("x-ncp-apigw-signature-v2", makeSignature("POST", url, accessKey, secretKey, timestamp));
		// 파라미터 맵
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("cid", "");
	
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			kr.co.g2e.utils.util.HttpUtil.Result result = HttpUtil.post(url, paramMap, headerMap);
			/* 응답 객체 설명
			tid: 결제 고유 번호. 20자(String)
			next_redirect_app_url: 요청한 클라이언트가 모바일 앱일 경우 해당 url을 통해 카카오톡 결제페이지를 띄움(String)
			next_redirect_mobile_url: 요청한 클라이언트가 모바일 웹일 경우 해당 url을 통해 카카오톡 결제페이지를 띄움(String)
			next_redirect_pc_url: 요청한 클라이언트가 pc 웹일 경우 redirect. 카카오톡으로 TMS를 보내기 위한 사용자입력화면이으로 redirect(String)
			android_app_scheme: 카카오페이 결제화면으로 이동하는 안드로이드 앱스킴(String)
			ios_app_scheme: 카카오페이 결제화면으로 이동하는 iOS 앱스킴(String)
			created_at: 결제 준비 요청 시간(Datetime)
			 */
			if (result.getStatusCode() == 200) { // 성공
				String json = result.getContent();
				resultMap = (Map<String, Object>) JsonUtil.parse(json);
				resultMap.put("ok", Boolean.TRUE);
			} else { // 실패
				resultMap.put("ok", Boolean.FALSE);
				resultMap.put("code", result.getStatusCode());
				resultMap.put("msg", "Http 통신 실패");
			}
		} catch (Throwable e) {
			resultMap.put("ok", Boolean.FALSE);
			resultMap.put("code", 999);
			resultMap.put("msg", "Http 통신 실패");
		}
		return resultMap;
	}

	/**
	 * API 호출 signature 생성
	 * @param httpMethod http메소드 문자열(대문자 사용) - GET, POST
	 * @param url 호출하는 URL
	 * @param accessKey 액세스키
	 * @param secretKey 시크릿키
	 * @param timestamp 1970.1.1 이후 UTC기준 타임스탬프 
	 * @return Base64 인코딩된 signature 문자열
	 */
	private static String makeSignature(String httpMethod, String url, String accessKey, String secretKey, long timestamp) {
		try {
			StringBuilder message = new StringBuilder();
			message.append(httpMethod);
			message.append(" ");
			message.append(url);
			message.append(NEW_LINE);
			message.append(timestamp);
			message.append(NEW_LINE);
			message.append(accessKey);
			SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(message.toString().getBytes("UTF-8"));
			String encodeBase64String = Base64.encodeBase64String(rawHmac);
			return encodeBase64String;
		} catch (Throwable e) {
			return "";
		}
	}
}