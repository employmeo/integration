package com.talytica.integration.partners.smartrecruiters;

import java.util.Date;

import org.json.JSONObject;

import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Location;
import com.employmeo.data.model.Position;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class SmartRecruitersAssessmentOrder {

	private String id; // order ID - resp ats id?
	private String status;
	private Date createDate;
	private Date lastUpdateDate;
	public OrderRequestor requestor;
	public OrderCandidate candidate;
	public OrderJob job;
	public OrderCompany company;
	public SmartRecruitersOffer offer;

	@Data
	@ToString
	@NoArgsConstructor
	public static class OrderRequestor {
		String firstName;
		String lastName;
		String email;
		String phone;
	}
	
	@Data
	@ToString
	@NoArgsConstructor
	public static class OrderCandidate {
		private String id;
		private String firstName;
		private String lastName;
		private String email;
		private String phone;
		private String addressLine;		
	}
	
	@Data
	@ToString
	@NoArgsConstructor
	public static  class OrderJob {
		String id;
		String name;
		JobLabel industry;
		JobLabel function;
		JobLabel experienceLevel;
		OrderLocation location;

		@Data
		@NoArgsConstructor
		public static class JobLabel {
			String id;
			String label;
		}


	}

	@Data
	@ToString
	@NoArgsConstructor
	public static class OrderLocation {
		String country;
		String region;
		String city;
		
		public String getId() {
			return city + "-" + region + "-" + country;
		}
	}
	
	@Data
	@ToString
	@NoArgsConstructor
	public static class OrderCompany {
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
