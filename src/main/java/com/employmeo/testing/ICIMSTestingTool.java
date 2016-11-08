package com.employmeo.testing;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class ICIMSTestingTool extends BasicTestingTool {

	// Basic Tool Settings
	public static final String SERVER_NAME = "https://localhost";
	private static String ATS_USER = "icims";
	private static String ATS_PASS = "FGx4bgfZ!C";

	public static int DELAY = 100;
	public static JSONObject account = new JSONObject().put("account_ats_id", "6269");

	public static NewCookie adminCookie = null;
	public static JSONArray assessments = null;
	public static JSONArray positions = null;
	public static JSONArray locations = null;
	public static int completedthreads = 0;

	public static void main (String[] args) throws Exception {

		Client integrationClient = getIntegrationClient(ATS_USER, ATS_PASS);
		// Get Locations, etc for account...
		locations = arrayFromService(integrationClient, 
				new JSONObject().put("account",account), 
				SERVER_NAME + "/integration/getlocations");
		positions = arrayFromService(integrationClient,
				new JSONObject().put("account",account),
				SERVER_NAME + "/integration/getpositions");
		assessments = arrayFromService(integrationClient,
				new JSONObject().put("account",account),
				SERVER_NAME + "/integration/getassessments");

		Response response;
		String[] jobs = {"1382","1384","1385"};
		String[] people = {"1239", "1240", "1243"};

  		for (String job : jobs ) {
			for (String person : people) {
				response = postJsonForResponse(integrationClient,
						createAppComplete(person, job, person+job),
						SERVER_NAME + "/integration/icimsapplicationcomplete");
				System.out.println("Response Status: " + response.getStatus());
				System.out.println("Response Status Phrase: " + response.getStatusInfo().getReasonPhrase());
				System.out.println("Response URI: " + response.getLocation());
				System.out.println("Response Media Type: " + response.getMediaType());
			}
		}		

	}
		
	private static JSONObject createAppComplete(String userId, String jobId, String workflowId) {
		JSONObject json = new JSONObject();
		json.put("systemId",workflowId);
		json.put("userId", userId);
		json.put("customerId","6269");
		json.put("returnUrl","https://jobs-assessmentsandbox.icims.com/jobs/1385/front-line-job-3/assessment?i=1");
		json.put("eventType","ApplicationCompletedEvent");
		JSONObject wflink = new JSONObject();
		wflink.put("rel", "applicantWorkflow");
		wflink.put("title", "Applicant Workflow");
		wflink.put("url", "https://api.icims.com/customers/6269/applicantworkflows/"+workflowId);
		JSONObject jlink = new JSONObject();
		jlink.put("rel", "job");
		jlink.put("title", "Job Profile");
		jlink.put("url", "https://api.icims.com/customers/6269/jobs/"+jobId);
		JSONObject plink = new JSONObject();
		plink.put("rel", "person");
		plink.put("title", "Person Profile");
		plink.put("url", "https://api.icims.com/customers/6269/people/"+userId);
		JSONObject ulink = new JSONObject();
		ulink.put("rel", "user");
		ulink.put("title", "Posting User");
		ulink.put("url", "https://api.icims.com/customers/6269/people/"+userId);
		json.accumulate("links", wflink);
		json.accumulate("links", jlink);
		json.accumulate("links", plink);
		json.accumulate("links", ulink);
		return json;
	} 
	
}
