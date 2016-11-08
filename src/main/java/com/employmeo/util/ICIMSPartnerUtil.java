package com.employmeo.util;

import java.util.Date;
import java.util.Iterator;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.objects.Account;
import com.employmeo.objects.AccountSurvey;
import com.employmeo.objects.Location;
import com.employmeo.objects.Partner;
import com.employmeo.objects.Person;
import com.employmeo.objects.Position;
import com.employmeo.objects.PositionProfile;
import com.employmeo.objects.Respondant;


public class ICIMSPartnerUtil implements PartnerUtil {
	private static final Logger log = LoggerFactory.getLogger(ICIMSPartnerUtil.class);
	private static final String ICIMS_USER = "employmeoapiuser";
	private static final String ICIMS_PASS = "YN9rEQnU";
	private static final String ICIMS_API = "https://api.icims.com/customers/";
	private static final String JOB_EXTRA_FIELDS = "?fields=jobtitle,assessmenttype,jobtype,joblocation,hiringmanager";
	private static final JSONObject ASSESSMENT_COMPLETE = new JSONObject("{'id':'D37002019001'}");
	private static final JSONObject ASSESSMENT_INCOMPLETE = new JSONObject("{'id':'D37002019002'}");
	private static final JSONObject ASSESSMENT_INPROGRESS = new JSONObject("{'id':'D37002019003'}");
	private static final JSONObject ASSESSMENT_SENT = new JSONObject("{'id':'D37002019004'}");
	private static final SimpleDateFormat ICIMS_SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
	private static String PROXY_URL = System.getenv("QUOTAGUARDSTATIC_URL"); 
	private Partner partner = null;
	
	public ICIMSPartnerUtil(Partner partner) {
		this.partner = partner;
	}
		
	public String getPrefix() {
		return partner.getPartnerPrefix();
	}

	@Override
	public String trimPrefix(String id) {
		return id.substring(id.indexOf(getPrefix())+getPrefix().length());
	}
	
	@Override
	public Account getAccountFrom(JSONObject json) {
		if (json.has("account_ats_id")) {
			json.put("customerId", json.getString("account_ats_id"));
		}
		Account account = null;
		// lookup account by ATS ID
		EntityManager em = DBUtil.getEntityManager();
		TypedQuery<Account> q = em.createQuery("SELECT a FROM Account a WHERE a.accountAtsId = :accountAtsId",
				Account.class);
		q.setParameter("accountAtsId", partner.getPartnerPrefix() + json.getString("customerId"));
		try {
			account = q.getSingleResult();
		} catch (NoResultException nre) {
			log.warn("Can't find account with atsId: " + json.getString("customerId"));
			throw new WebApplicationException(
					Response.status(Status.PRECONDITION_FAILED).entity(json.toString()).build());
		}
		return account;
	}
	
	@Override
	public Location getLocationFrom(JSONObject job, Account account) {
		String locationLink  = job.getJSONObject("joblocation").getString("address");
		String locationName = job.getJSONObject("joblocation").getString("value");

		Location location = null;
		EntityManager em = DBUtil.getEntityManager();
		TypedQuery<Location> q = em.createQuery(
				"SELECT l FROM Location l WHERE l.locationAtsId = :locationAtsId AND l.locationAccountId = :accountId",
				Location.class);
		q.setParameter("locationAtsId", locationLink);
		q.setParameter("accountId", account.getAccountId());
		try {
			location = q.getSingleResult();
		} catch (NoResultException nre) {
			try {
				JSONObject address = new JSONObject();
				address.put("street", locationName);
				// TODO create code to find a location in ICIMS, and create in employmeo
				AddressUtil.validate(address);
				location = new Location();
				location.setAccount(account);
				location.setLocationAtsId(locationLink);
				location.setLocationName(locationName);
				location.persistMe();
			} catch (Exception e) {
				log.warn("Failed to lookup or create new location");
			}
		}

		return account.getDefaultLocation();
	}
	
	@Override
	public Position getPositionFrom(JSONObject job, Account account) {
		// TODO - Get job title / type data, and figure out how to map it to Positions
		log.debug("Using Account default position and Ignoring job object: " + job);
		Position pos = account.getDefaultPosition();
		return pos;
	}
	
	@Override
	public AccountSurvey getSurveyFrom(JSONObject job, Account account) {

		JSONArray assessmenttypes = job.getJSONArray("assessmenttype");
		if (assessmenttypes.length() > 1) log.warn("More than 1 Assessment in: " + assessmenttypes);

		String assessmentName = assessmenttypes.getJSONObject(0).getString("value");
		job.put("assessment", assessmenttypes.getJSONObject(0));
		AccountSurvey aSurvey = null;
		List<AccountSurvey> assessments = account.getAccountSurveys();
		for (AccountSurvey as : assessments) {
			if (assessmentName.equals(as.getSurveyName())) aSurvey = as;
		}
		if (aSurvey == null) {
			StringBuffer sb = new StringBuffer();
			for (AccountSurvey as : assessments) sb.append(as.getSurveyName() + " ");
			log.warn("Could Not Match: " + assessmentName + " to any of" + sb.toString());
			aSurvey = account.getDefaultAccountSurvey();
		}
		
		return aSurvey;
	}
	
	@Override
	public Respondant getRespondantFrom(JSONObject json) {
		Respondant respondant = null;
		String workflowLink = ICIMS_API+json.getString("customerId") + 
				"/applicantworkflows/" +json.getString("systemId");
		
		EntityManager em = DBUtil.getEntityManager();
		TypedQuery<Respondant> q = em.createQuery(
				"SELECT r FROM Respondant r WHERE r.respondantAtsId = :link", Respondant.class);
		q.setParameter("link", workflowLink);

		try {
			respondant = q.getSingleResult();
		} catch (NoResultException nre) {
			log.debug("No Respondant Found for: " + workflowLink);
		}		
		return respondant;
	}

	@Override
	public Respondant createRespondantFrom(JSONObject json, Account account) {
		Respondant respondant = getRespondantFrom(json);
		if (respondant != null) return respondant; // Check that its not a duplicate request
		
		String workflowLink = null; // link to application
		JSONObject job = null; // ICIMS job applied to (includes location, etc)
		JSONObject candidate  = null; // This is ICIMS "Person"

		JSONArray links = json.getJSONArray("links");
		for (int i=0;i<links.length();i++) {
			JSONObject link = links.getJSONObject(i);
			switch (link.getString("rel")) {
			case "job":
				job = new JSONObject(icimsGet(link.getString("url")+JOB_EXTRA_FIELDS));
				job.put("link", link.getString("url"));
				break;
			case "person":
				candidate = new JSONObject(icimsGet(link.getString("url")));
				candidate.put("link", link.getString("url"));
				break;
			case "applicantWorkflow":
				workflowLink = link.getString("url");
				break;
			case "user":
				// Dont use this one.
				break;
			default:
				log.warn("Unexpected Link: " + link);
				break;
			}
		}
		
		Person person = getPerson(candidate, account);
		Position position = this.getPositionFrom(job, account);
		Location location = this.getLocationFrom(job, account);
		AccountSurvey aSurvey = this.getSurveyFrom(job, account);

		respondant = new Respondant();
		
		respondant.setRespondantAtsId(workflowLink);
		if (json.has("returnUrl")) respondant.setRespondantRedirectUrl(json.getString("returnUrl"));
		respondant.setRespondantScorePostMethod(workflowLink);
		respondant.setAccount(account);
		respondant.setPosition(position);
		respondant.setPartner(this.partner);
		respondant.setRespondantLocationId(location.getLocationId());
		respondant.setAccountSurvey(aSurvey);
		// TODO - add logic to grab hiring manager info to set up email notify, based on client config
		//respondant.setRespondantEmailRecipient(delivery.optString("scores_email_address"));

		respondant.setPerson(person);
		respondant.persistMe();

		return respondant;
	}

	
	@Override
	public JSONObject prepOrderResponse(JSONObject json, Respondant respondant) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getScoresMessage(Respondant respondant) {
		
		JSONObject scores = respondant.getAssessmentScore();
		PredictionUtil.predictRespondant(respondant);

		Iterator<String> it = scores.keys();
		StringBuffer notes = new StringBuffer();
		notes.append("Factor Scores: ");
		while (it.hasNext()) {
			String label = it.next();
			notes.append("[");
			notes.append(label);
			notes.append("= ");
			notes.append(scores.getDouble(label));
			notes.append("]");
			if (it.hasNext()) notes.append(", ");
		}

		JSONObject assessment = new JSONObject();
		assessment.put("value", respondant.getAccountSurvey().getSurveyName());

		JSONObject results = new JSONObject();
		results.put("assessmentname", assessment);
		results.put("assessmentdate", ICIMS_SDF.format(new Date(respondant.getRespondantFinishTime().getTime())));
		results.put("assessmentscore", respondant.getCompositeScore());
		results.put("assessmentresult", PositionProfile.getProfileDefaults(
				respondant.getRespondantProfile()).getString("profile_name"));
		results.put("assessmentnotes", notes.toString());
		results.put("assessmentstatus", ASSESSMENT_COMPLETE);
		results.put("assessmenturl", ExternalLinksUtil.getPortalLink(respondant));

		JSONArray resultset = new JSONArray();
		resultset.put(results);
		JSONObject json = new JSONObject();
		json.put("assessmentresults", resultset);
		return json;
	}

		
	@Override
	public void postScoresToPartner(Respondant respondant, JSONObject message) {
		String method = respondant.getRespondantAtsId();
		Response response = icimsPatch(method, message);
		log.debug("Posted Scores to ICIMS: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
		if (response.hasEntity()) log.debug("Response Message: " + response.readEntity(String.class));
	}	
	
	public Person getPerson(JSONObject applicant, Account account) {

		Person person = null;
		EntityManager em = DBUtil.getEntityManager();
		TypedQuery<Person> q = em.createQuery(
				"SELECT p FROM Person p WHERE p.personAtsId = :link", Person.class);
		q.setParameter("link", applicant.getString("link"));
		try {
				person = q.getSingleResult();
				return person;
		} catch (NoResultException nre) {
		}
		// If no result, or other error...
		person = new Person();
		person.setPersonAtsId(applicant.getString("link"));
		person.setPersonEmail(applicant.getString("email"));
		person.setPersonFname(applicant.getString("firstname"));
		person.setPersonLname(applicant.getString("lastname"));

		try {
			JSONObject address = applicant.getJSONArray("addresses").getJSONObject(0);
			address.put("street", address.getString("addressstreet1") + " " + address.optString("addressstreet2"));
			address.put("city", address.getString("addresscity"));
			address.put("state", address.getJSONObject("addressstate").getString("abbrev"));
			address.put("zip", address.getString("addresszip"));
			AddressUtil.validate(address);
			person.setPersonAddress(address.optString("formatted_address"));
			person.setPersonLat(address.optDouble("lat"));
			person.setPersonLong(address.optDouble("lng"));
		} catch (Exception e) {
			log.warn("Failed to handle address:" + e.getMessage());
		}
		person.persistMe();
		return person;
	}
	
	// Specific methods for talking to ICIMS

	private WebTarget prepTarget(String target) {
		ClientConfig cc = new ClientConfig();
		cc.property(ApacheClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION, true);
		cc.property(ClientProperties.PROXY_URI, PROXY_URL);
		try {
			URL proxyUrl = new URL(PROXY_URL);
			String userInfo = proxyUrl.getUserInfo();
			String pUser = userInfo.substring(0, userInfo.indexOf(':'));
			String pPass = userInfo.substring(userInfo.indexOf(':') + 1);			
			cc.property(ClientProperties.PROXY_USERNAME, pUser);
			cc.property(ClientProperties.PROXY_PASSWORD, pPass);
		} catch (Exception e) {
			log.warn("Failed to set proxy uname pass: " + PROXY_URL);
		}
		cc.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "BUFFERED");
		cc.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
		cc.connectorProvider(new ApacheConnectorProvider());
		Client client = ClientBuilder.newClient(cc);
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(ICIMS_USER, ICIMS_PASS);
		client.register(feature);
		
		return client.target(target);
	}
	
	private String icimsGet(String getTarget) {
		String response = prepTarget(getTarget).request(MediaType.APPLICATION_JSON).get(String.class);
		return response;
	}

	private Response icimsPost(String postTarget, JSONObject json) {
		Response response = prepTarget(postTarget).request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
		return response;
	}

	private Response icimsPatch(String postTarget, JSONObject json) {	
		Response response = prepTarget(postTarget).request(MediaType.APPLICATION_JSON)
				.method("PATCH",Entity.entity(json.toString(), MediaType.APPLICATION_JSON));	
		return response;
	}
	
}
