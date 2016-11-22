package com.talytica.integration.resources;

import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.common.service.EmailService;
import com.talytica.integration.util.PartnerUtil;
import com.talytica.integration.util.PartnerUtilityRegistry;

import io.swagger.annotations.Api;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/icimsstatusupdate")
@Api( value="/icimsstatusupdate", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class ICIMSStatusUpdateResource {

	@Context
	private SecurityContext sc;
	@Autowired
	PartnerRepository partnerRepository;
	@Autowired
	EmailService emailService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	private static final Logger log = LoggerFactory.getLogger(ICIMSStatusUpdateResource.class);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPost( String body) {
		JSONObject json = new JSONObject(body);
		log.debug("ICIMS Application Complete with: " +json);

		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);

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

		if (applicant.getRespondantStatus() < Respondant.STATUS_COMPLETED) {
			emailService.sendEmailInvitation(applicant);
		}

		URI link = null;
		try {
			link = new URI(emailService.getAssessmentLink(applicant));
		} catch (Exception e) {
			log.warn("Failed to URI-ify link: " + emailService.getAssessmentLink(applicant));
		}

		return Response.seeOther(link).build();
	}
}
