package kr.co.g2e.utils.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * Velocity를 이용한 템플릿 처리 유틸리티 클래스
 */
public final class VelocityUtil {
	private static final Log logger = LogFactory.getLog(VelocityUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private VelocityUtil() {
	}

	/**
	 * views.properties 파일에 설정된 key와 연결된 템플릿 파일에서 statement에 정의된 COMMAND의 문자열을 파라미터를
	 * 적용한 문자열을 생성한다.
	 * <br>
	 * Sql 문장생성 및 이메일 발송을 위한 템플릿 생성할때 응용할 수 있다.
	 * @param sc 서블릿 컨텍스트 객체
	 * @param filePath 템플릿의 파일경로 문자열
	 * @param statement 문장식별 문자열
	 * @param param 파라미터 객체
	 * @return 템플릿이 적용된 문자열
	 */
	public static String render(ServletContext sc, String filePath, String statement, Object param) {
		return render(sc, filePath, statement, param, "UTF8");
	}

	/**
	 * views.properties 파일에 설정된 key와 연결된 템플릿 파일에서 statement에 정의된 COMMAND의 문자열을 파라미터를
	 * 적용한 문자열을 생성한다.
	 * <br>
	 * Sql 문장생성 및 이메일 발송을 위한 템플릿 생성할때 응용할 수 있다.
	 * @param sc 서블릿 컨텍스트 객체
	 * @param filePath 템플릿의 파일경로 문자열
	 * @param statement 문장식별 문자열
	 * @param param 파라미터 객체
	 * @param fileEncoding 파일인코딩
	 * @return 템플릿이 적용된 문자열
	 */
	public static String render(ServletContext sc, String filePath, String statement, Object param, String fileEncoding) {
		StringWriter writer = new StringWriter();
		try {
			Velocity.init();
			VelocityContext context = new VelocityContext();
			context.put("COMMAND", statement);
			context.put("PARAM", param);
			context.put("UTIL", StringUtil.class);
			String template = readTemplate(sc, filePath, fileEncoding);
			StringReader reader = new StringReader(template);
			Velocity.evaluate(context, writer, "kr.co.g2e.utils.util.VelocityUtil", reader);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

	/**
	 * 템플릿파일을 읽어들인다.
	 */
	private static String readTemplate(ServletContext sc, String filePath, String fileEncoding) {
		return read(sc.getRealPath(filePath), fileEncoding);
	}

	/**
	 * 파일의 path의 파일 내용 읽어서 String으로 리턴한다
	 */
	private static String read(String filePath, String fileEncoding) {
		StringBuilder ta = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), fileEncoding));
			String line;
			while ((line = br.readLine()) != null) {
				ta.append(line + "\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
		return ta.toString();
	}
}