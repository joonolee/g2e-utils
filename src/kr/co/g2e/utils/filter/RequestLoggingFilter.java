package kr.co.g2e.utils.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.g2e.utils.util.Params;
import kr.co.g2e.utils.util.StringUtil;

/**
 * 요청을 로깅 하는 필터
 */
public class RequestLoggingFilter implements Filter {
	private static final Log logger = LogFactory.getLog(RequestLoggingFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		MyRequestWrapper reqWrapper = new MyRequestWrapper((HttpServletRequest) request); // 요청 Body를 여러번 읽을 수 있도록 Wrapper로 감싼다.
		long start = 0;
		if (logger.isDebugEnabled()) {
			start = System.nanoTime();
			logger.debug("★★★ " + getIpAddr(reqWrapper) + " 로 부터 \"" + reqWrapper.getMethod() + " " + reqWrapper.getRequestURI() + "\" 요청이 시작되었습니다");
			logger.debug("ContentLength: " + reqWrapper.getContentLength() + " bytes");
			logger.debug(Params.getParamsFromHeader(reqWrapper).toString());
			logger.debug(Params.getParamsFromCookie(reqWrapper).toString());
			logger.debug(Params.getParams(reqWrapper).toString());
		}
		filterChain.doFilter(reqWrapper, response);
		if (logger.isDebugEnabled()) {
			logger.debug("☆☆☆ " + getIpAddr(reqWrapper) + " 로 부터 \"" + reqWrapper.getMethod() + " " + reqWrapper.getRequestURI() + "\" 요청이 종료되었습니다 | duration : " + (System.nanoTime() - start) / 1000000 + " msecs");
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	private String getIpAddr(HttpServletRequest request) {
		return StringUtil.null2Str(request.getHeader("X-Forwarded-For"), request.getRemoteAddr());
	}

	/**
	 * 요청 본문(Body)를 여러번 읽을 수 있도록 만드는 Wrapper 객체
	 */
	class MyRequestWrapper extends HttpServletRequestWrapper {
		private final byte[] requestBody; // 요청본문
		private final Charset charset; // 캐릭터셋
		private final Map<String, String[]> parameterMap = new HashMap<String, String[]>(); // 파라미터 맵

		public MyRequestWrapper(HttpServletRequest request) {
			super(request);
			String charEncoding = request.getCharacterEncoding(); // 인코딩
			for (Object obj : request.getParameterMap().keySet()) {
				String key = (String) obj;
				parameterMap.put(key, request.getParameterValues(key));
			}
			this.charset = StringUtil.isEmpty(charEncoding) ? StandardCharsets.UTF_8 : Charset.forName(charEncoding);
			try {
				InputStream is = request.getInputStream();
				requestBody = IOUtils.toByteArray(is);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getParameter(String name) {
			String[] values = parameterMap.get(name);
			if (values == null) {
				return null;
			}
			return values[0];
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return Collections.unmodifiableMap(parameterMap);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Enumeration getParameterNames() {
			return Collections.enumeration(parameterMap.keySet());
		}

		@Override
		public String[] getParameterValues(String name) {
			return parameterMap.get(name);
		}

		@Override
		public ServletInputStream getInputStream() {
			final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestBody);
			return new ServletInputStream() {
				@Override
				public int read() {
					return byteArrayInputStream.read();
				}
			};
		}

		@Override
		public BufferedReader getReader() {
			return new BufferedReader(new InputStreamReader(this.getInputStream(), charset));
		}
	}
}