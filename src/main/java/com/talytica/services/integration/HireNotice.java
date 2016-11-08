package com.talytica.services.integration;

import java.sql.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONObject;

import com.employmeo.objects.Account;
import com.employmeo.objects.Partner;
import com.employmeo.objects.Respondant;
import com.employmeo.util.PartnerUtil;

@Path("hirenotice")
@PermitAll
public class HireNotice {
	private final static Response MISSING_REQUIRED_PARAMS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Missing Required Parameters' }").build();
	private final static Response UNKNOWN_STATUS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Unknown Applicant Status' }").build();
	private final static Response ACCOUNT_MATCH = Response.status(Response.Status.CONFLICT)
			.entity("{ message: 'Applicant ID not found for Account ID' }").build();
	private static final Logger log = LoggerFactory.getLogger(HireNotice.class);
	@Context
	private SecurityContext sc;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String doPost(JSONObject json) {
		log.debug("processing with:" + json.toString());
		PartnerUtil pu = ((Partner) sc.getUserPrincipal()).getPartnerUtil();
		Account account = null;
		Respondant respondant = null;
		String status = null;
		Date changeDate = null;

		try { // the required parameters
			account = pu.getAccountFrom(json.getJSONObject("account"));
			respondant = pu.getRespondantFrom(json.getJSONObject("applicant"));
			status = json.getJSONObject("applicant").getString("applicant_status");
			String hireDate = json.getJSONObject("applicant").getString("applicant_change_date");
			changeDate = Date.valueOf(hireDate);
			if ((respondant == null) || (account == null)) {
				throw new Exception("Can't find applicant or account.");
			}
		} catch (Exception e) {
			log.warn("Missing Parameters: " + e.getMessage());
			throw new WebApplicationException(e, MISSING_REQUIRED_PARAMS);
		}

		if (account.getAccountId() != respondant.getRespondantAccountId()) {
			log.warn("Account does not match applicant");
			throw new WebApplicationException(ACCOUNT_MATCH);
		}

		JSONObject jresp = respondant.getJSON();

		switch (status) {
		case "notoffered":
			respondant.setRespondantStatus(Respondant.STATUS_REJECTED);
			break;
		case "offered":
			respondant.setRespondantStatus(Respondant.STATUS_OFFERED);
			break;
		case "declinedoffer":
			respondant.setRespondantStatus(Respondant.STATUS_DECLINED);
			break;
		case "hired":
			respondant.setRespondantStatus(Respondant.STATUS_HIRED);
			respondant.setRespondantHireDate(changeDate);
			break;
		case "quit":
			respondant.setRespondantStatus(Respondant.STATUS_QUIT);
			break;
		case "terminated":
			respondant.setRespondantStatus(Respondant.STATUS_TERMINATED);
			break;
		default:
			throw new WebApplicationException(UNKNOWN_STATUS);
		}

		respondant.mergeMe();

		return jresp.toString();
	}
}