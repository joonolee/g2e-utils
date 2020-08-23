package kr.co.g2e.utils.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

/**
 * Firebase 기능을 이용할 수 있는 유틸리티 클래스
 */
public class FirebaseUtil {
	private static final Log logger = LogFactory.getLog(FirebaseUtil.class);

	/**
	 * 생성자, 외부에서 객체를 인스턴스화 할 수 없도록 설정
	 */
	private FirebaseUtil() {
	}

	/**
	 * Noti 객체
	 */
	public static class Noti {
		private String title;
		private String body;
		private String image;

		public Noti(String title, String body, String image) {
			this.title = title;
			this.body = body;
			this.image = image;
		}

		public String getTitle() {
			return title;
		}

		public String getBody() {
			return body;
		}

		public String getImage() {
			return image;
		}
	}

	/**
	 * 토큰 유효성 검사
	 * @param token 인증토큰
	 * @return Uid 문자열
	 */
	public static String verifyIdToken(String token) {
		try {
			return FirebaseAuth.getInstance().verifyIdToken(token).getUid();
		} catch (FirebaseAuthException e) {
			throw new RuntimeException("Google Firebase Token Decode Error");
		}
	}

	/**
	 * 특정 기기에 메시지 전송(노티만)
	 * @param token fcm 토큰
	 * @param noti 노티할 데이터
	 */
	public static void sendMessage(String token, Noti noti) {
		Notification notification = Notification.builder()
			.setTitle(noti.getTitle())
			.setBody(noti.getBody())
			.setImage(noti.getImage())
			.build();
		Message message = Message.builder()
			.setNotification(notification)
			.setToken(token)
			.build();
		try {
			String response = FirebaseMessaging.getInstance().send(message);
			logger.debug(response);
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
			throw new RuntimeException("Google Firebase sendMessage Error");
		}
	}

	/**
	 * 특정 기기에 메시지 전송(데이터만)
	 * @param token fcm 토큰
	 * @param data 전송할 데이터 맵
	 */
	public static void sendMessage(String token, Map<String, String> data) {
		Message message = Message.builder()
			.putAllData(data)
			.setToken(token)
			.build();
		try {
			String response = FirebaseMessaging.getInstance().send(message);
			logger.debug(response);
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
			throw new RuntimeException("Google Firebase sendMessage Error");
		}
	}

	/**
	 * 특정 기기에 메시지 전송(노티 + 데이터)
	 * @param token fcm 토큰
	 * @param noti 노티할 데이터
	 * @param data 전송할 데이터 맵
	 */
	public static void sendMessage(String token, Noti noti, Map<String, String> data) {
		Notification notification = Notification.builder()
			.setTitle(noti.getTitle())
			.setBody(noti.getBody())
			.setImage(noti.getImage())
			.build();
		Message message = Message.builder()
			.setNotification(notification)
			.putAllData(data)
			.setToken(token)
			.build();
		try {
			String response = FirebaseMessaging.getInstance().send(message);
			logger.debug(response);
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
			throw new RuntimeException("Google Firebase sendMessage Error");
		}
	}

	/**
	 * 여러 기기에 메시지 전송(노티만)
	 * @param tokenList fcm 토큰리스트
	 * @param noti 노티할 데이터
	 */
	public static void sendMessage(List<String> tokenList, Noti noti) {
		Notification notification = Notification.builder()
			.setTitle(noti.getTitle())
			.setBody(noti.getBody())
			.setImage(noti.getImage())
			.build();
		MulticastMessage message = MulticastMessage.builder()
			.setNotification(notification)
			.addAllTokens(tokenList)
			.build();
		try {
			BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
			logger.debug(response.toString());
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
			throw new RuntimeException("Google Firebase sendMessage Error");
		}
	}

	/**
	 * 여러 기기에 메시지 전송(데이터만)
	 * @param tokenList fcm 토큰리스트
	 * @param data 전송할 데이터 맵
	 */
	public static void sendMessage(List<String> tokenList, Map<String, String> data) {
		MulticastMessage message = MulticastMessage.builder()
			.putAllData(data)
			.addAllTokens(tokenList)
			.build();
		try {
			BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
			logger.debug(response.toString());
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
			throw new RuntimeException("Google Firebase sendMessage Error");
		}
	}

	/**
	 * 여러 기기에 메시지 전송(노티 + 데이터)
	 * @param tokenList fcm 토큰리스트
	 * @param noti 노티할 데이터
	 * @param data 전송할 데이터 맵
	 */
	public static void sendMessage(List<String> tokenList, Noti noti, Map<String, String> data) {
		Notification notification = Notification.builder()
			.setTitle(noti.getTitle())
			.setBody(noti.getBody())
			.setImage(noti.getImage())
			.build();
		MulticastMessage message = MulticastMessage.builder()
			.setNotification(notification)
			.putAllData(data)
			.addAllTokens(tokenList)
			.build();
		try {
			BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
			logger.debug(response.toString());
		} catch (FirebaseMessagingException e) {
			logger.error("", e);
			throw new RuntimeException("Google Firebase sendMessage Error");
		}
	}
}