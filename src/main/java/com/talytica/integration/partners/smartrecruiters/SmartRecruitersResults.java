package com.talytica.integration.partners.smartrecruiters;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SmartRecruitersResults {

	private String title; // assessment asid
	private String description;
	private boolean passed;
	private String resultType;
	private String result;
	private ResultAuthor author;
	
	@Data
	@ToString
	private class ResultAuthor {
		String firstName = "Talytica";
		String lastName = "Administrator";
		String email = "info@talytica.com";
		String phone = "424-1234-5678";
	}
		
}
