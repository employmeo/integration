package com.talytica.integration.objects;

import java.util.List;

import lombok.Data;

@Data
public class JazzApplicantPollConfiguration {
	private String apiKey;
	private List<String> workFlowIds;
	private String lookbackBeginDate;
	private String lookbackEndDate;	
	private Boolean sendEmail;	
}
