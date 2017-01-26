package com.talytica.integration.objects;

import java.util.Date;

import lombok.Data;

@Data
public class JazzJobApplicant {

	private String id;
	private String applicant_id;
	private String job_id;
	private String workflow_step_id;
	private Date date; // formatted as "2016-09-19", which jackson can map directly
	
}
