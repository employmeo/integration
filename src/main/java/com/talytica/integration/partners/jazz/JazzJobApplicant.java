package com.talytica.integration.partners.jazz;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class JazzJobApplicant {

	private String id; // applicantId
	private String first_name;
	private String last_name;
	private String job_id;
	private String job_title;
	private String prospect_phone;
	private String apply_date; // formatted as "2016-09-19", which jackson can map directly // changed back - SK 
	
}
