package com.talytica.integration.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Orders an assessment for a particular respondant", response = String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Order Processed"),
	   })
	public Response doPost(String body) {
		JSONObject json = new JSONObject(body);

		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);

		Account account = pu.getAccountFrom(json.getJSONObject("account"));
		if (null == account) return Response.status(Response.Status.BAD_REQUEST)
				.entity("{ message: 'Unable to match account'}").build();
		Respondant respondant = pu.createRespondantFrom(json, account);
		if (null == respondant) return Response.status(Response.Status.BAD_REQUEST)
				.entity("{ message: 'Unable to process order'}").build();
		JSONObject output = pu.prepOrderResponse(json, respondant);

		log.debug("ATS Request for Assessment Complete: " + respondant.getAtsId());
		return Response.status(Response.Status.ACCEPTED).entity(output.toString()).build();
	}

}