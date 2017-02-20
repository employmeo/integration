package com.talytica.integration.objects;

import java.util.List;

import com.employmeo.data.model.Account;

import lombok.Data;

@Data
public class JazzApplicantPollConfiguration {
	private Account account;
	private List<String> workFlowIds;
	private String lookbackBeginDate;
	private String lookbackEndDate;	
	private Boolean sendEmail;
	private String status;
}
