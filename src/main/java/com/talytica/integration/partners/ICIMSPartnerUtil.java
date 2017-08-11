package com.talytica.integration.partners;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.*;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.*;
import com.employmeo.data.service.*;
import com.talytica.common.service.ExternalLinksService;

import com.talytica.common.service.AddressService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class ICIMSPartnerUtil implements PartnerUtil {

	@Value("${partners.icims.user}")
	private String ICIMS_USER;
	@Value("${partners.icims.password}")
	private String ICIMS_PASS;
	@Value("${partners.icims.api}")
	private String ICIMS_API;
	@Value("${com.talytica.urls.proxy}")
	private String PROXY_URL;
	@Value("${partners.icims.proxy:false}")
	private boolean USE_PROXY;
	
	private static final String JOB_EXTRA_FIELDS = "?fields=jobtitle,assessmenttype,jobtype,joblocation,hiringmanager";
	public static final String ASSESSMENT_COMPLETE_ID = "{'id':'D37002019001'}";
	public static final String ASSESSMENT_IN_PROGRESS_ID = "{'id':'D37002019003'}";
	public static final String ASSESSMENT_SENT_ID = "{'id':'D37002019004'}";
	
	private static final SimpleDateFormat ICIMS_SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

	@Setter
	@Getter
	private Partner partner = null;

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
	
	@Autowired
	GraderService graderService;

	@Autowired
	ExternalLinksService externalLinksService;
	

	public ICIMSPartnerUtil() {
	}

	@Override
	public String getPrefix() {
		return partner.getPrefix();
	}

	@Override
	public String addPrefix(String id) {
		if (partner.getPrefix() == null) {
			return id;
		}
		return partner.getPrefix() + id;
	}

	@Override
	public String trimPrefix(String id) {
		return id.substring(id.indexOf(getPrefix()) + getPrefix().length());
	}

	@Override
	public Account getAccountFrom(JSONObject json) {
		if (json.has("account_ats_id")) {
			try {
				json.put("customerId", json.optString("account_ats_id"));
			} catch (JSONException jse) {
				// do nothing with stupid exception
			}
		}
		String customerId = partner.getPrefix() + json.optString("customerId");
		// lookup account by ATS ID
		Account account = accountService.getAccountByAtsId(customerId);
		return account;
	}

	@Override
	public Location getLocationFrom(JSONObject job, Account account) {
		String locationLink = job.optJSONObject("joblocation").optString("address");
		String locationName = job.optJSONObject("joblocation").optString("value");

		Location location = locationRepository.findByAccountIdAndAtsId(account.getId(), locationLink);

		if (location != null) {
			return location;
		}

		// create the location on the fly
		JSONObject address = new JSONObject();
		try {
			address.put("street", locationName);
			addressService.validate(address);
		} catch (Exception e) {
			log.error("Failed to vaidate location: {}", address);
		}
		location = new Location();
		location.setAccount(account);
		location.setAtsId(locationLink);
		location.setLocationName(locationName);

		return locationRepository.save(location);
	}

	@Override
	public Position getPositionFrom(JSONObject job, Account account) {
		Position jobPosition = null;

		if (job.has("jobtitle")) {
			String title = job.optString("jobtitle");
			for (Position position : account.getPositions()) {
				if (title.equalsIgnoreCase(position.getPositionName()))
					jobPosition = position;
			}
		}

		if (jobPosition == null) {
			log.debug("Using Account default position instead of {}", job);
			jobPosition = positionRepository.findOne(account.getDefaultPositionId());
		}

		return jobPosition;
	}

	@Override
	public AccountSurvey getSurveyFrom(JSONObject job, Account account) {

		AccountSurvey aSurvey = null;
		String assessmentName = null;
		if (job.has("assessmenttype")) {
			JSONArray assessmenttypes = job.optJSONArray("assessmenttype");
			if (assessmenttypes.length() > 1) {
				log.warn("More than 1 Assessment in: " + assessmenttypes);
			}

			assessmentName = assessmenttypes.optJSONObject(0).optString("value");

			Set<AccountSurvey> assessments = account.getAccountSurveys();
			for (AccountSurvey as : assessments) {
				if (assessmentName.equals(as.getDisplayName())) {
					aSurvey = as;
				}
			}
			if (aSurvey == null) {
				StringBuffer sb = new StringBuffer();
				for (AccountSurvey as : assessments) {
					sb.append(as.getDisplayName() + " ");
				}
				log.warn("Could Not Match: {} to any of {}", assessmentName, assessments);
			}
		} else {
			log.warn("Did not receive assessment type in job: {}", job);
		}

		if (aSurvey == null) {
			aSurvey = accountSurveyService.getAccountSurveyById(account.getDefaultAsId());
			log.warn("Using Account Default Assessment {}", aSurvey.getDisplayName());
		} else {
			log.info("Using {} based on {}", aSurvey.getDisplayName(), assessmentName );
		}

		return aSurvey;
	}

	@Override
	public Respondant getRespondantFrom(JSONObject json, Account account) {
		Respondant respondant = null;
		String workflowLink = ICIMS_API + json.optString("customerId") + "/applicantworkflows/"
				+ json.optString("systemId");

		respondant = respondantService.getRespondantByAtsId(workflowLink);
		log.debug("Workflow link {} resulted with {}", workflowLink, respondant);

		return respondant;
	}

	@Override
	public Respondant createRespondantFrom(JSONObject json, Account account) {
		Respondant respondant = getRespondantFrom(json, account);
		if (respondant != null) {
			return respondant; // Check that its not a duplicate request
		}

		String workflowLink = null; // link to application
		JSONObject job = null; // ICIMS job applied to (includes location, etc)
		JSONObject candidate = null; // This is ICIMS "Person"

		JSONArray links = json.optJSONArray("links");
		try {
			for (int i = 0; i < links.length(); i++) {
				JSONObject link = links.optJSONObject(i);
				switch (link.getString("rel")) {
				case "job":
					job = new JSONObject(icimsGet(link.getString("url") + JOB_EXTRA_FIELDS));
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
		} catch (Exception e) {
			log.error("Failed to get links: {}", e.getMessage());
		}

		Person person = getPerson(candidate, account);
		Position position = this.getPositionFrom(job, account);
		Location location = this.getLocationFrom(job, account);
		AccountSurvey aSurvey = this.getSurveyFrom(job, account);
		Person savedPerson = personService.save(person);

		respondant = new Respondant();
		respondant.setPerson(savedPerson);
		respondant.setPersonId(savedPerson.getId());
		respondant.setAtsId(workflowLink);
		if (json.has("returnUrl")) {
			respondant.setRedirectUrl(json.optString("returnUrl"));
		}
		respondant.setScorePostMethod(workflowLink);
		respondant.setAccount(account);
		respondant.setAccountId(account.getId());
		respondant.setPosition(position);
		respondant.setPositionId(position.getId());
		respondant.setPartner(this.partner);
		respondant.setPartnerId(this.partner.getId());
		respondant.setLocationId(location.getId());
		respondant.setAccountSurvey(aSurvey);
		respondant.setAccountSurveyId(aSurvey.getId());
		// TODO - add logic to grab hiring manager info to set up email notify,
		// based on client config
		// respondant.setRespondantEmailRecipient(delivery.optString("scores_email_address"));

		return respondantService.save(respondant);
	}

	@Override
	public JSONObject prepOrderResponse(JSONObject json, Respondant respondant) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getScoresMessage(Respondant respondant) {
		JSONObject json = new JSONObject();
		CustomProfile customProfile = respondant.getAccount().getCustomProfile();
		List<RespondantScore> scores = new ArrayList<RespondantScore>( respondant.getRespondantScores());
		
		scores.sort(new Comparator<RespondantScore>() {
			public int compare (RespondantScore a, RespondantScore b) {
				Corefactor corefactorA = corefactorRepository.findOne(a.getId().getCorefactorId());
				Corefactor corefactorB = corefactorRepository.findOne(a.getId().getCorefactorId());
				double aCoeff = 1d;
				double bCoeff = 1d;
				if (corefactorA.getDefaultCoefficient() != null) aCoeff = Math.abs(corefactorA.getDefaultCoefficient());
				if (corefactorB.getDefaultCoefficient() != null) bCoeff = Math.abs(corefactorB.getDefaultCoefficient());
				// first sort by coefficient - descending
				if (aCoeff != bCoeff) return (int)(bCoeff - aCoeff);
				// otherwise just sort by name
				return corefactorA.getName().compareTo(corefactorB.getName());
			}
		});
		StringBuffer notes = new StringBuffer();
		notes.append("Summary Scores:\n");
		
		for (RespondantScore score : scores) {
			Corefactor cf = corefactorRepository.findOne(score.getId().getCorefactorId());
			notes.append(cf.getName());
			notes.append(" : ");
			notes.append(score.getValue().intValue());
			notes.append("\n");
		}

		List<Grader> graders = graderService.getGradersByRespondantId(respondant.getId());
		if (!graders.isEmpty()) {
			StringBuffer references = new StringBuffer();
			StringBuffer evaluators = new StringBuffer();
			for (Grader grader : graders) {
				switch (grader.getType()) {
				case Grader.TYPE_PERSON:
					references.append(grader.getPerson().getFirstName());
					references.append(" ");
					references.append(grader.getPerson().getLastName());
					if ((null != grader.getRelationship()) && (!grader.getRelationship().isEmpty()))
					  references.append(" ("+grader.getRelationship()+")");
					references.append(" : ");
					references.append(grader.getSummaryScore());
					references.append("\n");
					break;
				case Grader.TYPE_SUMMARY_USER:
				case Grader.TYPE_USER:
				default:
					evaluators.append(grader.getUser().getFirstName());
					evaluators.append(" ");
					evaluators.append(grader.getUser().getLastName());
					evaluators.append(" : ");
					evaluators.append(grader.getSummaryScore());
					evaluators.append("\n");
					break;
				}
			}
			if (references.length() > 0) {
				notes.append("References:\n");
				notes.append(references);
			};
			if (evaluators.length() > 0) {
				notes.append("Evaluated By:\n");
				notes.append(evaluators);
			};
		}
		
		
		try {
			JSONObject assessment = new JSONObject();
			assessment.put("value", respondant.getAccountSurvey().getDisplayName());

			JSONObject results = new JSONObject();
			results.put("assessmentname", assessment);
			results.put("assessmentdate", ICIMS_SDF.format(new Date(respondant.getFinishTime().getTime())));
			results.put("assessmentscore", respondant.getCompositeScore());
			results.put("assessmentresult", customProfile.getName(respondant.getProfileRecommendation()));
			results.put("assessmentnotes", notes.toString());
			results.put("assessmentstatus", new JSONObject(ASSESSMENT_COMPLETE_ID));
			results.put("assessmenturl", externalLinksService.getPortalLink(respondant));

			JSONArray resultset = new JSONArray();
			resultset.put(results);

			json.put("assessmentresults", resultset);
		} catch (Exception e) {
			log.error("Error building scores message: {}", e.getMessage());
		}
		return json;
	}

	@Override
	public void postScoresToPartner(Respondant respondant, JSONObject message) {
		String method = respondant.getAtsId();
		Response response = icimsPatch(method, message);
		log.debug("Posted Scores to ICIMS: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
		if (response.hasEntity()) {
			log.debug("Response Message: " + response.readEntity(String.class));
		}
	}

	public Person getPerson(JSONObject applicant, Account account) {
		
		Person person = personService.getPersonByAtsId(applicant.optString("link"));
		if (person != null) {
			return person;
		}

		// If no result, or other error...
		person = new Person();
		person.setAtsId(applicant.optString("link"));
		person.setEmail(applicant.optString("email"));
		person.setFirstName(applicant.optString("firstname"));
		person.setLastName(applicant.optString("lastname"));

		JSONArray addresses = applicant.optJSONArray("addresses");
		if (addresses != null) {		
			JSONObject address = addresses.optJSONObject(0);
			if (address == null) ;
			try {
				address.put("street", address.optString("addressstreet1") + " " + address.optString("addressstreet2"));
				address.put("city", address.optString("addresscity"));
				address.put("state", address.optJSONObject("addressstate").optString("abbrev"));
				address.put("zip", address.optString("addresszip"));
				addressService.validate(address);
				person.setAddress(address.optString("formatted_address"));
				person.setLatitude(address.optDouble("lat"));
				person.setLongitude(address.optDouble("lng"));
			} catch (Exception e) {
				log.error("Failed to vaidate address: {}", address);
			}
		}

		return personService.save(person);
	}

	// Specific methods for talking to ICIMS

	private Client getClient() {
		ClientConfig cc = new ClientConfig();
		cc.property(ApacheClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION, true);

		if (USE_PROXY) {
		cc.property(ClientProperties.PROXY_URI, PROXY_URL);
			try {
				URL proxyUrl = new URL(PROXY_URL);
				String userInfo = proxyUrl.getUserInfo();
				String pUser = userInfo.substring(0, userInfo.indexOf(':'));
				String pPass = userInfo.substring(userInfo.indexOf(':') + 1);
				cc.property(ClientProperties.PROXY_USERNAME, pUser);
				cc.property(ClientProperties.PROXY_PASSWORD, pPass);
			} catch (Exception e) {
				log.info("No User & Pass for Proxy: {}", PROXY_URL);
			}
		}		
		cc.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "BUFFERED");
		cc.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
		cc.property("sslProtocol", "TLSv1.2");
		cc.connectorProvider(new ApacheConnectorProvider());
		Client client = ClientBuilder.newClient(cc);

		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(ICIMS_USER, ICIMS_PASS);
		client.register(feature);

		return client;
	}

	private String icimsGet(String getTarget) {
		Client client = getClient();
		Response response = client.target(getTarget).request(MediaType.APPLICATION_JSON).get();

		String result = response.readEntity(String.class);
		if (Response.Status.OK.getStatusCode() != response.getStatus()) {
			log.error("iCIMS GET Error {} responded with: {} {}, {}", getTarget,
					response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase(), result);
		}
		return result;
	}

	// private Response icimsPost(String postTarget, JSONObject json) {
	// Response response =
	// prepTarget(postTarget).request(MediaType.APPLICATION_JSON)
	// .post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
	// return response;
	// }

	private Response icimsPatch(String postTarget, JSONObject json) {
		Response response = getClient().target(postTarget).request(MediaType.APPLICATION_JSON).method("PATCH",
				Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
		return response;
	}

	@Override
	public JSONObject getScreeningMessage(Respondant respondant) {
		return getScoresMessage(respondant);
	}

	@Override
	public void changeCandidateStatus(Respondant respondant, String status) {
		
		try {
			JSONObject assessment = new JSONObject();
			assessment.put("value", respondant.getAccountSurvey().getDisplayName());
			JSONObject results = new JSONObject();
			results.put("assessmentname", assessment);
			results.put("assessmentstatus", new JSONObject(status));
			results.put("assessmenturl", externalLinksService.getPortalLink(respondant));
			JSONArray resultset = new JSONArray();
			resultset.put(results);
			JSONObject json = new JSONObject();
			json.put("assessmentresults", resultset);		
			postScoresToPartner(respondant, json);
		} catch (Exception e) {
			log.error("Failed to change respondant {} status to {}", respondant.getId(), status, e);
		}
	}
	
	@Override
	public void postScoresToPartner(String method, JSONObject message) {
		Response response = icimsPatch(method, message);
		log.debug("Posted Scores to ICIMS: " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
		if (response.hasEntity()) {
			log.debug("Response Message: " + response.readEntity(String.class));
		}	
	}

}
