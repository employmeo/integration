package com.talytica.integration.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.service.PartnerService;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;
import com.talytica.integration.service.WorkflowService;

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
	private PartnerService partnerService;
	@Autowired
	private WorkflowService workflowService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Orders an assessment for a particular respondant", response = String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Order Processed"),
	   })
	public Response doPost(String body) throws JSONException {
		JSONObject json = new JSONObject(body);

		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);

		Account account = pu.getAccountFrom(json.getJSONObject("account"));
		if (null == account) return Response.status(Response.Status.BAD_REQUEST)
				.entity("{ message: 'Unable to match account'}").build();
		Respondant respondant = pu.createRespondantFrom(json, account);
		if (null == respondant) {
			log.warn("Failed to process {}'s Order: {}", partner.getPartnerName(), json);
			return Response.status(Response.Status.BAD_REQUEST).entity("{ message: 'Unable to process order'}").build();
		}
				
		JSONObject output = pu.prepOrderResponse(json, respondant);
		
		workflowService.executeInvitedWorkflows(respondant);
		log.debug("{} request for assessment complete: {}",partner.getPartnerName(), respondant.getAtsId());
		return Response.status(Response.Status.ACCEPTED).entity(output.toString()).build();
	}

}