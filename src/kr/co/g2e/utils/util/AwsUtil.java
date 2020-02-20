package kr.co.g2e.utils.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;

/**
 * Amazon Web Service(AWS) 기능을 이용할 수 있는 유틸리티 클래스
 */
public class AwsUtil {
	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private AwsUtil() {
	}

	/**
	 * OCR 판독
	 * @param accessKeyId 액세스키 아이디
	 * @param secretAccessKey 액세스 시크릿키
	 * @param endpointUrl API 엔드포인트 주소
	 * @param region 지역
	 * @param imageData 이미지 데이터
	 * @return 판독 결과 문자열
	 */
	public static String textract(String accessKeyId, String secretAccessKey, String endpointUrl, String region, File soundFile) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(soundFile);
			ByteBuffer imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
			BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
			EndpointConfiguration awsEndpoint = new EndpointConfiguration(endpointUrl, region);
			AmazonTextract client = AmazonTextractClientBuilder
				.standard()
				.withEndpointConfiguration(awsEndpoint)
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
				.build();
			DetectDocumentTextRequest request = new DetectDocumentTextRequest()
				.withDocument(new Document().withBytes(imageBytes));
			DetectDocumentTextResult result = client.detectDocumentText(request);
			List<Block> blocks = result.getBlocks();
			StringBuilder buffer = new StringBuilder();
			for (Block block : blocks) {
				if ((block.getBlockType()).equals("LINE")) {
					buffer.append(block.getText());
					buffer.append(" ");
				}
			}
			return buffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return "";
	}
}