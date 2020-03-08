package com.talytica.integration.partners;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import com.employmeo.data.model.CustomProfile;
import com.employmeo.data.model.Respondant;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@Scope("prototype")
public class SalesforcePartnerUtil extends BasePartnerUtil {


	private static final long TOKEN_MILLIS = 7200*1000;

	@Value("${partners.salesforce.oauth}")
	String SFDC_OATH;
		
	@Value("${partners.salesforce.clientid}")
	String CLIENT_ID;

	@Value("${partners.salesforce.clientsecret}")
	String CLIENT_SECRET;

		private static String authToken = null;
		private static Date tokenExpiration = new Date();
		
		private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		
		// These are the hard coded names of salesforce fields
		private static String ASSESSMENT_DATE_FIELD = "Assessment_Date__c";
		private static String ASSESSMENT_NOTES_FIELD = "Assessment_Notes__c";
		private static String ASSESSMENT_RESULT_FIELD = "Assessment_Result__c";
		private static String ASSESSMENT_SCORE_FIELD = "Assessment_Score__c";
		private static String ASSESSMENT_STATUS_FIELD = "Assessment_Status__c";
		
		@Override
		public void postScoresToPartner(Respondant resp, JSONObject patch) {
			patchSFObject(resp.getScorePostMethod(), patch);
		}

		@Override
		public JSONObject getScoresMessage(Respondant resp){
			String notes = getScoreNotesFormat(resp);
			CustomProfile customProfile = resp.getAccount().getCustomProfile();
			String status = null;
			JSONObject patch = new JSONObject();
			
			switch (resp.getRespondantStatus()) {
				case Respondant.STATUS_INVITED:
				case Respondant.STATUS_STARTED:
				case Respondant.STATUS_REMINDED:
					status = "Reference Request Sent";
					break;
				case Respondant.STATUS_SCORED:
				case Respondant.STATUS_COMPLETED:
					status = "References Provided";
					break;
				case Respondant.STATUS_PREDICTED:
					status = "References Complete";
					break;
				case Respondant.STATUS_INSUFFICIENT_GRADERS:
				case Respondant.STATUS_UNGRADED:
				default:
					status = "References Not Yet Provided";
					break;
			}

			try {
				patch.put(ASSESSMENT_DATE_FIELD, SDF.format(resp.getFinishTime()));
				patch.put(ASSESSMENT_NOTES_FIELD, notes);
				patch.put(ASSESSMENT_RESULT_FIELD, customProfile.getName(resp.getProfileRecommendation()));
				patch.put(ASSESSMENT_SCORE_FIELD, resp.getCompositeScore());
				patch.put(ASSESSMENT_STATUS_FIELD, status);
			} catch (JSONException je){
				log.error("Failed due to json exception: {}",je.getMessage());
			}
			return patch;
		}
		
		public void patchSFObject(String endpoint, JSONObject patch) {
			Client client = ClientBuilder.newClient().property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
			WebTarget target = client.target(endpoint);
			
			try {
				Response response = target.request(MediaType.APPLICATION_JSON)
									.header("Authorization", "Bearer " + getOAuthToken())
									.method("PATCH", Entity.entity(patch.toString(), MediaType.APPLICATION_JSON));
				if (response.getStatus() >= 300) {
					String error = response.readEntity(String.class);
					log.error("Unable to patch object. Error {} occurred: {}", response.getStatus(), error);			
				} else {
					log.debug("Successfully posted message to {}, with payload: {}", endpoint, patch);
				}
				
			} catch (Exception e){
				log.error("Unable to patch object. Error occurred: {}", e.getMessage());
			} finally {
				client.close();
			}
		}
		
		public String getOAuthToken() throws Exception {

			if ((authToken == null) || (tokenExpiration.before(new Date()))) {

				Client client = ClientBuilder.newClient();
				JSONObject response = new JSONObject();
				String responseString = null;
				WebTarget target = client.target(SFDC_OATH);
				Form form = new Form();
				form.param("grant_type", "password");
				form.param("client_id", CLIENT_ID);
				form.param("client_secret", CLIENT_SECRET);
				form.param("username", getPartner().getApiLogin());
				form.param("password", getPartner().getApiPass());
				log.debug("Auth Request is: {}, {}, {}",CLIENT_ID,CLIENT_SECRET,getPartner());
				try {
					Response resp = target.request(MediaType.APPLICATION_JSON)
											.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
					responseString =  resp.readEntity(String.class);
					log.debug("Auth Request Response was: {}", responseString);
					response = new JSONObject(responseString);
					authToken = response.getString("access_token");
					//sfdcAPI = response.getString("instance_url");
					tokenExpiration = new Date(new Date().getTime() + TOKEN_MILLIS);
				} catch (Exception e) {
					log.error("SFDC Authentication Error: {}", responseString);
					throw new Exception(e);
				} finally {
					client.close();
				}
			}
			return authToken;
		}
			
}
