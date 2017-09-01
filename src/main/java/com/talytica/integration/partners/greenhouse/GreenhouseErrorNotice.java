package com.talytica.integration.partners.greenhouse;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GreenhouseErrorNotice {

	private String api_call;
	private List<String> errors;
	private String partner_test_id;
	private String partner_test_name;
	private String partner_interview_id;
	private String candidate_email;

}
