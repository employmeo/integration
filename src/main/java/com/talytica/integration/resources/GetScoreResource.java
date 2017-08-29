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
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import io.swagger.annotations.Api;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/getscore")
@Api( value="/getscore", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class GetScoreResource {
	private final Response MISSING_REQUIRED_PARAMS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Missing Required Parameters' }").build();
	private final Response ACCOUNT_MATCH = Response.status(Response.Status.CONFLICT)
			.entity("{ message: 'Applicant ID not found for Account ID' }").build();
	private static final Logger log = LoggerFactory.getLogger(GetScoreResource.class);
	@Context
	private SecurityContext sc;
	@Autowired
	PartnerService partnerService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String doPost( String body) throws JSONException {
		JSONObject json = new JSONObject(body);
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		Account account = null;
		Respondant respondant = null;

		log.debug("processing with: {}", json);
		try { // the required parameters
			account = pu.getAccountFrom(json.getJSONObject("account"));
			respondant = pu.getRespondantFrom(json.getJSONObject("applicant"), account);
			if ((account == null) || (respondant == null)) {
				log.debug("account: {} applicant {}", account, respondant);
				throw new Exception ("Not Found: " + json);
			}

		} catch (Exception e) {
			log.warn(e.getMessage());
			throw new WebApplicationException(e, MISSING_REQUIRED_PARAMS);
		}

		if (account.getId() != respondant.getAccountId()) {
			log.warn("Account does not match Applicant");
			throw new WebApplicationException(ACCOUNT_MATCH);
		}

		return pu.getScoresMessage(respondant).toString();
	}
}