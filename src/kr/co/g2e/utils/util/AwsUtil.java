package kr.co.g2e.utils.util;

import java.nio.ByteBuffer;
import java.util.List;

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

	public static String textract(String accessKeyId, String secretAccessKey, String endpointUrl, String region, ByteBuffer imageData) {
		EndpointConfiguration awsEndpoint = new EndpointConfiguration(endpointUrl, region);
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
		AmazonTextract client = AmazonTextractClientBuilder
			.standard()
			.withEndpointConfiguration(awsEndpoint)
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
			.build();
		DetectDocumentTextRequest request = new DetectDocumentTextRequest()
			.withDocument(new Document().withBytes(imageData));
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
	}
}
