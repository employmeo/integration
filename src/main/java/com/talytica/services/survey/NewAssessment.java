package com.talytica.services.survey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.employmeo.objects.AccountSurvey;
import com.employmeo.objects.Person;
import com.employmeo.objects.Respondant;

@Path("order")
public class NewAssessment {
	private static final Logger log = LoggerFactory.getLogger(NewAssessment.class);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String doPost(@FormParam("email") String to, @FormParam("fname") String fname,
			@FormParam("lname") String lname, @FormParam("address") String address, @FormParam("lat") Double personLat,
			@FormParam("lng") Double personLong, @FormParam("asid") Long asid,
			@FormParam("location_id") Long locationId, @FormParam("position_id") Long positionId) {

		// Validate input fields
		AccountSurvey as = AccountSurvey.getAccountSurveyByASID(asid);
		// Perform business logic
		Person applicant = new Person();
		applicant.setPersonEmail(to);
		applicant.setPersonFname(fname);
		applicant.setPersonLname(lname);
		applicant.setPersonAddress(address);
		applicant.setPersonLat(personLat);
		applicant.setPersonLong(personLong);
		applicant.persistMe();

		Respondant respondant = new Respondant();
		respondant.setPerson(applicant);
		respondant.setRespondantAccountId(as.getAsAccountId());
		respondant.setRespondantAsid(asid);
		respondant.setRespondantLocationId(locationId);// ok for null location
		respondant.setRespondantPositionId(positionId);// ok for null location
		respondant.persistMe();

		JSONObject json = new JSONObject();
		json.put("person", applicant.getJSON());
		json.put("respondant", respondant.getJSON());
		json.put("survey", as.getJSON());

		log.debug(json.toString());
		return json.toString();
	}

}