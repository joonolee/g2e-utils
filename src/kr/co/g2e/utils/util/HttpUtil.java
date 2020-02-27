package kr.co.g2e.utils.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * HTTP 클라이언트의 기능을 이용할 수 있는 유틸리티 클래스
 */
public final class HttpUtil {
	private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private HttpUtil() {
	}

	/**
	 * Result 객체
	 */
	public static class Result {
		private int statusCode;
		private String content;
		private Map<String, String> headerMap;

		public Result(int statusCode, String content, Map<String, String> headerMap) {
			this.statusCode = statusCode;
			this.content = content;
			this.headerMap = headerMap;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getContent() {
			return content;
		}

		public Map<String, String> getHeaderMap() {
			return Collections.unmodifiableMap(this.headerMap);
		}

		@Override
		public String toString() {
			return String.format("Result={ statusCode : %d, content : %s, headerMap : %s }", getStatusCode(), getContent(), getHeaderMap());
		}
	}

	/**
	 * url 을 Get 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @return Result 객체
	 */
	public static Result get(String url) {
		return get(url, null);
	}

	/**
	 * url 을 Get 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result get(String url, int timeoutMilliseconds) {
		return get(url, null, timeoutMilliseconds);
	}

	/**
	 * url 을 Get 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result get(String url, Map<String, String> headerMap) {
		return get(url, headerMap, 0);
	}

	/**
	 * url 을 Get 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param headerMap 헤더 맵 객체
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result get(String url, Map<String, String> headerMap, int timeoutMilliseconds) {
		int statusCode = 0;
		Map<String, String> responseHeaderMap = new HashMap<String, String>();
		String content = "";
		CloseableHttpClient httpClient = null;
		try {
			if (timeoutMilliseconds > 0) {
				RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(timeoutMilliseconds)
					.build();
				SocketConfig socketConfig = SocketConfig.custom()
					.setSoTimeout(timeoutMilliseconds)
					.build();
				httpClient = HttpClients.custom()
					.setDefaultRequestConfig(requestConfig)
					.setDefaultSocketConfig(socketConfig)
					.build();
			} else {
				httpClient = HttpClients.createDefault();
			}
			HttpGet httpGet = new HttpGet(url);
			if (headerMap != null) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpGet.addHeader(entry.getKey(), entry.getValue());
				}
			}
			HttpResponse response = httpClient.execute(httpGet);
			statusCode = response.getStatusLine().getStatusCode();
			for (Header header : response.getAllHeaders()) {
				responseHeaderMap.put(header.getName(), header.getValue());
			}
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
				}
			}
		}
		return new Result(statusCode, content, responseHeaderMap);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @return Result 객체
	 */
	public static Result post(String url) {
		return post(url, (Map<String, String>) null, (Map<String, String>) null);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, int timeoutMilliseconds) {
		return post(url, (Map<String, String>) null, (Map<String, String>) null, timeoutMilliseconds);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap) {
		return post(url, paramMap, (Map<String, String>) null);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, int timeoutMilliseconds) {
		return post(url, paramMap, (Map<String, String>) null, timeoutMilliseconds);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, Map<String, String> headerMap) {
		return post(url, paramMap, headerMap, 0);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param headerMap 헤더 맵 객체
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, Map<String, String> headerMap, int timeoutMilliseconds) {
		int statusCode = 0;
		Map<String, String> responseHeaderMap = new HashMap<String, String>();
		String content = "";
		CloseableHttpClient httpClient = null;
		try {
			if (timeoutMilliseconds > 0) {
				RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(timeoutMilliseconds)
					.build();
				SocketConfig socketConfig = SocketConfig.custom()
					.setSoTimeout(timeoutMilliseconds)
					.build();
				httpClient = HttpClients.custom()
					.setDefaultRequestConfig(requestConfig)
					.setDefaultSocketConfig(socketConfig)
					.build();
			} else {
				httpClient = HttpClients.createDefault();
			}
			HttpPost httpPost = new HttpPost(url);
			if (headerMap != null) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			if (paramMap != null) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> entry : paramMap.entrySet()) {
					params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, "UTF-8");
				httpPost.setEntity(ent);
			}
			HttpResponse response = httpClient.execute(httpPost);
			statusCode = response.getStatusLine().getStatusCode();
			for (Header header : response.getAllHeaders()) {
				responseHeaderMap.put(header.getName(), header.getValue());
			}
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
				}
			}
		}
		return new Result(statusCode, content, responseHeaderMap);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다. (첨부파일 포함)
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param fileList 파일 리스트 객체
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, List<File> fileList) {
		return post(url, paramMap, fileList, null);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다. (첨부파일 포함)
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param fileList 파일 리스트 객체
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, List<File> fileList, int timeoutMilliseconds) {
		return post(url, paramMap, fileList, null, timeoutMilliseconds);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다. (첨부파일 포함)
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param fileList 파일 리스트 객체
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, List<File> fileList, Map<String, String> headerMap) {
		return post(url, paramMap, fileList, headerMap, 0);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다. (첨부파일 포함)
	 * @param url url 주소
	 * @param paramMap 파라미터 맵 객체
	 * @param fileList 파일 리스트 객체
	 * @param headerMap 헤더 맵 객체
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, Map<String, String> paramMap, List<File> fileList, Map<String, String> headerMap, int timeoutMilliseconds) {
		int statusCode = 0;
		Map<String, String> responseHeaderMap = new HashMap<String, String>();
		String content = "";
		CloseableHttpClient httpClient = null;
		try {
			if (timeoutMilliseconds > 0) {
				RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(timeoutMilliseconds)
					.build();
				SocketConfig socketConfig = SocketConfig.custom()
					.setSoTimeout(timeoutMilliseconds)
					.build();
				httpClient = HttpClients.custom()
					.setDefaultRequestConfig(requestConfig)
					.setDefaultSocketConfig(socketConfig)
					.build();
			} else {
				httpClient = HttpClients.createDefault();
			}
			HttpPost httpPost = new HttpPost(url);
			if (headerMap != null) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			MultipartEntityBuilder meb = MultipartEntityBuilder.create();
			meb.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			if (paramMap != null) {
				for (Map.Entry<String, String> entry : paramMap.entrySet()) {
					meb.addTextBody(entry.getKey(), entry.getValue());
				}
			}
			if (fileList != null) {
				for (File file : fileList) {
					meb.addBinaryBody("userfile", file);
				}
			}
			httpPost.setEntity(meb.build());
			HttpResponse response = httpClient.execute(httpPost);
			statusCode = response.getStatusLine().getStatusCode();
			for (Header header : response.getAllHeaders()) {
				responseHeaderMap.put(header.getName(), header.getValue());
			}
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
				}
			}
		}
		return new Result(statusCode, content, responseHeaderMap);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열(컨텐트 타입: application/x-www-form-urlencoded)
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr) {
		return post(url, paramStr, DEFAULT_CONTENT_TYPE, (Map<String, String>) null);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열
	 * @param contentType 파라미터 컨텐트 타입(예: application/x-www-form-urlencoded, application/json)
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr, String contentType) {
		return post(url, paramStr, contentType, (Map<String, String>) null);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr, int timeoutMilliseconds) {
		return post(url, paramStr, DEFAULT_CONTENT_TYPE, (Map<String, String>) null, timeoutMilliseconds);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열
	 * @param contentType 파라미터 컨텐트 타입(예: application/x-www-form-urlencoded, application/json)
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr, String contentType, int timeoutMilliseconds) {
		return post(url, paramStr, contentType, (Map<String, String>) null, timeoutMilliseconds);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr, Map<String, String> headerMap) {
		return post(url, paramStr, null, headerMap, 0);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열
	 * @param contentType 파라미터 컨텐트 타입(예: application/x-www-form-urlencoded, application/json)
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr, String contentType, Map<String, String> headerMap) {
		return post(url, paramStr, contentType, headerMap, 0);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열
	 * @param headerMap 헤더 맵 객체
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr, Map<String, String> headerMap, int timeoutMilliseconds) {
		return post(url, paramStr, null, headerMap, timeoutMilliseconds);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param paramStr 파라미터 문자열
	 * @param contentType 파라미터 컨텐트 타입(예: application/x-www-form-urlencoded, application/json)
	 * @param headerMap 헤더 맵 객체
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, String paramStr, String contentType, Map<String, String> headerMap, int timeoutMilliseconds) {
		int statusCode = 0;
		Map<String, String> responseHeaderMap = new HashMap<String, String>();
		String content = "";
		CloseableHttpClient httpClient = null;
		try {
			if (timeoutMilliseconds > 0) {
				RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(timeoutMilliseconds)
					.build();
				SocketConfig socketConfig = SocketConfig.custom()
					.setSoTimeout(timeoutMilliseconds)
					.build();
				httpClient = HttpClients.custom()
					.setDefaultRequestConfig(requestConfig)
					.setDefaultSocketConfig(socketConfig)
					.build();
			} else {
				httpClient = HttpClients.createDefault();
			}
			HttpPost httpPost = new HttpPost(url);
			if (headerMap != null) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			if (paramStr != null) {
				StringEntity ent = new StringEntity(paramStr, "UTF-8");
				if (contentType != null) {
					ent.setContentType(contentType);
				}
				httpPost.setEntity(ent);
			}
			HttpResponse response = httpClient.execute(httpPost);
			statusCode = response.getStatusLine().getStatusCode();
			for (Header header : response.getAllHeaders()) {
				responseHeaderMap.put(header.getName(), header.getValue());
			}
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
				}
			}
		}
		return new Result(statusCode, content, responseHeaderMap);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param file 전송할 파일
	 * @return Result 객체
	 */
	public static Result post(String url, File file) {
		return post(url, file, null, 0);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param file 전송할 파일
	 * @param headerMap 헤더 맵 객체
	 * @return Result 객체
	 */
	public static Result post(String url, File file, Map<String, String> headerMap) {
		return post(url, file, headerMap, 0);
	}

	/**
	 * url 을 Post 방식으로 호출하고 결과를 리턴한다.
	 * @param url url 주소
	 * @param file 전송할 파일
	 * @param headerMap 헤더 맵 객체
	 * @param timeoutMilliseconds 소켓 타임아웃 시간(밀리세컨드)
	 * @return Result 객체
	 */
	public static Result post(String url, File file, Map<String, String> headerMap, int timeoutMilliseconds) {
		int statusCode = 0;
		Map<String, String> responseHeaderMap = new HashMap<String, String>();
		String content = "";
		CloseableHttpClient httpClient = null;
		try {
			if (timeoutMilliseconds > 0) {
				RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(timeoutMilliseconds)
					.build();
				SocketConfig socketConfig = SocketConfig.custom()
					.setSoTimeout(timeoutMilliseconds)
					.build();
				httpClient = HttpClients.custom()
					.setDefaultRequestConfig(requestConfig)
					.setDefaultSocketConfig(socketConfig)
					.build();
			} else {
				httpClient = HttpClients.createDefault();
			}
			HttpPost httpPost = new HttpPost(url);
			if (headerMap != null) {
				for (Map.Entry<String, String> entry : headerMap.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			if (file != null) {
				FileEntity ent = new FileEntity(file);
				httpPost.setEntity(ent);
			}
			HttpResponse response = httpClient.execute(httpPost);
			statusCode = response.getStatusLine().getStatusCode();
			for (Header header : response.getAllHeaders()) {
				responseHeaderMap.put(header.getName(), header.getValue());
			}
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				content = EntityUtils.toString(resEntity);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
				}
			}
		}
		return new Result(statusCode, content, responseHeaderMap);
	}
}