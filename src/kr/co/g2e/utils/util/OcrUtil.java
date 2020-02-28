package kr.co.g2e.utils.util;

import java.io.File;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import kr.co.g2e.utils.util.HttpUtil.Result;

/**
 * https://ocr.space/ 에서 제공하는 ocr 판독 기능을 이용할 수 있는 유틸리티 클래스
 * 
 * 상세 내용 참고 : https://ocr.space/OCRAPI
 */
public class OcrUtil {

	/**
	 * OCR API
	 */
	private static final String OCR_API_URL = "https://api.ocr.space/parse/image";

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private OcrUtil() {
	}

	/**
	 * OCR 판독
	 * @param apiKey API키
	 * @param imageUrl 이미지 URL
	 * @return 판독 결과 문자열
	 */
	@SuppressWarnings("unchecked")
	public static String ocr(String apiKey, String imageUrl) {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("apikey", apiKey);
		// 결과 맵 생성
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Map<String, String> paramMap = new HashMap<String, String>(); // 파라미터 맵
			paramMap.put("url", imageUrl);
			paramMap.put("OCREngine", "2");
			// 요청
			Result result = HttpUtil.post(OCR_API_URL, paramMap, headerMap);
			String json = result.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			List<Map<String, Object>> resultList = (List<Map<String, Object>>) resultMap.get("ParsedResults");
			if (resultList != null) {
				StringBuilder buffer = new StringBuilder();
				for (Map<String, Object> field : resultList) {
					String inferText = (String) field.get("ParsedText"); // 추론한 문자열 추출
					buffer.append(inferText.replaceAll("\n", " "));
					buffer.append(" ");
				}
				return buffer.toString();
			}
		} catch (Throwable e) {
			// 예외는 무시
		}
		return "";
	}

	/**
	 * OCR 판독
	 * @param apiKey API키
	 * @param imageFile 이미지 파일
	 * @return 판독 결과 문자열
	 */
	@SuppressWarnings("unchecked")
	public static String ocr(String apiKey, File imageFile) {
		Map<String, String> headerMap = new HashMap<String, String>();
		//headerMap.put("Content-Type", "image/jpeg");
		headerMap.put("apikey", apiKey);
		// 결과 맵 생성
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Map<String, String> paramMap = new HashMap<String, String>(); // 파라미터 맵
			byte[] fileContent = FileUtils.readFileToByteArray(imageFile);
			String encodedString = Base64.encodeBase64String(fileContent);
			String mimeType = URLConnection.guessContentTypeFromName(imageFile.getName());
			paramMap.put("base64Image", "data:" + mimeType + ";base64," + encodedString);
			paramMap.put("OCREngine", "2");
			// 요청
			Result result = HttpUtil.post(OCR_API_URL, paramMap, headerMap);
			String json = result.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			List<Map<String, Object>> resultList = (List<Map<String, Object>>) resultMap.get("ParsedResults");
			if (resultList != null) {
				StringBuilder buffer = new StringBuilder();
				for (Map<String, Object> field : resultList) {
					String inferText = (String) field.get("ParsedText"); // 추론한 문자열 추출
					buffer.append(inferText.replaceAll("\n", " "));
					buffer.append(" ");
				}
				return buffer.toString();
			}
		} catch (Throwable e) {
			// 예외는 무시
		}
		return "";
	}
}