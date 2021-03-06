package kr.co.g2e.utils.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

/**
 * HTML, JavaScript, CSS Minify filter
 */
public class MinifyFilter implements Filter {
	private HtmlCompressor compressor;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		MyResponseWrapper resWrapper = null;
		try {
			resWrapper = new MyResponseWrapper((HttpServletResponse) response);
			filterChain.doFilter(request, resWrapper);
			String contentType = nullToBlankString(resWrapper.getContentType());
			if (isTextualContentType(contentType)) {
				minifying(response, resWrapper, contentType);
			} else {
				resWrapper.writeTo(response.getOutputStream());
			}
		} finally {
			if (resWrapper != null) {
				resWrapper.close();
				resWrapper = null;
			}
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		compressor = new HtmlCompressor();
		compressor.setCompressCss(true);
		compressor.setCompressJavaScript(true);
	}

	@Override
	public void destroy() {
	}

	private void minifying(ServletResponse response, MyResponseWrapper resWrapper, String contentType) throws IOException {
		PrintWriter writer = response.getWriter();
		String content = resWrapper.toString();
		if (isCompressibleContentType(contentType)) {
			writer.write(compressor.compress(content));
		} else {
			writer.write(content);
		}
		writer.flush();
	}

	private boolean isTextualContentType(String contentType) {
		return "".equals(contentType) || contentType.contains("text") || contentType.contains("json") || contentType.contains("xml");
	}

	private boolean isCompressibleContentType(String contentType) {
		return contentType.contains("html") || contentType.contains("xml") || contentType.contains("javascript") || contentType.contains("css");
	}

	private static String nullToBlankString(String str) {
		String rval = "";
		if (str == null) {
			rval = "";
		} else {
			rval = str;
		}
		return rval;
	}

	class MyResponseWrapper extends HttpServletResponseWrapper {
		private ByteArrayOutputStream bytes;
		private PrintWriter writer;

		public MyResponseWrapper(HttpServletResponse res) {
			super(res);
			bytes = new ByteArrayOutputStream(8 * 1024);
			writer = new PrintWriter(bytes);
		}

		@Override
		public PrintWriter getWriter() {
			return writer;
		}

		@Override
		public ServletOutputStream getOutputStream() {
			return new MyOutputStream(bytes);
		}

		@Override
		public String toString() {
			writer.flush();
			return bytes.toString();
		}

		public void writeTo(OutputStream os) throws IOException {
			bytes.writeTo(os);
		}

		public void close() throws IOException {
			bytes.close();
			writer.close();
			bytes = null;
			writer = null;
		}
	}

	class MyOutputStream extends ServletOutputStream {
		private ByteArrayOutputStream bytes;

		public MyOutputStream(ByteArrayOutputStream bytes) {
			this.bytes = bytes;
		}

		@Override
		public void write(int c) throws IOException {
			bytes.write(c);
		}

		@Override
		public void write(byte[] b) throws IOException {
			bytes.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			bytes.write(b, off, len);
		}

		@Override
		public void close() throws IOException {
			bytes.close();
			super.close();
			bytes = null;
		}
	}
}