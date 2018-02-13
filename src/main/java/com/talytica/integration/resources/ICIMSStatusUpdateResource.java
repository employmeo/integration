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
import com.employmeo.data.service.PartnerService;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.EmailService;
import com.talytica.common.service.ExternalLinksService;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;
import com.talytica.integration.service.WorkflowService;

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
	PartnerService partnerService;
	@Autowired
	EmailService emailService;
	@Autowired
	RespondantService respondantService;
	@Autowired
	ExternalLinksService externalLinksService;
	@Autowired
	WorkflowService workflowService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	private static final Logger log = LoggerFactory.getLogger(ICIMSStatusUpdateResource.class);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPost( String body) throws JSONException  {
		JSONObject json = new JSONObject(body);
		log.debug("ICIMS Status Update requested: {}", json);
		Partner partner = null;
		if (sc.getUserPrincipal() != null) {
			partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		} else {
			partner = partnerService.getPartnerByLogin("icims"); // allowing anonymous posts from ICIMS
		}
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);

		Account account = pu.getAccountFrom(json);
		Respondant applicant = pu.createRespondantFrom(json, account);

		Boolean statusChange = false;
		String newStatus = json.getString("newStatus");

		for (CustomWorkflow flow : applicant.getPosition().getCustomWorkflows()) {
			if ((CustomWorkflow.TYPE_STATUSUPDATE.equalsIgnoreCase(flow.getType())) && (flow.getAtsId().equalsIgnoreCase(newStatus))) {
				switch (flow.getText()) {
				case "hired":
					statusChange = true;
					applicant.setRespondantStatus(Respondant.STATUS_HIRED);
					break;
				case "rejected":
					statusChange = true;
					applicant.setRespondantStatus(Respondant.STATUS_REJECTED);
					break;
				case "reinvite":
					statusChange = true;
					applicant.setRespondantStatus(Respondant.STATUS_REMINDED);
					break;
				default:
				}
			}
		}
		if (statusChange) respondantService.save(applicant);
		if (applicant.getRespondantStatus() < Respondant.STATUS_COMPLETED) {
			pu.inviteCandidate(applicant);
			workflowService.executeInvitedWorkflows(applicant);
		}
		

		return Response.ok().build();
	}
}
