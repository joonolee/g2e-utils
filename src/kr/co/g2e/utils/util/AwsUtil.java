package kr.co.g2e.utils.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.util.IOUtils;

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
	 * S3(Simple Storage Service)에 파일 업로드
	 * @param region 지역
	 * @param accessKey 액세스키
	 * @param secretAccessKey 액세스 시크릿키
	 * @param bucketName 버킷명
	 * @param fileKey 파일키(UUID)
	 * @param file 파일
	 */
	public static void s3PutObject(String region, String accessKey, String secretAccessKey, String bucketName, String fileKey, File file) {
		try {
			BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey);
			AmazonS3 s3Client = AmazonS3ClientBuilder
				.standard()
				.withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
				.build();
			PutObjectRequest request = new PutObjectRequest(bucketName, fileKey, file);
			request.setCannedAcl(CannedAccessControlList.PublicRead); // 파일은 공개적으로 읽을 수 있도록 권한 설정 
			s3Client.putObject(request);
		} catch (SdkClientException e) {
			e.printStackTrace();
		}
	}

	/**
	 * SES(Simple Email Service)로 이메일 전송
	 * @param region 지역
	 * @param accessKey 액세스키
	 * @param secretAccessKey 액세스 시크릿키
	 * @param fromAddress 보내는사람 이메일주소
	 * @param subject 제목
	 * @param htmlBody html 본문
	 * @param textBody text 본문
	 * @param toAddresses 받는사람 이메일주소들
	 */
	public static void ses(String region, String accessKey, String secretAccessKey, String fromAddress, String subject, String htmlBody, String textBody, String... toAddresses) {
		try {
			BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey);
			AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder
				.standard()
				.withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
				.build();
			SendEmailRequest request = new SendEmailRequest()
				.withSource(fromAddress)
				.withMessage(new Message()
					.withBody(new Body()
						.withHtml(new Content()
							.withCharset("UTF-8").withData(htmlBody))
						.withText(new Content()
							.withCharset("UTF-8").withData(textBody)))
					.withSubject(new Content()
						.withCharset("UTF-8").withData(textBody)))
				.withDestination(
					new Destination().withToAddresses(toAddresses));
			client.sendEmail(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * OCR 판독
	 * @param region 지역
	 * @param accessKey 액세스키
	 * @param secretAccessKey 액세스 시크릿키
	 * @param endpointUrl API 엔드포인트 주소
	 * @param imageFile 이미지 파일
	 * @return 판독 결과 문자열
	 */
	public static String textract(String region, String accessKey, String secretAccessKey, String endpointUrl, File imageFile) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(imageFile);
			ByteBuffer imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
			BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey);
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