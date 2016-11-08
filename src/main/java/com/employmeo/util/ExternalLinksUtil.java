package com.employmeo.util;

import java.net.URL;
import org.json.JSONObject;
import com.employmeo.objects.Respondant;
import com.employmeo.objects.User;

public class ExternalLinksUtil {
	
	public static String BASE_SURVEY_URL = System.getenv("BASE_SURVEY_URL");
	public static String BASE_PORTAL_URL = System.getenv("BASE_PORTAL_URL");
	public static String BASE_SERVICE_URL = System.getenv("BASE_SERVICE_URL");

	/****
	 * Section to generate external links (uses environment variables)
	 */

	public static String getAssessmentLink(Respondant respondant) {
		String link = null;
		try {
			link = new URL(
					BASE_SURVEY_URL + "/take_assessment.html" + "?&respondant_uuid=" + respondant.getRespondantUuid())
							.toString();
		} catch (Exception e) {
			link = BASE_SURVEY_URL + "/take_assessment.html" + "?&respondant_uuid=" + respondant.getRespondantUuid();
		}
		return link.toString();
	}

	public static String getPortalLink(Respondant respondant) {
		String link = null;
		try {
			link = new URL(
					BASE_PORTAL_URL + "/respondant_score.jsp" + "?&respondant_uuid=" + respondant.getRespondantUuid())
							.toString();
		} catch (Exception e) {
			link = BASE_PORTAL_URL + "/respondant_score.jsp" + "?&respondant_uuid=" + respondant.getRespondantUuid();
		}
		return link.toString();
	}

	public static String getRenderLink(JSONObject applicant) {
		String link = null;
		try {
			link = new URL(BASE_PORTAL_URL + "/render.html" + "?&scores=" + applicant.getJSONArray("scores").toString())
					.toString();
		} catch (Exception e) {
			link = BASE_PORTAL_URL + "/render.html" + "?&scores=" + applicant.getJSONArray("scores").toString();
		}
		return link.toString();
	}

	public static String getForgotPasswordLink(User user) {
		String link = null;
		try {
			link = new URL(BASE_PORTAL_URL + "/reset_password.jsp?&user=" + user.getUserEmail()
							+ "&hash=" + user.getUserPassword()).toString();
		} catch (Exception e) {
			link = BASE_PORTAL_URL + "/reset_password.jsp?&user=" + user.getUserEmail()
					+ "&hash=" + user.getUserPassword();		}
		return link.toString();
	}

}
