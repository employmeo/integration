package com.talytica.integration.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.common.service.EmailService;
import com.talytica.common.service.ExternalLinksService;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import io.swagger.annotations.Api;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/icimsstatusupdate")
@Api( value="/icimsstatusupdate", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class ICIMSStatusUpdateResource {

	@Autowired
	PartnerRepository partnerRepository;
	@Autowired
	EmailService emailService;
	@Autowired
	ExternalLinksService externalLinksService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	private static final Logger log = LoggerFactory.getLogger(ICIMSStatusUpdateResource.class);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPost( String body) throws JSONException  {
		JSONObject json = new JSONObject(body);
		log.debug("ICIMS Status Update requested: {}", json);
		Partner partner = partnerRepository.findByPartnerName("ICIMS");
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
		
		//URI link = null;
		//
		//try {
		//	link = new URI(externalLinksService.getAssessmentLink(applicant));
		//} catch (Exception e) {
		//	log.warn("Failed to URI-ify link: " + externalLinksService.getAssessmentLink(applicant));
		//}

		return Response.ok().build();
	}
}
