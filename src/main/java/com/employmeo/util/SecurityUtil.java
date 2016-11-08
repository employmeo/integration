package com.employmeo.util;

import java.security.MessageDigest;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.employmeo.objects.User;

public class SecurityUtil {

	public static final long AUTH_FAILED = -1;
	private static String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
	private static Matcher matcher;
	private static String PASSWORDLETTERS = "0123456789abcdefghijklmnopABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static Random rnd = new Random();

	private SecurityUtil() {
	}

	public static boolean isEmailValid(String email) {
		matcher = emailPattern.matcher(email);
		return matcher.matches();
	}

	public static String randomPass(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(PASSWORDLETTERS.charAt(rnd.nextInt(PASSWORDLETTERS.length())));
		return sb.toString();
	}

	public static String hashPassword(String password) {
		StringBuffer sb = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(password.getBytes());
			byte byteData[] = md.digest();

			// convert the byte to hex format method 1
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return sb.toString();
	}

	public static User login(String email, String password) {
		String hashword = SecurityUtil.hashPassword(password);
		return loginHashword(email, hashword);
	}

	public static User loginHashword(String email, String hashword) {

		EntityManager em = DBUtil.getEntityManager();
		TypedQuery<User> q = em.createQuery(
				"SELECT u FROM User u WHERE u.userEmail = :email AND u.userPassword = :password", User.class);
		q.setParameter("email", email);
		q.setParameter("password", hashword);
		User user = null;
		try {
			user = q.getSingleResult();
			user.refreshMe();
		} catch (NoResultException nre) {
			user = new User();
			user.setUserEmail(email);
		}
		return user;
	}

}
