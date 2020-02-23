package kr.co.g2e.utils.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import kr.co.g2e.utils.util.HttpUtil.Result;

/**
 * 네이버 비지니스 플랫폼(Ncloud) 유틸리티 클래스
 */
public final class NcloudUtil {

	/**
	 * SMS API
	 */
	private static final String SMS_API_DOMAIN = "https://sens.apigw.ntruss.com";
	private static final String SMS_API_URL = "/sms/v2";

	/**
	 * EMAIL API
	 */
	private static final String EMAIL_API_DOMAIN = "https://mail.apigw.ntruss.com";
	private static final String EMAIL_API_URL = "/api/v1/mails";

	/**
	 * STT API
	 */
	private static final String STT_API_DOMAIN = "https://naveropenapi.apigw.ntruss.com";
	private static final String STT_API_URL = "/recog/v1/stt";

	/**
	 * 라인 구분자
	 */
	private static final String NEW_LINE = "\n";

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private NcloudUtil() {
	}

	/**
	 * SMS 발송(90바이트 이하)
	 * @param accessKey 엑세스키
	 * @param secretKey 시크릿키
	 * @param serviceId 서비스아이디
	 * @param from 발신번호
	 * @param content 메시지 내용
	 * @param to 수신번호(-를 제외한 숫자만 입력 가능)
	 * @return 응답결과
	 */
	public static Map<String, Object> sendSms(String accessKey, String secretKey, String serviceId, String from, String content, String to) {
		return sendSmsLms(accessKey, secretKey, serviceId, "SMS", from, "", content, to);
	}

	/**
	 * LMS 발송(90바이트 초과)
	 * @param accessKey 엑세스키
	 * @param secretKey 시크릿키
	 * @param serviceId 서비스아이디
	 * @param from 발신번호
	 * @param subject 메시지 제목
	 * @param content 메시지 내용
	 * @param to 수신번호(-를 제외한 숫자만 입력 가능)
	 * @return 응답결과
	 */
	public static Map<String, Object> sendLms(String accessKey, String secretKey, String serviceId, String from, String subject, String content, String to) {
		return sendSmsLms(accessKey, secretKey, serviceId, "LMS", from, subject, content, to);
	}

	/**
	 * 이메일 발송(템플릿 사용)
	 * @param accessKey 엑세스키
	 * @param secretKey 시크릿키
	 * @param fromEmail 발신자 이메일주소
	 * @param fromName 발신자명
	 * @param templateSid 템플릿 아이디
	 * @param toEmail 수신자 이메일주소
	 * @param toName 수신자명
	 * @return
	 */
	public static Map<String, Object> sendEmail(String accessKey, String secretKey, String fromEmail, String fromName, Integer templateSid, String toEmail, String toName) {
		return sendEmail(accessKey, secretKey, fromEmail, fromName, templateSid, "", "", toEmail, toName, null);
	}

	/**
	 * 이메일 발송(템플릿, 파라미터 바인딩 사용)
	 * @param accessKey 엑세스키
	 * @param secretKey 시크릿키
	 * @param fromEmail 발신자 이메일주소
	 * @param fromName 발신자명
	 * @param templateSid 템플릿 아이디
	 * @param toEmail 수신자 이메일주소
	 * @param toName 수신자명
	 * @param parameters 치환 파라미터
	 * @return 응답결과
	 */
	public static Map<String, Object> sendEmail(String accessKey, String secretKey, String fromEmail, String fromName, Integer templateSid, String toEmail, String toName, Map<String, Object> parameters) {
		return sendEmail(accessKey, secretKey, fromEmail, fromName, templateSid, "", "", toEmail, toName, parameters);
	}

	/**
	 * 이메일 발송
	 * @param accessKey 엑세스키
	 * @param secretKey 시크릿키
	 * @param fromEmail 발신자 이메일주소
	 * @param fromName 발신자명
	 * @param subject 메일 제목
	 * @param content 메일 본분
	 * @param toEmail 수신자 이메일주소
	 * @param toName 수신자명
	 * @return 응답결과
	 */
	public static Map<String, Object> sendEmail(String accessKey, String secretKey, String fromEmail, String fromName, String subject, String content, String toEmail, String toName) {
		return sendEmail(accessKey, secretKey, fromEmail, fromName, null, subject, content, toEmail, toName, null);
	}

	/**
	 * 이메일 발송(파라미터 바인딩 사용)
	 * @param accessKey 엑세스키
	 * @param secretKey 시크릿키
	 * @param fromEmail 발신자 이메일주소
	 * @param fromName 발신자명
	 * @param subject 메일 제목
	 * @param content 메일 본분
	 * @param toEmail 수신자 이메일주소
	 * @param toName 수신자명
	 * @param parameters 치환 파라미터
	 * @return 응답결과
	 */
	public static Map<String, Object> sendEmail(String accessKey, String secretKey, String fromEmail, String fromName, String subject, String content, String toEmail, String toName, Map<String, Object> parameters) {
		return sendEmail(accessKey, secretKey, fromEmail, fromName, null, subject, content, toEmail, toName, parameters);
	}

	/**
	 * OCR 판독
	 * @param apigwUrl API 게이트웨이 주소
	 * @param secretKey 시크릿키
	 * @param imageFormat 판독할 이미지 주소
	 * @param imageUrl 이미지 URL
	 * @return 판독 결과 문자열
	 */
	@SuppressWarnings("unchecked")
	public static String ocr(String apigwUrl, String secretKey, String imageFormat, String imageUrl) {
		Map<String, String> headerMap = new HashMap<String, String>(); // 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		headerMap.put("Content-Type", "application/json; charset=utf-8");
		headerMap.put("X-OCR-SECRET", secretKey);
		/* 파라미터 설명
		[필수]version: 버전 정보 필수로 V1 입력합니다.(string)
		[필수]requestId: API 호출 UUID입니다.(string)
		[필수]timestamp: API 호출 Timestamp 값입니다.	
		<옵션>lang: OCR 인식시 요청할 언어 정보입니다.	lang 필드가 설정되지 않은 경우, ‘ko’가 default로 설정됩니다.(string)
		[필수]images: Json array로 입력되며, 현재는 1개의 이미지 입력만 허용 됩니다. (한국어/일본어)(json array)
		[필수]images.format: 이미지 포맷을 설정합니다.	“jpg”, “jpeg”, “png” 이미지 형식을 지원합니다.(string)
		<옵션>images.url: images.url 혹은 image.data 중 하나가 존재해야 합니다.	URL은 이미지를 가져 올 수 있는 공개 URL이어야 합니다.(string)
		<옵션>image.data: images.url 혹은 image.data 중 하나가 존재해야 합니다. URL과 데이터가 모두 존재하면 데이터를 사용하고 URL은 무시합니다. image.data는 base64 인코딩 이미지 바이트입니다.(string)
		[필수]image.name: 이미지 명을 입력합니다. 이미지를 식별하는데 사용되며, 응답 결과 확인에 사용됩니다.(string)
		<옵션>image.templateIds: 텍스트 OCR API에서는 사용되지 않습니다. Template OCR API에서는 이 필드를 설정하지 않으면 도메인에 배포된 모든 서비스 템플릿으로 자동 분류됩니다.(json array)
		 */
		long timestamp = new Date().getTime();
		Map<String, Object> paramMap = new HashMap<String, Object>(); // 파라미터 맵
		// images 맵 생성
		Map<String, String> imageMap = new HashMap<String, String>();
		imageMap.put("format", imageFormat);
		imageMap.put("name", "ocr_image");
		imageMap.put("url", imageUrl);
		paramMap.put("images", Arrays.asList(imageMap));
		// 파라미터 맵 생성
		paramMap.put("lang", "ko");
		paramMap.put("requestId", UUID.randomUUID().toString());
		paramMap.put("timestamp", timestamp);
		paramMap.put("version", "V1");
		// 결과 맵 생성
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.post(apigwUrl, JsonUtil.stringify(paramMap), headerMap);
			String json = result.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			List<Map<String, Object>> imagesList = (List<Map<String, Object>>) resultMap.get("images");
			if (imagesList != null && imagesList.size() > 0) {
				Map<String, Object> ocrMap = imagesList.get(0);
				if (ocrMap != null) {
					List<Map<String, Object>> fieldsList = (List<Map<String, Object>>) ocrMap.get("fields");
					if (fieldsList != null) {
						StringBuilder buffer = new StringBuilder();
						for (Map<String, Object> field : fieldsList) {
							String inferText = (String) field.get("inferText"); // 추론된 문자열 추출
							buffer.append(inferText);
							buffer.append(" ");
						}
						return buffer.toString();
					}
				}
			}
		} catch (Throwable e) {
			// 예외는 무시
		}
		return "";
	}

	/**
	 * OCR 판독
	 * @param apigwUrl API 게이트웨이 주소
	 * @param secretKey 시크릿키
	 * @param imageFormat 판독할 이미지 주소
	 * @param imageFile 이미지 파일
	 * @return 판독 결과 문자열
	 */
	@SuppressWarnings("unchecked")
	public static String ocr(String apigwUrl, String secretKey, String imageFormat, File imageFile) {
		Map<String, String> headerMap = new HashMap<String, String>(); // 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		headerMap.put("Content-Type", "application/json; charset=utf-8");
		headerMap.put("X-OCR-SECRET", secretKey);
		/* 파라미터 설명
		[필수]version: 버전 정보 필수로 V1 입력합니다.(string)
		[필수]requestId: API 호출 UUID입니다.(string)
		[필수]timestamp: API 호출 Timestamp 값입니다.	
		<옵션>lang: OCR 인식시 요청할 언어 정보입니다.	lang 필드가 설정되지 않은 경우, ‘ko’가 default로 설정됩니다.(string)
		[필수]images: Json array로 입력되며, 현재는 1개의 이미지 입력만 허용 됩니다. (한국어/일본어)(json array)
		[필수]images.format: 이미지 포맷을 설정합니다.	“jpg”, “jpeg”, “png” 이미지 형식을 지원합니다.(string)
		<옵션>images.url: images.url 혹은 image.data 중 하나가 존재해야 합니다.	URL은 이미지를 가져 올 수 있는 공개 URL이어야 합니다.(string)
		<옵션>image.data: images.url 혹은 image.data 중 하나가 존재해야 합니다. URL과 데이터가 모두 존재하면 데이터를 사용하고 URL은 무시합니다. image.data는 base64 인코딩 이미지 바이트입니다.(string)
		[필수]image.name: 이미지 명을 입력합니다. 이미지를 식별하는데 사용되며, 응답 결과 확인에 사용됩니다.(string)
		<옵션>image.templateIds: 텍스트 OCR API에서는 사용되지 않습니다. Template OCR API에서는 이 필드를 설정하지 않으면 도메인에 배포된 모든 서비스 템플릿으로 자동 분류됩니다.(json array)
		 */
		long timestamp = new Date().getTime();
		Map<String, Object> paramMap = new HashMap<String, Object>(); // 파라미터 맵
		try {
			// images 맵 생성
			Map<String, String> imageMap = new HashMap<String, String>();
			imageMap.put("format", imageFormat);
			imageMap.put("name", "ocr_image");
			byte[] fileContent = FileUtils.readFileToByteArray(imageFile);
			String encodedString = Base64.encodeBase64String(fileContent);
			imageMap.put("data", encodedString);
			paramMap.put("images", Arrays.asList(imageMap));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 파라미터 맵 생성
		paramMap.put("lang", "ko");
		paramMap.put("requestId", UUID.randomUUID().toString());
		paramMap.put("timestamp", timestamp);
		paramMap.put("version", "V1");
		// 결과 맵 생성
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.post(apigwUrl, JsonUtil.stringify(paramMap), headerMap);
			String json = result.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			List<Map<String, Object>> imagesList = (List<Map<String, Object>>) resultMap.get("images");
			if (imagesList != null && imagesList.size() > 0) {
				Map<String, Object> ocrMap = imagesList.get(0);
				if (ocrMap != null) {
					List<Map<String, Object>> fieldsList = (List<Map<String, Object>>) ocrMap.get("fields");
					if (fieldsList != null) {
						StringBuilder buffer = new StringBuilder();
						for (Map<String, Object> field : fieldsList) {
							String inferText = (String) field.get("inferText"); // 추론된 문자열 추출
							buffer.append(inferText);
							buffer.append(" ");
						}
						return buffer.toString();
					}
				}
			}
		} catch (Throwable e) {
			// 예외는 무시
		}
		return "";
	}

	/**
	 * STT (Speech-To-Text)
	 * @param clientId 앱 등록 시 발급받은 Client ID
	 * @param clientSecret 앱 등록 시 발급 받은 Client Secret
	 * @param lang 언어 코드 (Kor, Jpn, Eng, Chn)
	 * @param soundFile 음성 파일 (mp3, aac, ac3, ogg, flac, wav - 최대 60초)
	 * @return 인식 결과 문자열
	 */
	@SuppressWarnings("unchecked")
	public static String stt(String clientId, String clientSecret, String lang, File soundFile) {
		String subUrl = STT_API_URL + "?lang=" + lang;
		String fullUrl = STT_API_DOMAIN + subUrl;
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("X-NCP-APIGW-API-KEY-ID", clientId);
		headerMap.put("X-NCP-APIGW-API-KEY", clientSecret);
		headerMap.put("Content-Type", "application/octet-stream");
		// 결과 맵 생성
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.post(fullUrl, soundFile, headerMap);
			String json = result.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			if (resultMap != null) {
				return (String) resultMap.get("text");
			}
		} catch (Throwable e) {
			// 예외는 무시
		}
		return "";
	}

	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드

	/**
	 * SMS/LMS 발송
	 * @param accessKey 엑세스키
	 * @param secretKey 시크릿키
	 * @param serviceId 서비스아이디
	 * @param type SMS Type - SMS, LMS, MMS(소문자 가능)
	 * @param from 발신번호
	 * @param subject 메시지 제목
	 * @param content 메시지 내용
	 * @param to 수신번호(-를 제외한 숫자만 입력 가능)
	 * @return 응답결과
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> sendSmsLms(String accessKey, String secretKey, String serviceId, String type, String from, String subject, String content, String to) {
		/* 헤더 설명
		[필수]Content-Type: 요청 Body Content Type을 application/json으로 지정
		[필수]x-ncp-apigw-timestamp: 1970년 1월 1일 00:00:00 협정 세계시(UTC)부터의 경과 시간을 밀리초(Millisecond)로 나타낸 것이다. API Gateway 서버와 시간 차가 5분 이상 나는 경우 유효하지 않은 요청으로 간주
		[필수]x-ncp-iam-access-key: 포탈 또는 Sub Account에서 발급받은 Access Key ID
		[필수]x-ncp-apigw-signature-v2: 위 예제의 Body를 Access Key Id와 맵핑되는 SecretKey로 암호화한 서명. HMAC 암호화 알고리즘은 HmacSHA256 사용
		 */
		String subUrl = SMS_API_URL + "/services/" + serviceId + "/messages";
		String fullUrl = SMS_API_DOMAIN + subUrl;
		long timestamp = new Date().getTime();
		Map<String, String> headerMap = new HashMap<String, String>(); // 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		headerMap.put("Content-Type", "application/json; charset=utf-8");
		headerMap.put("x-ncp-apigw-timestamp", String.valueOf(timestamp));
		headerMap.put("x-ncp-iam-access-key", accessKey);
		headerMap.put("x-ncp-apigw-signature-v2", makeSignature("POST", subUrl, accessKey, secretKey, timestamp));
		/* 파라미터 설명
		[필수]type: SMS Type - SMS, LMS, MMS (소문자 가능)(String)
		<옵션>contentType: 메시지 Type - COMM: 일반메시지, AD: 광고메시지(default: COMM)(String)
		<옵션>countryCode: 국가번호 - SENS에서 제공하는 국가`로의 발송만 가능(default: 82)(String)
		[필수]from: 발신번호 - 사전 등록된 발신번호만 사용 가능(String)
		<옵션>subject: 기본 메시지 제목 - LMS, MMS에서만 사용 가능(String)
		[필수]content: 기본 메시지 내용 - SMS: 최대 80byte, LMS, MMS: 최대 2000bytes(String)
		[필수]messages: 메시지 정보 - 아래 항목들 참조 (messages.XXX), 최대 1,000개(String)
		[필수]messages.to: 수신번호 - "-"를 제외한 숫자만 입력 가능(String)
		<옵션>messages.subject: 개별 메시지 제목 - LMS, MMS에서만 사용 가능(String)
		<옵션>messages.content: 개별 메시지 내용 - SMS: 최대 80bytes, LMS, MMS: 최대 2000bytes(String)
		<옵션>files.name: 파일 이름 - MMS에서만 사용 가능, jpg, jpeg 확장자를 가진 파일 이름, 최대 40자(String)
		<옵션>files.body: 파일 바디 - MMS에서만 사용 가능, jpg, jpeg 이미지를 Base64로 인코딩한 값, 원 파일 기준 최대 300Kbyte, 파일 명 최대 40자, 해상도 최대 1500 * 1500(String)
		<옵션>reserveTime: 예약 일시 - 메시지 발송 예약 일시 (yyyy-MM-dd HH:mm)(String)
		<옵션>reserveTimeZone: 예약 일시 타임존 - 예약 일시 타임존 (기본: Asia/Seoul)(String)
		<옵션>scheduleCode: 스케줄 코드 - 등록하려는 스케줄 코드(String)
		 */
		Map<String, Object> paramMap = new HashMap<String, Object>(); // 파라미터 맵
		paramMap.put("type", type);
		paramMap.put("subject", subject);
		paramMap.put("from", from);
		paramMap.put("content", content);
		Map<String, String> messagesMap = new HashMap<String, String>();
		messagesMap.put("to", to);
		paramMap.put("messages", Arrays.asList(messagesMap));
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.post(fullUrl, JsonUtil.stringify(paramMap), headerMap);
			/* 응답 객체 설명
			[필수]requestId: 요청 아이디(String)	
			[필수]requestTime: 요청 시간(DateTime)	
			[필수]statusCode: 요청 상태 코드. 202 - 성공, 그외 - 실패(String)
			[필수]statusName: 요청 상태명. success - 성공, fail - 실패(String)
			 */
			String json = result.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			if (result.getStatusCode() == 202) {
				resultMap.put("ok", Boolean.TRUE);
			} else { // 실패
				resultMap.put("ok", Boolean.FALSE);
			}
		} catch (Throwable e) {
			resultMap.put("ok", Boolean.FALSE);
			resultMap.put("statusCode", 999);
			resultMap.put("statusName", "Http 통신 실패");
		}
		return resultMap;
	}

	/**
	 * 이메일 발송
	 * @param accessKey 엑세스키
	 * @param secretKey 시크릿키
	 * @param fromEmail 발신자 이메일주소
	 * @param fromName 발신자명
	 * @param templateSid 템플릿 아이디
	 * @param subject 메일 제목
	 * @param content 메일 본분
	 * @param toEmail 수신자 이메일주소
	 * @param toName 수신자명
	 * @param parameters 치환 파라미터
	 * @return 응답결과
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> sendEmail(String accessKey, String secretKey, String fromEmail, String fromName, Integer templateSid, String subject, String content, String toEmail, String toName, Map<String, Object> parameters) {
		/* 헤더 설명
		[필수]Content-Type: 요청 Body Content Type을 application/json으로 지정
		[필수]x-ncp-apigw-timestamp: 1970년 1월 1일 00:00:00 협정 세계시(UTC)부터의 경과 시간을 밀리초(Millisecond)로 나타낸 것이다. API Gateway 서버와 시간 차가 5분 이상 나는 경우 유효하지 않은 요청으로 간주
		[필수]x-ncp-iam-access-key: 포탈 또는 Sub Account에서 발급받은 Access Key ID
		[필수]x-ncp-apigw-signature-v2: 위 예제의 Body를 Access Key Id와 맵핑되는 SecretKey로 암호화한 서명. HMAC 암호화 알고리즘은 HmacSHA256 사용
		 */
		String subUrl = EMAIL_API_URL;
		String fullUrl = EMAIL_API_DOMAIN + subUrl;
		long timestamp = new Date().getTime();
		Map<String, String> headerMap = new HashMap<String, String>(); // 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		headerMap.put("Content-Type", "application/json; charset=utf-8");
		headerMap.put("x-ncp-apigw-timestamp", String.valueOf(timestamp));
		headerMap.put("x-ncp-iam-access-key", accessKey);
		headerMap.put("x-ncp-apigw-signature-v2", makeSignature("POST", subUrl, accessKey, secretKey, timestamp));
		/* 파라미터 설명
		<조건>senderAddress: templateSid가 전달되지 않으면 필수, 도메인에 naver.com, navercorp.com 등 사용불가, 발송자 Email 주소. 임의의 도메인 주소 사용하셔도 됩니다만, 가능하면 발신자 소유의 도메인 Email 계정을 사용하실 것을 권고드립니다. DMARC가 적용된 ‘id@naver.com’와 같은 포털사이트의 웹메일 계정을 사용하실 경우 DMARC 검사에 실패하게 되어 수신 측 정책에 따라 스팸 처리될 가능성이 높아집니다.(String)
		<옵션>senderName: 발송자 이름, Max:69(String)
		<옵션>templateSid: 템플릿 ID(Integer)
		<조건>title: Mail 제목. templateSid가 전달되지 않으면 필수, Min:0, Max:500(String)
		<조건>body: Email 본문. templateSid가 전달되지 않으면 필수, Max:500KB (광고메일일 경우 수신거부 메시지를 포함하여 계산됩니다.)(String)
		<조건>recipients: 수신자목록(List)
		<옵션>parameters: 치환 파라미터 (전체 수신자에게 적용), '치환 ID'를 key로, '치환 ID에 맵핑되는 값'을 value로 가지는 Map 형태의 Object(Object)
		 */
		Map<String, Object> paramMap = new HashMap<String, Object>(); // 파라미터 맵
		paramMap.put("senderAddress", fromEmail);
		paramMap.put("senderName", fromName);
		paramMap.put("templateSid", templateSid);
		paramMap.put("title", subject);
		paramMap.put("body", content);
		Map<String, String> recipientsMap = new HashMap<String, String>();
		recipientsMap.put("address", toEmail);
		recipientsMap.put("name", toName);
		recipientsMap.put("type", "R");
		paramMap.put("recipients", Arrays.asList(recipientsMap));
		paramMap.put("parameters", parameters);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.post(fullUrl, JsonUtil.stringify(paramMap), headerMap);
			/* 응답 객체 설명
			[필수]requestId: Email 발송 요청 ID (각 요청을 구분하는 ID, 한번에 여러건에 메일 발송을 요청할 경우 requestId가 여러개의 mailId를 포함할 수 있다.(String)
			[필수]count: 메일 요청 건수(Integer)	
			 */
			String json = result.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			if (result.getStatusCode() == 201) {
				resultMap.put("ok", Boolean.TRUE);
			} else { // 실패
				resultMap.put("ok", Boolean.FALSE);
			}
		} catch (Throwable e) {
			resultMap.put("ok", Boolean.FALSE);
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