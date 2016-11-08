package com.employmeo.testing;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.objects.Person;
import com.employmeo.objects.PositionProfile;
import com.employmeo.objects.Respondant;
import com.employmeo.util.EmailUtility;


public class BasicTestingTool {

	// Basic Tool Settings
	public static String SERVER_NAME = "https://employmeo-dev.herokuapp.com";
	private static int THREAD_COUNT = 25;
	private static String ATS_USER = "test";
	private static String ATS_PASS = "password";
	private static int LOOPS = 12;
	private static int DELAY = 5;
	public static JSONObject account = new JSONObject().put("account_ats_id", "Demo");

	public static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();
	public static final Logger logger = Logger.getLogger("com.employmeo.testing");
	public static final Random rand = new Random();

	public static NewCookie adminCookie = null;
	public static JSONArray assessments = null;
	public static JSONArray positions = null;
	public static JSONArray locations = null;
	public static int completedthreads = 0;
		
	public static void main (String[] args) throws Exception {

		//Long appId = new Long(3457);
		//String link = "https://employmeo.herokuapp.com/take_assessment.html?&respondant_uuid=650868fa-deb7-4243-8bf5-7893adb3cf0a";
		//takeSurvey(SERVER_NAME, appId, link);

		runLoops();
	}
	
	public static void runLoops() throws Exception {
	
		// Test out logging into the admin server, updating the dash, etc.
		logger.info("Starting up with " + THREAD_COUNT + " threads and " + LOOPS + " loops.");
		Form form = new Form();
		form.param("email", "sri@demo.com");
		form.param("password", "password");
		form.param("rememberme", "true");
		String service = SERVER_NAME + "/admin/login";
		postFormToService(getClient(), form, service);
		logger.info("Completed admin login attempt.");

		// Get Locations, etc for account...
		Client integrationClient = getIntegrationClient(ATS_USER, ATS_PASS);
		locations = arrayFromService(integrationClient, new JSONObject().put("account",account), SERVER_NAME + "/integration/getlocations");
		positions = arrayFromService(integrationClient, new JSONObject().put("account",account), SERVER_NAME + "/integration/getpositions");
		assessments = arrayFromService(integrationClient, new JSONObject().put("account",account), SERVER_NAME + "/integration/getassessments");
		
		// Launch multiple invite + complete + score + hire streams using ATS integrations
		for (int i=0;i<THREAD_COUNT;i++) {
			int threadnum = i;
			TASK_EXECUTOR.submit(new Runnable() {
				@Override
				public void run() {
					long waittime = DELAY + (long)(10000.0 * rand.nextDouble());
					for (int j=0;j<LOOPS;j++) {
					Long appId = null;
					String link = null;
						try {
							Thread.sleep(waittime);
							JSONObject result = postJsonToService(integrationClient, randomAtsOrder(), SERVER_NAME + "/integration/atsorder");
							appId = result.getJSONObject("applicant").getLong("applicant_id");
							link = result.getJSONObject("delivery").getString("assessment_url");
							logger.info("Thread (" + threadnum + ") Received Assessment (" + appId + ") Link: " + link);
							Thread.sleep(waittime);
							takeSurvey(SERVER_NAME, appId, link);
							logger.info("Thread (" + threadnum + ") Completed Assessment: " + appId);
							Thread.sleep(100*DELAY);
							if (Math.random()<.95) {
								hireDecision(SERVER_NAME, integrationClient, result.getJSONObject("applicant"));
								logger.info("Thread (" + threadnum + ") Hire Decision for Applicant: " + appId);
							} else {
								logger.info("Thread (" + threadnum + ") No decision made: " + appId);							
							}
						} catch (Exception e) {
							logger.severe("Error! Thread (" + threadnum + ") processing Applicant (" + appId + "): " + e.getMessage());
						}
						logger.info("Thread (" + threadnum + ") completed " + (j+1) + " of " + LOOPS + " loops.");
					}
					threadComplete(threadnum);
				}});
		}
		
	}
	
	public static void threadComplete(int threadnum) {
		completedthreads++;
		logger.info("Thread (" + threadnum + ") closed, " + (THREAD_COUNT - completedthreads) + " threads remaining.");
	}
	
	public static void takeSurvey(String serverName, Long appId, String link) throws Exception {
		Client client = getClient();
		Form form = new Form();
		int idx = link.indexOf("=");
		String uuid = link.substring(idx + 1);
		form.param("respondant_uuid", uuid);
		
		JSONObject collection = postFormToService(client,form, serverName + "/survey/getsurvey");
		JSONObject survey = collection.getJSONObject("survey");
		JSONArray questions = survey.getJSONArray("questions");

		for (int j=0;j<questions.length();j++) {
			long waittime = DELAY + (long)(DELAY * rand.nextDouble());
			Thread.sleep(waittime);
			Form response = new Form();
			response.param("response_respondant_id", appId.toString());
			response.param("response_question_id", Long.toString(questions.getJSONObject(j).getLong("question_id")));
			response.param("response_value", Integer.toString(RandomizerUtil.randomResponse(questions.getJSONObject(j))));
			postFormToService(client,response, serverName + "/survey/response");
		}
		form = new Form();
		form.param("respondant_id", appId.toString());
		postFormToService(client,form, serverName + "/survey/submitassessment");

	}

	public static void hireDecision(String serverName, Client client, JSONObject applicant) {

		JSONObject getScores = new JSONObject();
		getScores.put("account", account);
		getScores.put("applicant", applicant);		
		JSONObject score = postJsonToService(client, getScores, serverName + "/integration/getscore");
		String profile = score.getJSONObject("applicant").getString("applicant_profile");

		double prob = 0.05;		
		switch (profile) {
			case PositionProfile.PROFILE_A:
				prob = 0.95;
				break;
			case PositionProfile.PROFILE_B:
				prob = 0.70;
				break;
			case PositionProfile.PROFILE_C:
				prob = 0.45;
				break;
			case PositionProfile.PROFILE_D:
				prob = 0.05;
				break;
			default:
				break;
		}
		
		JSONObject hireNotice = new JSONObject();
		hireNotice.put("account", account);
		Date date = new Date();
		String changedate = new SimpleDateFormat("yyyy-MM-dd").format(date);
		applicant.put("applicant_change_date",changedate);	
		if (prob > rand.nextDouble()) {
			applicant.put("applicant_status", "hired");
		} else {
			applicant.put("applicant_status", "notoffered");
		}
		hireNotice.put("applicant", applicant);
		postJsonToService(client,hireNotice, serverName + "/integration/hirenotice");
	}
	
	// Utilities for calling web services
	public static synchronized JSONObject postJsonToService(Client client, JSONObject message, String service) {

		WebTarget target = client.target(service);
		String result = target.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(message.toString(), MediaType.APPLICATION_JSON), String.class);
		
		return new JSONObject(result);

	}

	public static synchronized Response postJsonForResponse(Client client, JSONObject message, String service) {

		WebTarget target = client.target(service);
		return target.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(message.toString(), MediaType.APPLICATION_JSON));
	}

	public static synchronized JSONArray arrayFromService(Client client, JSONObject message, String service) {

		WebTarget target = client.target(service);
		String result = target.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(message.toString(), MediaType.APPLICATION_JSON), String.class);
		
		return new JSONArray(result);

	}
	
	public static synchronized JSONObject postFormToService(Client client, Form form, String service) {

		WebTarget target = client.target(service);
		String result = target.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);
		
		return new JSONObject(result);

	}
	
	public static synchronized JSONObject randomAtsOrder() {
		JSONObject atsorder = new JSONObject();
		JSONObject delivery = new JSONObject();
		delivery.put("email_applicant", false);
		delivery.put("redirect_url", "https://google.com");
		if (Math.random() > 0.99) {
			delivery.put("scores_email_notify", true);
			delivery.put("scores_email_address", "sridharkaza@gmail.com");
		} else {
			delivery.put("scores_post_url", "https://employmeo.herokuapp.com/integration/echo");
		}
		JSONObject applicant = new JSONObject();
		JSONObject assessment = RandomizerUtil.randomJson(assessments);
		
		applicant.put("applicant_ats_id", Long.toString(System.currentTimeMillis()));
		applicant.put("fname", RandomizerUtil.randomFname());
		applicant.put("lname", RandomizerUtil.randomLname());
		applicant.put("email", RandomizerUtil.randomEmail(applicant.getString("fname"),applicant.getString("lname")));
		JSONObject address = new JSONObject();

		RandomizerUtil.randomAddress(address);
		applicant.put("address", address);
		JSONObject fw = null;
		atsorder.put("account",account);
		atsorder.put("applicant", applicant);
		atsorder.put("assessment", assessment);
		atsorder.put("application", fw);
		atsorder.put("position", RandomizerUtil.randomJson(positions));
		atsorder.put("location", RandomizerUtil.randomJson(locations));
		atsorder.put("delivery",delivery);
		return atsorder;
	}

	public static Client getClient() throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[]{new X509TrustManager() {
	        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
	        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
	        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
	    }}, new java.security.SecureRandom());

		return ClientBuilder.newBuilder().sslContext(sslContext).build();
	}

	public static Client getIntegrationClient(String atsUser, String atsPass) {
		Client client = null;
		ClientConfig cc = new ClientConfig();
		cc.property(ClientProperties.FOLLOW_REDIRECTS, false);
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[]{new X509TrustManager() {
		        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
		        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
		        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
		    }}, new java.security.SecureRandom());

			client = ClientBuilder.newBuilder().sslContext(sslContext).withConfig(cc).build();
		} catch (Exception e) {
			client = ClientBuilder.newBuilder().withConfig(cc).build();			
		}

		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(atsUser, atsPass);
		client.register(feature);
		return client;
	}


}
