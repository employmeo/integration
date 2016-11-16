package com.talytica.integration.util;

import java.util.Date;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.Location;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Person;
import com.employmeo.data.model.Position;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.repository.CorefactorRepository;
import com.employmeo.data.repository.LocationRepository;
import com.employmeo.data.repository.PositionRepository;
import com.employmeo.data.service.AccountService;
import com.employmeo.data.service.AccountSurveyService;

import com.employmeo.data.service.PartnerService;
import com.employmeo.data.service.PersonService;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.EmailService;
import com.talytica.integration.objects.PositionProfile;
import com.talytica.integration.service.AddressService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;

public class ICIMSPartnerUtil implements PartnerUtil {
	private static final Logger log = LoggerFactory.getLogger(ICIMSPartnerUtil.class);
	private static final String ICIMS_USER = "employmeoapiuser";
	private static final String ICIMS_PASS = "YN9rEQnU";
	private static final String ICIMS_API = "https://api.icims.com/customers/";
	private static final String JOB_EXTRA_FIELDS = "?fields=jobtitle,assessmenttype,jobtype,joblocation,hiringmanager";
	private static final JSONObject ASSESSMENT_COMPLETE = new JSONObject("{'id':'D37002019001'}");
	//private static final JSONObject ASSESSMENT_INCOMPLETE = new JSONObject("{'id':'D37002019002'}");
	//private static final JSONObject ASSESSMENT_INPROGRESS = new JSONObject("{'id':'D37002019003'}");
	//private static final JSONObject ASSESSMENT_SENT = new JSONObject("{'id':'D37002019004'}");
	private static final SimpleDateFormat ICIMS_SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
	private static String PROXY_URL = System.getenv("QUOTAGUARDSTATIC_URL"); 

	private Partner partner = null;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	AddressService addressService;
	
	@Autowired
	PartnerService partnerService;
	
	@Autowired
	AccountService accountService;
	
	@Autowired
	AccountSurveyService accountSurveyService;
	
	@Autowired
	RespondantService respondantService;
	
	@Autowired
	PersonService personService;
	
	@Autowired
	LocationRepository locationRepository;
	
	@Autowired
	PositionRepository positionRepository;
	
	@Autowired
	CorefactorRepository corefactorRepository;

	public ICIMSPartnerUtil(Partner partner) {
		this.partner = partner;
	}
		
	public String getPrefix() {
		return partner.getPrefix();
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
		String customerId = partner.getPrefix() + json.getString("customerId");
		// lookup account by ATS ID		
		Account account = accountService.getAccountByAtsId(customerId);
		return account;
	}
	
	@Override
	public Location getLocationFrom(JSONObject job, Account account) {
		String locationLink  = job.getJSONObject("joblocation").getString("address");
		String locationName = job.getJSONObject("joblocation").getString("value");

		Location location = locationRepository.findByAccountIdAndAtsId(account.getId(), locationLink);

		if (location != null) return location;

		// create the location on the fly
		JSONObject address = new JSONObject();
		address.put("street", locationName);
		addressService.validate(address);
		location = new Location();
		location.setAccount(account);
		location.setAtsId(locationLink);
		location.setLocationName(locationName);
			
		return locationRepository.save(location);
	}
	
	@Override
	public Position getPositionFrom(JSONObject job, Account account) {
		// TODO - Get job title / type data, and figure out how to map it to Positions
		log.debug("Using Account default position and Ignoring job object: " + job);

		return positionRepository.findOne(account.getDefaultPositionId());
	}
	
	@Override
	public AccountSurvey getSurveyFrom(JSONObject job, Account account) {

		JSONArray assessmenttypes = job.getJSONArray("assessmenttype");
		if (assessmenttypes.length() > 1) log.warn("More than 1 Assessment in: " + assessmenttypes);

		String assessmentName = assessmenttypes.getJSONObject(0).getString("value");
		job.put("assessment", assessmenttypes.getJSONObject(0));
		AccountSurvey aSurvey = null;
		Set<AccountSurvey> assessments = account.getAccountSurveys();
		for (AccountSurvey as : assessments) {
			if (assessmentName.equals(as.getDisplayName())) aSurvey = as;
		}
		if (aSurvey == null) {
			StringBuffer sb = new StringBuffer();
			for (AccountSurvey as : assessments) sb.append(as.getDisplayName() + " ");
			log.warn("Could Not Match: " + assessmentName + " to any of" + sb.toString());
			aSurvey = accountSurveyService.getAccountSurveyById(account.getDefaultAsId());
		}
		
		return aSurvey;
	}
	
	@Override
	public Respondant getRespondantFrom(JSONObject json) {
		Respondant respondant = null;
		String workflowLink = ICIMS_API+json.getString("customerId") + 
				"/applicantworkflows/" +json.getString("systemId");
		
		respondant = respondantService.getRespondantByAtsId(workflowLink);
		log.debug("Workflow link {} resulted with {}", workflowLink, respondant);
		
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
		Person savedPerson = personService.save(person);

		respondant = new Respondant();
		respondant.setPerson(savedPerson);
		respondant.setAtsId(workflowLink);
		if (json.has("returnUrl")) respondant.setRedirectUrl(json.getString("returnUrl"));
		respondant.setScorePostMethod(workflowLink);
		respondant.setAccount(account);
		respondant.setPosition(position);
		respondant.setPartner(this.partner);
		respondant.setLocationId(location.getId());
		respondant.setAccountSurvey(aSurvey);
		// TODO - add logic to grab hiring manager info to set up email notify, based on client config
		//respondant.setRespondantEmailRecipient(delivery.optString("scores_email_address"));

		return respondantService.save(respondant);
	}

	
	@Override
	public JSONObject prepOrderResponse(JSONObject json, Respondant respondant) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getScoresMessage(Respondant respondant) {
		
		Set<RespondantScore> scores = respondant.getRespondantScores();	
		StringBuffer notes = new StringBuffer();
		notes.append("Factor Scores: ");
		for (RespondantScore score : scores) {
			Corefactor cf = corefactorRepository.findOne(score.getId().getCorefactorId());
			notes.append("[");
			notes.append(cf.getName());
			notes.append(" : ");
			notes.append(score.getValue().intValue());
			notes.append("]");
		}

		JSONObject assessment = new JSONObject();
		assessment.put("value", respondant.getAccountSurvey().getDisplayName());

		JSONObject results = new JSONObject();
		results.put("assessmentname", assessment);
		results.put("assessmentdate", ICIMS_SDF.format(new Date(respondant.getFinishTime().getTime())));
		results.put("assessmentscore", respondant.getCompositeScore());
		results.put("assessmentresult", PositionProfile.getProfileDefaults(
				respondant.getProfileRecommendation()).getString("profile_name"));
		results.put("assessmentnotes", notes.toString());
		results.put("assessmentstatus", ASSESSMENT_COMPLETE);
		results.put("assessmenturl", emailService.getPortalLink(respondant));

		JSONArray resultset = new JSONArray();
		resultset.put(results);
		JSONObject json = new JSONObject();
		json.put("assessmentresults", resultset);
		return json;
	}

		
	@Override
	public void postScoresToPartner(Respondant respondant, JSONObject message) {
		String method = respondant.getAtsId();
		Response response = icimsPatch(method, message);
		log.debug("Posted Scores to ICIMS: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
		if (response.hasEntity()) log.debug("Response Message: " + response.readEntity(String.class));
	}	
	
	public Person getPerson(JSONObject applicant, Account account) {

		Person person = personService.getPersonByAtsId(applicant.getString("link"));
		if (person != null)	return person;

		// If no result, or other error...
		person = new Person();
		person.setAtsId(applicant.getString("link"));
		person.setEmail(applicant.getString("email"));
		person.setFirstName(applicant.getString("firstname"));
		person.setLastName(applicant.getString("lastname"));


		JSONObject address = applicant.getJSONArray("addresses").getJSONObject(0);
		address.put("street", address.getString("addressstreet1") + " " + address.optString("addressstreet2"));
		address.put("city", address.getString("addresscity"));
		address.put("state", address.getJSONObject("addressstate").getString("abbrev"));
		address.put("zip", address.getString("addresszip"));
		addressService.validate(address);
		person.setAddress(address.optString("formatted_address"));
		person.setLatitude(address.optDouble("lat"));
		person.setLongitude(address.optDouble("lng"));

		return personService.save(person);
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

	//private Response icimsPost(String postTarget, JSONObject json) {
	//	Response response = prepTarget(postTarget).request(MediaType.APPLICATION_JSON)
	//			.post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
	//	return response;
	//}

	private Response icimsPatch(String postTarget, JSONObject json) {	
		Response response = prepTarget(postTarget).request(MediaType.APPLICATION_JSON)
				.method("PATCH",Entity.entity(json.toString(), MediaType.APPLICATION_JSON));	
		return response;
	}
	
}
