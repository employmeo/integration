package com.talytica.integration.partners.greenhouse;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GreenhouseAssessmentOrder {

	private String partner_test_id; // assessment asid
	private OrderCandidate candidate;

	@Data
	@ToString
	private class OrderCandidate {

		private String first_name;
		private String last_name;
		private String resume_url;
		private String phone_number;
		private String email;
		private String greenhouse_profile_url; // use this as ats_id
		
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		JSONObject applicant = new JSONObject();
		try {
			applicant.put("applicant_ats_id", getCandidate().getGreenhouse_profile_url());		
			applicant.put("email",getCandidate().getEmail());
			applicant.put("fname",getCandidate().getFirst_name());
			applicant.put("lname",getCandidate().getLast_name());
			// skip applicant.put address
	
			JSONObject assessment = new JSONObject();
			assessment.put("assessment_asid",Long.valueOf(getPartner_test_id()));
	
			json.put("assessment", assessment);
			json.put("applicant", applicant);
			// skip json put location, position
		} catch (JSONException e) {
		}
		return json;
	}
}
