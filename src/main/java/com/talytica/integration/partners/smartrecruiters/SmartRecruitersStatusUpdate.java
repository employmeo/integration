package com.talytica.integration.partners.smartrecruiters;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SmartRecruitersStatusUpdate {

	private String message; // assessment asid
	private String assessmentURL;
	private String messageToCandidate;
	private UpdateAuthor author = new UpdateAuthor();

	
	@Data
	@ToString
	private class UpdateAuthor {
		String firstName = "Talytica";
		String lastName = "Administrator";
		String email = "info@talytica.com";
		String phone = "424-1234-5678";
	}
		
}
