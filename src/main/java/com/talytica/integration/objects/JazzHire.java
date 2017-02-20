package com.talytica.integration.objects;

import java.util.Date;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class JazzHire {
	
	private String id; // hire id
	private String applicant_id;
	private String job_id;
	private String workflow_step_id;
	private String workflow_step_name;
	private String prospect_phone;
	private Date hired_date; // formatted as "2016-09-19", which jackson can map directly  
	private String hired_time;
	
}
