package kr.co.g2e.utils.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import kr.co.g2e.utils.util.HttpUtil.Result;

/**
 * Google Cloud Platform 기능을 이용할 수 있는 유틸리티 클래스
 */
public class GcloudUtil {
	/**
	 * OCR API
	 */
	private static final String OCR_API_DOMAIN = "https://vision.googleapis.com";
	private static final String OCR_API_URL = "/v1/images:annotate";

	/**
	 * STT API
	 */
	private static final String STT_API_DOMAIN = "https://speech.googleapis.com";
	private static final String STT_API_URL = "/v1/speech:recognize";

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private GcloudUtil() {
	}

	/**
	 * OCR 판독
	 * @param subscriptionKey 서브스크립션키
	 * @param imageUrl 이미지 주소
	 * @return 판독 결과 문자열
	 */
	public static String ocr(String subscriptionKey, String imageUrl) {
		String subUrl = OCR_API_URL;
		String fullUrl = OCR_API_DOMAIN + subUrl;
		Map<String, String> headerMap = new HashMap<String, String>(); // 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		headerMap.put("Content-Type", "application/json");
		headerMap.put("Authorization", "Bearer " + subscriptionKey);
		Map<String, Object> paramMap = new HashMap<String, Object>(); // 파라미터 맵
		Map<String, Object> requestMap = new HashMap<String, Object>(); // 요청 맵
		Map<String, Object> imageMap = new HashMap<String, Object>(); // 이미지 맵
		imageMap.put("uri", imageUrl);
		requestMap.put("image", imageMap);
		Map<String, Object> featureMap = new HashMap<String, Object>(); // 특징 맵
		featureMap.put("type", "TEXT_DETECTION");
		requestMap.put("features", Arrays.asList(featureMap));
		paramMap.put("requests", Arrays.asList(requestMap));
		// 결과 맵 생성
		//Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.post(fullUrl, JsonUtil.stringify(paramMap), headerMap);
			String json = result.getContent();
			return json;
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
	public static String ocr(String subscriptionKey, File imageFile) {
		String subUrl = OCR_API_URL;
		String fullUrl = OCR_API_DOMAIN + subUrl;
		Map<String, String> headerMap = new HashMap<String, String>(); // 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		headerMap.put("Content-Type", "application/json");
		headerMap.put("Authorization", "Bearer " + subscriptionKey);
		Map<String, Object> paramMap = new HashMap<String, Object>(); // 파라미터 맵
		Map<String, Object> requestMap = new HashMap<String, Object>(); // 요청 맵
		try {
			// 이미지 맵 생성
			Map<String, String> imageMap = new HashMap<String, String>();
			byte[] fileContent = FileUtils.readFileToByteArray(imageFile);
			String encodedString = Base64.encodeBase64String(fileContent);
			imageMap.put("content", encodedString);
			requestMap.put("image", imageMap);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Map<String, Object> featureMap = new HashMap<String, Object>(); // 특징 맵
		featureMap.put("type", "TEXT_DETECTION");
		requestMap.put("features", Arrays.asList(featureMap));
		paramMap.put("requests", Arrays.asList(requestMap));
		// 결과 맵 생성
		//Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.post(fullUrl, JsonUtil.stringify(paramMap), headerMap);
			String json = result.getContent();
			return json;
		} catch (Throwable e) {
			// 예외는 무시
		}
		return "";
	}

	public static String stt(String apiKey, File audioFile) {
		String subUrl = STT_API_URL + "?key=" + apiKey;
		String fullUrl = STT_API_DOMAIN + subUrl;
		Map<String, Object> paramMap = new HashMap<String, Object>(); // 파라미터 맵
		// config 맵 생성
		Map<String, String> configMap = new HashMap<String, String>();
		paramMap.put("config", configMap);
		try {
			// audio 맵 생성
			Map<String, String> audioMap = new HashMap<String, String>();
			byte[] fileContent = FileUtils.readFileToByteArray(audioFile);
			String encodedString = Base64.encodeBase64String(fileContent);
			audioMap.put("content", encodedString);
			paramMap.put("audio", audioMap);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// 결과 맵 생성
		//Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.post(fullUrl, JsonUtil.stringify(paramMap));
			String json = result.getContent();
			return json;
		} catch (Throwable e) {
			// 예외는 무시
		}
		return "";
	}
}