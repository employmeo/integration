package com.talytica.integration.partners.fountain;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FountainWebHook {
	private FountainApplicant applicant;
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		JSONObject applicant = new JSONObject();
		try {
			applicant.put("applicant_ats_id", getApplicant().getId());		
			applicant.put("email",getApplicant().getEmail());
			applicant.put("fname",getApplicant().getFirst_name());
			applicant.put("lname",getApplicant().getLast_name());
			// skip applicant.put address
	
			// need to figure out how to get the assessment from applicant - funnel?
			//JSONObject assessment = new JSONObject();	
			//json.put("assessment", assessment);
			
			json.put("applicant", applicant);
			// skip json put location, position
		} catch (JSONException e) {
		}
		return json;
	}
}
