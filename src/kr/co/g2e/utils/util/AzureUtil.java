package kr.co.g2e.utils.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.g2e.utils.util.HttpUtil.Result;

/**
 * Microsoft Azure Cloud Service 기능을 이용할 수 있는 유틸리티 클래스
 */
public class AzureUtil {
	/**
	 * OCR API
	 */
	private static final String OCR_API_DOMAIN = "https://koreacentral.api.cognitive.microsoft.com";
	private static final String OCR_API_URL = "/vision/v2.0/recognizeText";

	/**
	 * STT API
	 */
	private static final String STT_API_DOMAIN = "https://koreacentral.stt.speech.microsoft.com";
	private static final String STT_API_URL = "/speech/recognition/conversation/cognitiveservices/v1";

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private AzureUtil() {
	}

	/**
	 * OCR 판독
	 * @param subscriptionKey 서브스크립션키
	 * @param imageUrl 이미지 주소
	 * @return 판독 결과 문자열
	 */
	@SuppressWarnings("unchecked")
	public static String ocr(String subscriptionKey, String imageUrl) {
		String subUrl = OCR_API_URL + "?mode=Printed";
		String fullUrl = OCR_API_DOMAIN + subUrl;
		Map<String, String> headerMap = new HashMap<String, String>(); // 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		headerMap.put("Content-Type", "application/json");
		headerMap.put("Ocp-Apim-Subscription-Key", subscriptionKey);
		Map<String, Object> paramMap = new HashMap<String, Object>(); // 파라미터 맵
		// 파라미터 맵 생성
		paramMap.put("url", imageUrl);
		// 결과 맵 생성
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result1 = HttpUtil.post(fullUrl, JsonUtil.stringify(paramMap), headerMap);
			// 헤더에서 값 추출: 키(Operation-Location), OCR 판독 결과 URL 값 리턴
			Map<String, String> responseHeaderMap = result1.getHeaderMap();
			Thread.sleep(1000); // 1초 대기
			// OCR 결과 요청
			Result result2 = HttpUtil.get(responseHeaderMap.get("Operation-Location"), headerMap);
			String json = result2.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			if ("Succeeded".equals(resultMap.get("status"))) {
				Map<String, Object> recognitionResultMap = (Map<String, Object>) resultMap.get("recognitionResult");
				if (recognitionResultMap != null) {
					List<Map<String, Object>> lineList = (List<Map<String, Object>>) recognitionResultMap.get("lines");
					if (lineList != null) {
						StringBuilder buffer = new StringBuilder();
						for (Map<String, Object> field : lineList) {
							String inferText = (String) field.get("text"); // 추론된 문자열 추출
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
	 * @param subscriptionKey 서브스크립션키
	 * @param imageFile 이미지 파일
	 * @return 판독 결과 문자열
	 */
	@SuppressWarnings("unchecked")
	public static String ocr(String subscriptionKey, File imageFile) {
		String subUrl = OCR_API_URL + "?mode=Printed";
		String fullUrl = OCR_API_DOMAIN + subUrl;
		Map<String, String> headerMap = new HashMap<String, String>(); // 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		headerMap.put("Content-Type", "application/octet-stream");
		headerMap.put("Ocp-Apim-Subscription-Key", subscriptionKey);
		// 결과 맵 생성
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result1 = HttpUtil.post(fullUrl, imageFile, headerMap);
			// 헤더에서 값 추출: 키(Operation-Location), OCR 판독 결과 URL 값 리턴
			Map<String, String> responseHeaderMap = result1.getHeaderMap();
			Thread.sleep(1000); // 1초 대기
			// OCR 결과 요청
			Result result2 = HttpUtil.get(responseHeaderMap.get("Operation-Location"), headerMap);
			String json = result2.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			if ("Succeeded".equals(resultMap.get("status"))) {
				Map<String, Object> recognitionResultMap = (Map<String, Object>) resultMap.get("recognitionResult");
				if (recognitionResultMap != null) {
					List<Map<String, Object>> lineList = (List<Map<String, Object>>) recognitionResultMap.get("lines");
					if (lineList != null) {
						StringBuilder buffer = new StringBuilder();
						for (Map<String, Object> field : lineList) {
							String inferText = (String) field.get("text"); // 추론된 문자열 추출
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
	 * @param subscriptionKey 서브스크립션키
	 * @param lang 언어
	 * @param soundFile 음성 파일 (ogg, wav - 최대 60초)
	 * @return 인식 결과 문자열
	 */
	@SuppressWarnings("unchecked")
	public static String stt(String subscriptionKey, String lang, File soundFile) {
		String subUrl = STT_API_URL + "?language=" + lang;
		String fullUrl = STT_API_DOMAIN + subUrl;
		Map<String, String> headerMap = new HashMap<String, String>();
		if ("ogg".equalsIgnoreCase(FileUtil.getFileExtension(soundFile))) {
			headerMap.put("Content-Type", "audio/ogg; codecs=opus");
		} else if ("wav".equalsIgnoreCase(FileUtil.getFileExtension(soundFile))) {
			headerMap.put("Content-Type", "audio/wav; codecs=pcm");
		} else {
			throw new RuntimeException("Unsupported audio format");
		}
		headerMap.put("Ocp-Apim-Subscription-Key", subscriptionKey);
		// 결과 맵 생성
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.post(fullUrl, soundFile, headerMap);
			String json = result.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			if ("Success".equals(resultMap.get("RecognitionStatus"))) {
				return (String) resultMap.get("DisplayText");
			}
		} catch (Throwable e) {
			// 예외는 무시
		}
		return "";
	}
}