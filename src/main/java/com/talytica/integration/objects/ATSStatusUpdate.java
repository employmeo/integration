package com.talytica.integration.objects;

import java.util.Date;

import com.talytica.integration.partners.jazz.JazzHire;
import com.talytica.integration.partners.jazz.JazzJobApplicant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode
public class ATSStatusUpdate {
	
	public ATSApplicant applicant;
	private ATSAccount account;
	
	public ATSStatusUpdate(@NonNull JazzJobApplicant applicant, @NonNull String accountAtsId, String status, Date date) {
		setApplicant(new ATSApplicant(null,applicant.getId(),applicant.getJob_id(),status,date));
		setAccount(new ATSAccount(accountAtsId));
	}
	
	public ATSStatusUpdate(@NonNull JazzHire hire, @NonNull String accountAtsId) {
		setApplicant(new ATSApplicant(null, hire.getApplicant_id(),hire.getJob_id(),"hired", hire.getHired_date()));
		setAccount(new ATSAccount(accountAtsId));
	}
	
	@Data
	@AllArgsConstructor
	class ATSAccount {
		private String account_ats_id;
	}
	
	@Data
	@AllArgsConstructor
	public class ATSApplicant {
		private String id;
		private String applicant_id;
		private String job_id;
		private String applicant_status;
		private Date applicant_change_date;
	}
	
}
