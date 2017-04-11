package com.talytica.integration.partners;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.CustomProfile;
import com.employmeo.data.model.Location;
import com.employmeo.data.model.Person;
import com.employmeo.data.model.Position;
import com.employmeo.data.model.Prediction;
import com.employmeo.data.model.PredictionTarget;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantNVP;
import com.employmeo.data.model.RespondantScore;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class JazzPartnerUtil extends BasePartnerUtil {

	@Value("${partners.jazz.api}")
	private String JAZZ_SERVICE;

	private static final SimpleDateFormat JAZZ_SDF = new SimpleDateFormat("yyyy-MM-dd");

	public JazzPartnerUtil() {
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
	public Respondant getRespondantFrom(JSONObject applicant, Account account) {
		Respondant respondant = null;
		respondant = respondantService.getRespondantByAtsId(addPrefix(applicant.get("id").toString()));
		if ((null == respondant) && (applicant.has("applicant_id")) && (applicant.has("job_id"))) {
			Person person = personService.getPersonByAtsId(addPrefix(applicant.getString("applicant_id")));
			Position position = accountService.getPositionByAtsId(account.getId(),addPrefix(applicant.getString("job_id")));
			if ((null != person) & (null != position)) {
				respondant = respondantService.getRespondantByPersonAndPosition(person,position);
			}
		}
		
		return respondant;
	}

	@Override
	public Respondant createRespondantFrom(JSONObject json, Account account) {
		String jobId = json.getString("job_id");
		String appId = json.getString("id");
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
			savedRespondant = getRespondantFrom(application, account);
		}

		if (savedRespondant == null) {
			Respondant respondant = new Respondant();
			respondant.setAccount(account);
			respondant.setAccountId(account.getId());
			respondant.setAtsId(addPrefix(application.getString("id")));
			String phoneNum = savedPerson.getPhone();
			if (null == phoneNum || phoneNum.isEmpty()) {
				log.warn("JazzApp {} no phone {}", respondant.getAtsId(), savedPerson);
			} else {
				phoneNum = phoneNum.replaceAll("[^\\d]", "");
				phoneNum = phoneNum.startsWith("1") ? phoneNum.substring(1) : phoneNum;
				respondant.setPayrollId(StringUtils.left(phoneNum,10)); //shorten to 10 digits.
				log.debug("JazzApp {} using phone as lookupid {}", respondant.getAtsId(), phoneNum);
			}
			respondant.setPerson(savedPerson);
			respondant.setPersonId(savedPerson.getId());
			respondant.setPositionId(position.getId());
			respondant.setPosition(position);
			respondant.setLocationId(location.getId());
			respondant.setLocation(location);
			respondant.setAccountSurveyId(survey.getId());
			respondant.setAccountSurvey(survey);
			respondant.setPartner(getPartner());
			respondant.setPartnerId(getPartner().getId());
			respondant.setScorePostMethod(JAZZ_SERVICE+"notes");
			respondant.setRespondantStatus(Respondant.STATUS_CREATED);
			try {
				String applyDate = json.getString("apply_date");
				respondant.setCreatedDate(JAZZ_SDF.parse(applyDate));
			} catch (Exception e) {
				log.error("Could not parse date {}", json.opt("apply_date"));
			}
			savedRespondant = respondantService.save(respondant);

			@SuppressWarnings("unchecked")
			Set<String> keys = candidate.keySet();
			List<RespondantNVP> nvps = new ArrayList<RespondantNVP>();
			HashMap<String,Integer> questionSet = new HashMap<String,Integer>();

			for (String key : keys) {
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
				case "comments_count":
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
				case "questionnaire":
					JSONArray array = candidate.getJSONArray(key);
					for (int j = 0; j < array.length(); j++) {
						RespondantNVP nvp = new RespondantNVP();
						String question = array.getJSONObject(j).getString("question");
						String value = array.getJSONObject(j).getString("answer");
						if (null == value || value.isEmpty()) break;
						Integer count = questionSet.get(question); 
						if (count != null) {
							count++;
							questionSet.put(question, count);
							question = question + " (" + count.toString() + ")";
						} else {
							count = Integer.valueOf(1);
							questionSet.put(question, count);
						}		
						nvp.setRespondantId(savedRespondant.getId());
						nvp.setName(question);
						nvp.setValue(value);
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
						if ("NEW".equalsIgnoreCase(nvp.getValue())) savedRespondant.setRespondantStatus(Respondant.STATUS_PRESCREEN); 
					}
					break;
				case "desired_salary":
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
					if (null == value || value.isEmpty()) break;
					nvp.setValue(value);
					nvps.add(nvp);
					break;
				}
			}

			respondantService.save(nvps);

			if (json.has("email") && json.getBoolean("email")) {
				emailService.sendEmailInvitation(savedRespondant);
				savedRespondant.setRespondantStatus(Respondant.STATUS_INVITED);
			} 
			
			if(respondant.getRespondantStatus() != Respondant.STATUS_CREATED) respondantService.save(savedRespondant);
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

	
	@Override
	public JSONObject getScoresMessage(Respondant respondant) {
		JSONObject message = new JSONObject();
		message.put("apikey", trimPrefix(respondant.getAccount().getAtsId()));
		message.put("applicant_id", trimPrefix(respondant.getPerson().getAtsId()));
		message.put("user_id", "usr_anonymous");
		message.put("security", "0");
		
		StringBuffer notes = new StringBuffer();
		CustomProfile customProfile = respondant.getAccount().getCustomProfile();
		notes.append("Assessment Result: ");
		notes.append(customProfile.getName(respondant.getProfileRecommendation()));
		notes.append(" (");
		notes.append(respondant.getCompositeScore());
		notes.append(")\n");
		for (RespondantScore rs : respondant.getRespondantScores()) {
			notes.append(corefactorService.findCorefactorById(rs.getId().getCorefactorId()).getName());
			notes.append(": ");
			notes.append(String.format("%.2f", rs.getValue()));
			notes.append("\n");	
		}
		message.put("contents", notes.toString());

		return message;
	}	
	
	@Override
	public JSONObject getScreeningMessage(Respondant respondant) {
		JSONObject message = new JSONObject();
		message.put("apikey", trimPrefix(respondant.getAccount().getAtsId()));
		message.put("applicant_id", trimPrefix(respondant.getPerson().getAtsId()));
		message.put("user_id", "usr_anonymous");
		message.put("security", "0");
		CustomProfile customProfile = respondant.getAccount().getCustomProfile();
		StringBuffer notes = new StringBuffer();
		notes.append("Initial Screen Result: ");
		notes.append(customProfile.getName(respondant.getProfileRecommendation()));
		notes.append(" (");
		notes.append(respondant.getCompositeScore());
		notes.append(")\n");
		for (Prediction prediction : respondant.getPredictions()) {			
			PredictionTarget target = predictionModelService.getTargetById(prediction.getTargetId());
			notes.append(target.getLabel());
			notes.append(": ");
			notes.append(String.format("%.2f", prediction.getPredictionScore()));
			notes.append("\n");	
		}
		message.put("contents", notes.toString());

		return message;
	}	
	
	// Special calls to Jazz HR to get data for applicant

	public String jazzGet(String getTarget, Account account) {
		String apiKey = trimPrefix(account.getAtsId());
		return jazzGet(getTarget, apiKey, null);
	}

	public String jazzGet(String getTarget, String apiKey, Map<String, String> params) {
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
	
	public Date getDateFrom(String date) {
		Date returnDate = new Date();
		try {
			returnDate = JAZZ_SDF.parse(date);
		} catch (Exception e) {
			log.warn("failed to convert Jazz Date {}", date);
		}
		return returnDate;
	}
}
