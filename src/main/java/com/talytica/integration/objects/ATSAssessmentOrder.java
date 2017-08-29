package com.talytica.integration.objects;

import com.talytica.integration.partners.jazz.JazzJobApplicant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper=true)
public class ATSAssessmentOrder extends JazzJobApplicant {

	private ATSOrderAccount account;
	private Boolean email = Boolean.FALSE;
	
	public ATSAssessmentOrder(@NonNull JazzJobApplicant applicant, @NonNull String accountAtsId) {
		setId(applicant.getId());
		setFirst_name(applicant.getFirst_name());
		setLast_name(applicant.getLast_name());
		setJob_id(applicant.getJob_id());
		setJob_title(applicant.getJob_title());
		setProspect_phone(applicant.getProspect_phone());
		setApply_date(applicant.getApply_date());
		setAccount(new ATSOrderAccount(accountAtsId));
	}
	
	@Data
	@AllArgsConstructor
	class ATSOrderAccount {
		private String account_ats_id;
	}
}
