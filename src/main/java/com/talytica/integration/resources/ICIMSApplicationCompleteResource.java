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
import com.talytica.common.service.ExternalLinksService;
import com.talytica.integration.util.PartnerUtil;
import com.talytica.integration.util.PartnerUtilityRegistry;

import io.swagger.annotations.Api;;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/icimsapplicationcomplete")
@Api( value="/icimsapplicationcomplete", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class ICIMSApplicationCompleteResource {

	@Context
	private SecurityContext sc;
	@Autowired
	PartnerRepository partnerRepository;
	@Autowired
	ExternalLinksService externalLinksService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	private static final Logger log = LoggerFactory.getLogger(ICIMSApplicationCompleteResource.class);



	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPost( String body) {
		JSONObject json = new JSONObject(body);
		log.debug("ICIMS Application Complete with: " +json);

		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);

		Account account = pu.getAccountFrom(json);
		Respondant applicant = pu.createRespondantFrom(json, account);

		URI link = null;
		try {
			link = new URI(externalLinksService.getAssessmentLink(applicant));
		} catch (Exception e) {
			log.warn("Failed to URI-ify link: " + externalLinksService.getAssessmentLink(applicant));
		}

		return Response.seeOther(link).build();

	}
}
