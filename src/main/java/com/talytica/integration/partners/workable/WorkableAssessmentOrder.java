package com.talytica.integration.partners.workable;

import org.json.JSONObject;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class WorkableAssessmentOrder {

	private String test_id; // assessment asid
	private String job_id; // their position id
	private String job_shortcode; // our position id
	private String job_title; // position name
	private String callback_url; // position name
	private OrderCandidate candidate;

	@Data
	@ToString
	private class OrderCandidate {

		private String first_name;
		private String last_name;
		private String resume_url;
		private String phone;
		private String email;
		
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		JSONObject applicant = new JSONObject();
		applicant.put("applicant_ats_id", getCallback_url());		
		applicant.put("email",getCandidate().getEmail());
		applicant.put("fname",getCandidate().getFirst_name());
		applicant.put("lname",getCandidate().getLast_name());
		// skip applicant.put address

		JSONObject assessment = new JSONObject();
		assessment.put("assessment_asid",Long.valueOf(getTest_id()));

//		JSONObject position = new JSONObject();
//		position.put("position_id",Long.valueOf(getJob_shortcode()));

		JSONObject delivery = new JSONObject();
		delivery.put("email_applicant",Boolean.TRUE);
		delivery.put("scores_post_url",getCallback_url());
		json.put("delivery",delivery);
		
		json.put("assessment", assessment);
		json.put("position", assessment);
		json.put("applicant", applicant);
		json.put("delivery",delivery);
		// skip json put location - no location comes across
		
		return json;
	}
}
