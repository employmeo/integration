package com.talytica.integration.resources;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONObject;

import com.employmeo.objects.Account;
import com.employmeo.objects.Partner;
import com.employmeo.objects.Respondant;
import com.employmeo.util.EmailUtility;
import com.employmeo.util.ExternalLinksUtil;
import com.employmeo.util.PartnerUtil;

@Path("icimsstatusupdate")
public class ICIMSStatusUpdate {

	@Context
	private SecurityContext sc;
	private static final Logger log = LoggerFactory.getLogger(ICIMSStatusUpdate.class);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPost(JSONObject json) {
		log.debug("ICIMS Application Complete with: " +json);

		PartnerUtil pu = PartnerUtil.getUtilFor((Partner) sc.getUserPrincipal());
		Account account = pu.getAccountFrom(json);

		// TODO - Build out Hire Notification Logic (what are the status types, etc...)
		// String newStatus = json.getString("newStatus");//	New status	(ID)
		// String oldStatus = json.getString("oldStatus");//	Old status	(ID)
		// String eventType = json.getString("eventType");
		//
		// if status change is from assessed to offered, not offered, hired, etc - then
		// lookup the respondant in employmeo and update accordingly. otherwise, create
		// a new respondant as below...
		
		Respondant applicant = pu.createRespondantFrom(json, account);
		applicant.refreshMe();
		if (applicant.getRespondantStatus() < Respondant.STATUS_COMPLETED) {
			EmailUtility.sendEmailInvitation(applicant);
		}
		
		URI link = null;
		try {
			link = new URI(ExternalLinksUtil.getAssessmentLink(applicant));
		} catch (Exception e) {
			log.warn("Failed to URI-ify link: " + ExternalLinksUtil.getAssessmentLink(applicant));			
		}
		
		return Response.seeOther(link).build();
	}
}
