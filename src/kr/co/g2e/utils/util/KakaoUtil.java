package kr.co.g2e.utils.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.g2e.utils.util.HttpUtil.Result;

/**
 * 카카오 OPEN API 기능을 이용할 수 있는 유틸리티 클래스
 */
public class KakaoUtil {
	/**
	 * REVERSE GEOCODING API
	 */
	private static final String REVERSE_GEOCODING_API_URL = "https://dapi.kakao.com/v2/local/geo/coord2address.json";

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private KakaoUtil() {
	}

	/**
	 * 카카오 역지오코딩 API 를 활용하여 좌표값으로 도로명 주소를 얻어온다.
	 * @param adminKey 어드민 키
	 * @param longitude 경도
	 * @param latitude 위도
	 * @return 좌표에 해당하는 도로명 주소 값
	 */
	@SuppressWarnings("unchecked")
	public static String reverseGeocoding(String adminKey, String longitude, String latitude) {
		String subUrl = "?x=" + longitude + "&y=" + latitude;
		String fullUrl = REVERSE_GEOCODING_API_URL + subUrl;
		Map<String, String> headerMap = new HashMap<String, String>(); // 헤더 맵(값은 UTF-8 URL인코딩하여 셋팅해야 함)
		headerMap.put("Authorization", "KakaoAK " + adminKey);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			// 요청
			Result result = HttpUtil.get(fullUrl, headerMap);
			String json = result.getContent();
			resultMap = (Map<String, Object>) JsonUtil.parse(json);
			List<Map<String, Object>> documentsList = (List<Map<String, Object>>) resultMap.get("documents");
			if (documentsList != null && documentsList.size() > 0) {
				Map<String, Object> addressMap = documentsList.get(0);
				if (addressMap != null) {
					Map<String, Object> roadAddressMap = (Map<String, Object>) addressMap.get("road_address");
					if (roadAddressMap != null) {
						String addressName = (String) roadAddressMap.get("address_name");
						if (addressName != null) {
							return addressName;
						}
					}
				}
			}
		} catch (Throwable e) {
			// 예외는 무시
		}
		return "";
	}
}