package com.talytica.services.portal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.employmeo.objects.Account;
import com.employmeo.objects.Person;
import com.employmeo.objects.Respondant;
import com.employmeo.objects.User;
import com.employmeo.util.EmailUtility;

import io.swagger.annotations.Api;

@Path("inviteapplicant")
@Api( value="/inviteapplicant", consumes=MediaType.APPLICATION_FORM_URLENCODED, produces=MediaType.APPLICATION_JSON)
public class InviteApplicant {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String doPost(
			@FormParam("account_id") Long accountId,
			@FormParam("email") String to, @FormParam("fname") String fname, @FormParam("lname") String lname,
			@FormParam("address") String address, @FormParam("lat") Double personLat,
			@FormParam("lng") Double personLng, @FormParam("asid") Long asid,
			@FormParam("position_id") Long positionId, @FormParam("location_id") Long locationId,
			@FormParam("notifyemail") String notifyemail,
			@DefaultValue("false") @FormParam("notifyme") boolean notifyMe) {
		// Collect expected input fields

		Account account = Account.getAccountById(accountId);

		// Perform business logic
		Person applicant = new Person();
		applicant.setPersonEmail(to);
		applicant.setPersonFname(fname);
		applicant.setPersonLname(lname);
		applicant.setPersonAddress(address);
		applicant.setPersonLat(personLat);
		applicant.setPersonLong(personLng);
		applicant.persistMe();

		Respondant respondant = new Respondant();
		respondant.setPerson(applicant);
		respondant.setRespondantAccountId(account.getAccountId());
		respondant.setRespondantAsid(asid);
		respondant.setRespondantLocationId(locationId);// ok for null location
		respondant.setRespondantPositionId(positionId);// ok for null location
		if (notifyMe) respondant.setRespondantEmailRecipient(notifyemail);
		respondant.persistMe();
		respondant.refreshMe(); // gets the remaining auto-gen-fields
		
		JSONObject json = new JSONObject();
		json.put("person", applicant.getJSON());
		json.put("respondant", respondant.getJSON());

		EmailUtility.sendEmailInvitation(respondant);

		return json.toString();

	}

}