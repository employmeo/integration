package com.talytica.integration.resources;

import java.sql.Date;

import javax.annotation.security.PermitAll;
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
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.util.PartnerUtil;
import com.talytica.integration.util.PartnerUtilityRegistry;

import io.swagger.annotations.Api;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/hirenotice")
@Api( value="/hirenotice", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
@PermitAll
public class HireNoticeResource {
	private final static Response MISSING_REQUIRED_PARAMS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Missing Required Parameters' }").build();
	private final static Response UNKNOWN_STATUS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Unknown Applicant Status' }").build();
	private final static Response ACCOUNT_MATCH = Response.status(Response.Status.CONFLICT)
			.entity("{ message: 'Applicant ID not found for Account ID' }").build();
	private static final Logger log = LoggerFactory.getLogger(HireNoticeResource.class);

	@Context
	private SecurityContext sc;
	@Autowired
	PartnerRepository partnerRepository;
	@Autowired
	RespondantService respondantService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPost( String body) {
		JSONObject json = new JSONObject(body);
		log.debug("processing with:" + json.toString());
		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);

		Account account = null;
		Respondant respondant = null;
		String status = null;
		Date changeDate = null;

		try { // the required parameters
			account = pu.getAccountFrom(json.getJSONObject("account"));
			respondant = pu.getRespondantFrom(json.getJSONObject("applicant"), account);
			status = json.getJSONObject("applicant").getString("applicant_status");
			Long hireDate = json.getJSONObject("applicant").getLong("applicant_change_date");
			changeDate = new Date(hireDate);
			if ((respondant == null) || (account == null)) {
				throw new Exception("Can't find applicant or account.");
			}
		} catch (Exception e) {
			log.warn("Missing Parameters: " + e.getMessage());
			throw new WebApplicationException(e, MISSING_REQUIRED_PARAMS);
		}

		if (account.getId() != respondant.getAccountId()) {
			log.warn("Account does not match applicant");
			throw new WebApplicationException(ACCOUNT_MATCH);
		}

		Integer newStatus = null;
		switch (status) {
		case "invited":
			newStatus = Respondant.STATUS_INVITED;
			break;
		case "notoffered":
			newStatus = Respondant.STATUS_REJECTED;
			break;
		case "offered":
			newStatus = Respondant.STATUS_OFFERED;
			break;
		case "declinedoffer":
			newStatus = Respondant.STATUS_DECLINED;
			break;
		case "hired":
			newStatus = Respondant.STATUS_HIRED;
			respondant.setHireDate(changeDate);
			break;
		case "quit":
			newStatus = Respondant.STATUS_QUIT;
			break;
		case "terminated":
			newStatus = Respondant.STATUS_TERMINATED;
			break;
		default:
			throw new WebApplicationException(UNKNOWN_STATUS);
		}
		if ((newStatus != null) && (newStatus > respondant.getRespondantStatus())) {
			respondant.setRespondantStatus(newStatus);
			respondantService.save(respondant);
		} else {
			log.warn("Failed to update respondant {} from status {} to {}",respondant.getId(),respondant.getRespondantStatus(),newStatus);
			return Response.status(Response.Status.NOT_MODIFIED).build();
		}

		return Response.status(Response.Status.ACCEPTED).build();
	}
}