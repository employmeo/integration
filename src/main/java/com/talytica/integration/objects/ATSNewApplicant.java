package com.talytica.integration.objects;

import com.employmeo.data.model.Person;
import com.employmeo.data.model.Respondant;

public class ATSNewApplicant {
	
	public String atsId;
	public String personId;
	public String firstName;
	public String lastName;
	public String email;	
	public ATSAddress address;
	public ATSLocation location;
	public ATSPosition position;
	public ATSAssessment assessment;
	
	public Boolean emailInvitation;
	
	public String redirectUrl;
	public String emailRecipient;
	public String scorePostMethod;
	
	public Person startPerson() {
		Person person = new Person();
		person.setFirstName(firstName);
		person.setLastName(lastName);
		person.setLastName(lastName);
		person.setEmail(email);
		return person;
	}

	public Respondant startRespondant() {
		Respondant respondant = new Respondant();
		respondant.setRedirectUrl(redirectUrl);
		if (this.emailRecipient != null) respondant.setEmailRecipient(emailRecipient);
		respondant.setScorePostMethod(scorePostMethod);
		return respondant;
	}

	
}
