package com.talytica.integration.partners.smartrecruiters;

import java.util.Date;

import org.json.JSONObject;

import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Location;
import com.employmeo.data.model.Position;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SmartRecruitersAssessmentOrder {

	private String id; // order ID - resp ats id?
	private String status;
	private Date createDate;
	private Date lasUpdateDate;
	private OrderRequestor requestor;
	private OrderCandidate candidate;
	private OrderJob job;
	private OrderCompany company;
	private SmartRecruitersOffer offer;

	@Data
	@ToString
	private class OrderRequestor {
		String firstName;
		String lastName;
		String email;
		String phone;
	}
	
	@Data
	@ToString
	private class OrderCandidate {
		private String id;
		private String firstName;
		private String lastName;
		private String email;
		private String phone;
		private String addressLine;		
	}
	
	@Data
	@ToString
	private class OrderJob {
		String id;
		String name;
		JobLabel industry;
		JobLabel function;
		JobLabel experienceLevel;
		OrderLocation location;

		@Data
		private class JobLabel {
			String id;
			String label;
		}


	}

	@Data
	@ToString
	private class OrderLocation {
		String country;
		String region;
		String city;
		
		public String getId() {
			return city + "-" + region + "-" + country;
		}
	}
	
	@Data
	@ToString
	private class OrderCompany {
		String id; // account ats id
		String name; // account name
	}
	
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		JSONObject applicant = new JSONObject();
		applicant.put("applicant_ats_id", getCandidate().getId());		
		applicant.put("email",getCandidate().getEmail());
		applicant.put("fname",getCandidate().getFirstName());
		applicant.put("lname",getCandidate().getLastName());
		applicant.put("phone",getCandidate().getLastName());
		JSONObject address = new JSONObject();
		address.put("street", getCandidate().getAddressLine());
		applicant.put("address", address);
		
		JSONObject assessment = new JSONObject();
		assessment.put("assessment_asid",Long.valueOf(getOffer().getCatalogId()));

		JSONObject position = new JSONObject();
		position.put("position_name",getJob().getName());
		position.put("position_ats_id",getJob().getId());
		JSONObject location = new JSONObject();
		location.put("city",getJob().getLocation().getCity());
		location.put("state",getJob().getLocation().getRegion());
		location.put("location_ats_id",getJob().getLocation().getId());
		
		json.put("assessment", assessment);
		json.put("applicant", applicant);
		json.put("position", position);
		json.put("location", location);
		
		return json;
	}
	
}
