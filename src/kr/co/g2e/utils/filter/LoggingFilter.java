package kr.co.g2e.utils.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.g2e.utils.util.Params;
import kr.co.g2e.utils.util.StringUtil;

/**
 * 요청과 응답을 로깅 하는 필터
 */
public class LoggingFilter implements Filter {
	private static final Log logger = LogFactory.getLog(LoggingFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		long currTime = 0;
		if (logger.isDebugEnabled()) {
			currTime = System.nanoTime();
			logger.debug("★★★ " + getIpAddr(req) + " 로 부터 \"" + req.getMethod() + " " + req.getRequestURI() + "\" 요청이 시작되었습니다");
			logger.debug("ContentLength : " + request.getContentLength() + " bytes");
			logger.debug(Params.getParamsFromHeader(req).toString());
			logger.debug(Params.getParamsFromCookie(req).toString());
			logger.debug(Params.getParams(req).toString());
		}
		filterChain.doFilter(request, response);
		if (logger.isDebugEnabled()) {
			logger.debug("☆☆☆ " + getIpAddr(req) + " 로 부터 \"" + req.getMethod() + " " + req.getRequestURI() + "\" 요청이 종료되었습니다 | duration : " + (System.nanoTime() - currTime) + " ns\n");
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
}