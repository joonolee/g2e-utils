package kr.co.g2e.utils.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
		long currTime = 0;
		if (logger.isDebugEnabled()) {
			currTime = System.nanoTime();
			logger.debug("★★★ " + getIpAddr(reqWrapper) + " 로 부터 \"" + reqWrapper.getMethod() + " " + reqWrapper.getRequestURI() + "\" 요청이 시작되었습니다");
			logger.debug("ContentLength: " + reqWrapper.getContentLength() + " bytes");
			logger.debug(Params.getParamsFromHeader(reqWrapper).toString());
			logger.debug(Params.getParamsFromCookie(reqWrapper).toString());
			Params reqParams = Params.getParams(reqWrapper);
			logger.debug(reqParams.toString());
			logger.debug("ReqBody={ " + reqParams.getBody() + " }");
		}
		filterChain.doFilter(reqWrapper, response);
		if (logger.isDebugEnabled()) {
			logger.debug("☆☆☆ " + getIpAddr(reqWrapper) + " 로 부터 \"" + reqWrapper.getMethod() + " " + reqWrapper.getRequestURI() + "\" 요청이 종료되었습니다 | duration : " + (System.nanoTime() - currTime) + " ns\n");
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
		private byte[] requestBody;
		private Charset charset;

		public MyRequestWrapper(HttpServletRequest request) {
			super(request);
			String charEncoding = request.getCharacterEncoding(); // 인코딩
			this.charset = StringUtil.isEmpty(charEncoding) ? StandardCharsets.UTF_8 : Charset.forName(charEncoding);
			try {
				InputStream is = request.getInputStream();
				this.requestBody = IOUtils.toByteArray(is);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public ServletInputStream getInputStream() {
			final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.requestBody);
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