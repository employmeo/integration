package com.talytica.integration.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.integration.util.PartnerUtil;
import com.talytica.integration.util.PartnerUtilityRegistry;

import io.swagger.annotations.*;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/atsorder")
@Api( value="/atsorder", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class ATSOrderResource {

	@Context
	private SecurityContext sc;
	@Autowired
	PartnerRepository partnerRepository;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	private static final Logger log = LoggerFactory.getLogger(ATSOrderResource.class);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Orders an assessment for a particular respondant", response = String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Order Processed"),
	   })
	public String doPost(String body) {
		JSONObject json = new JSONObject(body);
		log.debug("ATS Requesting Assessment with: " + json.toString());

		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);

		Account account = pu.getAccountFrom(json.getJSONObject("account"));
		Respondant respondant = pu.createRespondantFrom(json, account);
		JSONObject output = pu.prepOrderResponse(json, respondant);

		log.debug("ATS Request for Assessment Complete: " + respondant.getAtsId());
		return output.toString();
	}

}