package com.talytica.integration.resources;

import java.util.Date;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PartnerRepository;
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/hirenotice")
@Api( value="/hirenotice", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
@PermitAll
public class HireNoticeResource {

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
	public Response doPost( String body) throws JSONException  {
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
			if ((respondant == null) || (account == null)) {
				return Response.status(Response.Status.BAD_REQUEST).entity("{ message: 'Applicant Not Found' }").build();
			}
			status = json.getJSONObject("applicant").optString("applicant_status");
			if (status == null) Response.status(Response.Status.BAD_REQUEST).entity("{ message: 'Unknown Applicant Status' }").build();
			try {changeDate = new Date( json.getJSONObject("applicant").getLong("applicant_change_date"));}
			catch (Exception e) {log.warn("Failed to convert date: " + e.getMessage());}
		} catch (Exception e) {
			log.warn("Error Interpreting Parameters: " + e.getMessage());
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{ message: 'Error Interpreting Parameters: "+e.getMessage()+"' }").build();
		}
		log.debug("Updating {} to status {}", respondant.getId(), status);
		if (account.getId() != respondant.getAccountId()) {
			log.warn("Account does not match applicant");
			return Response.status(Response.Status.CONFLICT).entity("{ message: 'Applicant ID not found for Account ID' }").build();
		}

		Integer newStatus = null;
		switch (status) {
		case "invited":
			newStatus = Respondant.STATUS_INVITED;
			break;
		case "advanced":
			newStatus = Respondant.STATUS_ADVANCED;
			respondant.setSecondStageSurveyId(account.getSecondAsid());
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
			if (changeDate != null) respondant.setHireDate(changeDate);
			break;
		case "quit":
			newStatus = Respondant.STATUS_QUIT;
			break;
		case "terminated":
			newStatus = Respondant.STATUS_TERMINATED;
			break;
		default:
			return Response.status(Response.Status.BAD_REQUEST).entity("{ message: 'Unknown Applicant Status' }").build();
		}
		if ((newStatus != null) && (newStatus > respondant.getRespondantStatus())) {
			respondant.setRespondantStatus(newStatus);
			respondantService.save(respondant);
		} else if ((newStatus == null) || (newStatus < respondant.getRespondantStatus())){
			log.debug("Did not update respondant {} from status {} to {}",respondant.getId(),respondant.getRespondantStatus(),newStatus);
			return Response.status(Response.Status.NOT_MODIFIED).entity("Status: " + status + " not accepted").build();
		}

		return Response.status(Response.Status.ACCEPTED).entity("{ message: 'Status Change Accepted' }").build();
	}
}