package com.talytica.integration.util;

import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class JazzPartnerUtil extends BasePartnerUtil {
	
	@Autowired
	RespondantNVPRepository respondantNVPRepository;
		
	@Value("https://api.resumatorapi.com/v1/")
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
			JSONObject response = jazzGet(jobservice, account);
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
		String appjobservice = "applicants2jobs/applicant_id/"+appId+"/job_id/"+jobId;
		JSONObject candidate = jazzGet(appservice, account);
		JSONObject application = jazzGet(appjobservice, account);
		
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
		}
				
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
				break; // Do nothing... these are arrays of data that don't come from candidate.
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
				break; // Do nothing... these are empty fields, and not allowed for hiring decisions
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
				for (int j=0;j<array.length();j++) {
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
					for (int j=0;j<jobs.length();j++) {
						if (jobId == jobs.getJSONObject(j).getString("job_id")) job = jobs.getJSONObject(j);
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
	
	private JSONObject jazzGet(String getTarget, Account account) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(JAZZ_SERVICE + getTarget + "?apikey=" + trimPrefix(account.getAtsId()));
		JSONObject json = null;
		try {
			json = new JSONObject(target.request(MediaType.APPLICATION_JSON).get(String.class));
		} catch (Exception e) {
			log.error("Failed to grab service {}",getTarget);
		}
		return json;
	}
	
}
