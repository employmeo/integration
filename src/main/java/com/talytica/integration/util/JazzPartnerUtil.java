package com.talytica.integration.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Location;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Person;
import com.employmeo.data.model.Position;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantNVP;
import com.employmeo.data.repository.RespondantNVPRepository;
import com.employmeo.data.service.PartnerService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.talytica.integration.objects.ATSAssessmentOrder;
import com.talytica.integration.objects.JazzJobApplicant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class JazzPartnerUtil extends BasePartnerUtil {

	@Autowired
	RespondantNVPRepository respondantNVPRepository;

	@Autowired
	PartnerService partnerService;

	@Autowired
	IntegrationClientFactory integrationClientFactory;

	@Value("https://api.resumatorapi.com/v1/")
	private String JAZZ_SERVICE;

	@Getter
	private Partner partner;

	private static final String PARTNER_NAME = "Jazz";
	// private static final String[] workFlowStatusIds = {"2827415","2886659","2827450","2878899","2878947"};
	private static final String[] workFlowStatusIds = { "2827415" };

	private static final SimpleDateFormat JAZZ_SDF = new SimpleDateFormat("yyyy-MM-dd");

	public JazzPartnerUtil() {
	}

	@PostConstruct
	public void initializePartner() {
		Set<Partner> allPartners = partnerService.getAllPartners();
		partner = allPartners.stream().filter(partner -> PARTNER_NAME.equalsIgnoreCase(partner.getPartnerName()))
				.findFirst().get();
		log.debug("JazzPartner initialized: {}", partner);
	}

	@Override
	public Position getPositionFrom(JSONObject position, Account account) {

		String jobId = position.getString("job_id");
		Position savedPosition = accountService.getPositionByAtsId(account.getId(), addPrefix(jobId));

		if (savedPosition == null) {
			Position pos = new Position();
			pos.setAccount(account);
			pos.setAccountId(account.getId());
			String jobservice = "jobs/" + jobId;
			JSONObject response = new JSONObject(jazzGet(jobservice, account));
			pos.setPositionName(response.getString("title"));
			pos.setAtsId(addPrefix(jobId));
			String desc = response.getString("description");
			pos.setDescription(desc.substring(0, Math.min(desc.length(), 1083)));
			savedPosition = accountService.save(pos);
		}
		return savedPosition;
	}

	@Override
	public AccountSurvey getSurveyFrom(JSONObject assessment, Account account) {
		return accountSurveyService.getAccountSurveyById(account.getDefaultAsId());
	}

	@Override
	public Location getLocationFrom(JSONObject json, Account account) {
		return accountService.getLocationById(account.getDefaultLocationId());
	}

	@Override
	public Respondant getRespondantFrom(JSONObject applicant) {
		return respondantService.getRespondantByAtsId(addPrefix(applicant.getString("id")));
	}

	@Override
	public Respondant createRespondantFrom(JSONObject json, Account account) {
		String jobId = json.getString("job_id");
		String appId = (json.getString("id"));
		String appservice = "applicants/" + appId;
		String appjobservice = "applicants2jobs/applicant_id/" + appId + "/job_id/" + jobId;
		JSONObject candidate = new JSONObject(jazzGet(appservice, account));
		JSONObject application = new JSONObject(jazzGet(appjobservice, account));

		Position position = getPositionFrom(json, account);
		Location location = getLocationFrom(null, account);
		AccountSurvey survey = getSurveyFrom(null, account);

		Person savedPerson = personService.getPersonByAtsId(addPrefix(json.getString("id")));
		Respondant savedRespondant = null;
		if (savedPerson == null) {
			log.debug("no person found with id {}", addPrefix(json.getString("id")));
			Person person = new Person();
			person.setAtsId(addPrefix(json.getString("id")));
			person.setFirstName(json.optString("first_name"));
			person.setLastName(json.optString("last_name"));
			person.setPhone(json.optString("prospect_phone"));
			person.setEmail(candidate.optString("email"));
			person.setAddress(candidate.optString("location"));
			savedPerson = personService.save(person);
		} else {
			savedRespondant = getRespondantFrom(application);
		}

		if (savedRespondant == null) {
			Respondant respondant = new Respondant();
			respondant.setAccount(account);
			respondant.setAccountId(account.getId());
			respondant.setAtsId(addPrefix(application.getString("id")));
			respondant.setPerson(savedPerson);
			respondant.setPersonId(savedPerson.getId());
			respondant.setPositionId(position.getId());
			respondant.setLocationId(location.getId());
			respondant.setAccountSurveyId(survey.getId());
			respondant.setPartner(getPartner());
			respondant.setPartnerId(getPartner().getId());
			try {
				String applyDate = json.getString("apply_date");
				respondant.setCreatedDate(JAZZ_SDF.parse(applyDate));
			} catch (Exception e) {
				log.error("Could not parse date {}", json.opt("apply_date"));
			}
			savedRespondant = respondantService.save(respondant);

			Iterator<String> keys = candidate.keys();
			List<RespondantNVP> nvps = new ArrayList<RespondantNVP>();

			while (keys.hasNext()) {
				String key = keys.next();
				switch (key) {
				case "id":
				case "first_name":
				case "last_name":
				case "location":
				case "email":
				case "address":
				case "apply_date":
				case "phone":
					break; // Do Nothing... already saved on person/respondant
				case "messages":
				case "activities":
				case "evaluation":
				case "rating":
				case "categories":
				case "comments":
				case "comment_count":
				case "feedback":
					break; // Do nothing... these are arrays of data that don't
							// come from candidate.
				case "eeo_disability":
				case "eeo_gender":
				case "eeo_race":
				case "eeoc_disability":
				case "eeoc_disability_date":
				case "eeoc_disability_signature":
				case "eeoc_veteran":
				case "has_cdl":
				case "has_driver_license":
				case "has_felony":
				case "felony_explanation":
				case "languages":
				case "over_18":
					break; // Do nothing... these are empty fields, and not
							// allowed for hiring decisions
				case "desired_start_date":
				case "can_work_evenings":
				case "can_work_overtime":
				case "can_work_weekends":
				case "citizenship_status":
				case "college_gpa":
				case "education_level":
				case "twitter_username":
				case "website":
				case "willing_to_relocate":
					break; // Do nothing... these are empty fields,
				case "questionnaire":
					JSONArray array = candidate.getJSONArray(key);
					for (int j = 0; j < array.length(); j++) {
						RespondantNVP nvp = new RespondantNVP();
						nvp.setName(array.getJSONObject(j).getString("question"));
						nvp.setValue(array.getJSONObject(j).getString("answer"));
						nvp.setRespondantId(savedRespondant.getId());
						nvps.add(nvp);
					}
					break;
				case "jobs": // could be one or multiple jobs.
					Object object = candidate.get(key);
					JSONObject job = null;
					if (JSONObject.class == object.getClass()) {
						job = candidate.getJSONObject(key);
					} else if (JSONArray.class == object.getClass()) {
						JSONArray jobs = (JSONArray) object;
						for (int j = 0; j < jobs.length(); j++) {
							if (jobId == jobs.getJSONObject(j).getString("job_id"))
								job = jobs.getJSONObject(j);
						}
					}
					if (job != null) {
						RespondantNVP nvp = new RespondantNVP();
						nvp.setValue(job.getString("applicant_progress"));
						nvp.setRespondantId(savedRespondant.getId());
						nvp.setName("applicant_progress");
						nvps.add(nvp);
					}
					break;
				case "desired_salary":
				default:
					RespondantNVP nvp = new RespondantNVP();
					nvp.setRespondantId(savedRespondant.getId());
					nvp.setName(key);
					String value;
					if (String.class == candidate.get(key).getClass()) {
						value = candidate.getString(key);
					} else {
						value = candidate.get(key).toString();
					}
					nvp.setValue(value.substring(0, Math.min(value.length(), 4096)));
					nvps.add(nvp);
				}
			}

			respondantNVPRepository.save(nvps);

			if (json.has("email")) {
				emailService.sendEmailInvitation(savedRespondant);
			}
		}
		return savedRespondant;
	}

	@Override
	public JSONObject prepOrderResponse(JSONObject json, Respondant respondant) {

		JSONObject jApplicant = new JSONObject();
		jApplicant.put("applicant_ats_id", this.trimPrefix(respondant.getAtsId()));
		jApplicant.put("applicant_id", respondant.getId());
		jApplicant.put("first_name", respondant.getPerson().getFirstName());
		jApplicant.put("last_name", respondant.getPerson().getLastName());
		return jApplicant;
	}

	// Special calls to Jazz HR to get data for applicant

	private String jazzGet(String getTarget, Account account) {
		String apiKey = trimPrefix(account.getAtsId());
		return jazzGet(getTarget, apiKey, null);
	}

	private String jazzGet(String getTarget, String apiKey, Map<String, String> params) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(JAZZ_SERVICE + getTarget).queryParam("apikey", apiKey);

		if (null != params) {
			for (Map.Entry<String, String> param : params.entrySet()) {
				target = target.queryParam(param.getKey(), param.getValue());
			}
		}

		String serviceResponse = null;
		try {
			serviceResponse = target.request(MediaType.APPLICATION_JSON).get(String.class);
			log.trace("Service {} yielded response : {}", getTarget, serviceResponse);
		} catch (Exception e) {
			log.warn("Failed to grab service {}. Exception: {}", getTarget, e);
		}
		return serviceResponse;
	}

	public void orderNewCandidateAssessments(String accountApiKey) {
		List<ATSAssessmentOrder> orders = Lists.newArrayList();
		
		Set<JazzJobApplicant> applicants = fetchApplicants(accountApiKey);
		
		for(JazzJobApplicant applicant: applicants) {
			ATSAssessmentOrder order = new ATSAssessmentOrder(applicant, accountApiKey);
			order.setEmail(Boolean.TRUE);
			orders.add(order);
		}
				
		log.debug("Prepared {} ATS order requests", orders.size());
		orderAssessments(orders);
	}

	private void orderAssessments(List<ATSAssessmentOrder> orders) {
		log.debug("Ordering assessments...");

		Client client = integrationClientFactory.newInstance(getPartner().getLogin(), getPartner().getPassword());
		String serverHost = integrationClientFactory.getServer();
		String orderAssessmentsUriPath = "/integration/atsorder";
		String targetPath = serverHost + orderAssessmentsUriPath;

		int counter = 0;
		for (ATSAssessmentOrder order : orders) {
			String applicantName = "'" + order.getFirst_name() + " " + order.getLast_name() + "'";
			log.info("Placing order #{} for {}", ++counter, applicantName);

			WebTarget target = client.target(targetPath);
			try {
				Response response = target
									.request(new String[] { "application/json" })
									.post(Entity.entity(order, "application/json"));
				Boolean success = (null == response || response.getStatus() >= 300) ? false : true;
				String serviceResponse = response.readEntity(String.class); 
				if (success) {
					log.debug("Posted ATS order to integration server for applicant {}", applicantName);
				} else {
					log.warn("Failed to post ATS order successfully for {}. Server response: {}", applicantName, serviceResponse);
				}
			} catch(Exception e) {
				log.warn("Failed to post ATS order to integration server: {}", order, e );
			}
		}
		log.info("All {} ATS order requests complete.", orders.size());

		try {
			client.close();
			log.debug("Integration client closed successfully");
		} catch (Exception e) {
			log.warn("Failed to close integrationClient cleanly. Watch for leaks", e);
		}

	}

	private Set<JazzJobApplicant> fetchApplicants(String accountApiKey) {
		Set<JazzJobApplicant> applicants = Sets.newHashSet();
		for (String status : workFlowStatusIds) {
			log.debug("Fetching applicants for statusId: {}", status);
			
			List<JazzJobApplicant> jobApplicantsByStatus = fetchJobApplicantsByStatus(status, accountApiKey);
			applicants.addAll(jobApplicantsByStatus);
			log.info("Fetched a batch of {} applicants, with total applicants now at {}", jobApplicantsByStatus.size(), applicants.size());
		}
		return applicants;
	}

	public List<JazzJobApplicant> fetchJobApplicantsByStatus(String status, String accountApiKey) {
		List<JazzJobApplicant> applicants = Lists.newArrayList();
		String applicantsServiceEndpoint = "applicants/status/" + status;

		try {
			String applicantsServiceResponse = jazzGet(applicantsServiceEndpoint, accountApiKey, null);

			if (null != applicantsServiceResponse) {
				applicants = parseApplicants(applicantsServiceResponse);
				log.debug("Retrieved jazzed applicants: {}", applicants.size());
			}

		} catch (Exception e) {
			log.warn("Failed to retrieve/process applicants from Jazzed API service", e);
		}

		return applicants;
	}

	List<JazzJobApplicant> parseApplicants(String applicantsServiceResponse)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper = mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		List<JazzJobApplicant> partnerApplicants = mapper.readValue(applicantsServiceResponse,
				new TypeReference<List<JazzJobApplicant>>() {
				});

		log.trace("Parsed Jazz applicants: {}", partnerApplicants);
		return partnerApplicants;
	}
}
