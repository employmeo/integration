package com.talytica.integration.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.employmeo.data.model.*;
import com.employmeo.data.service.AccountService;
import com.employmeo.data.service.PartnerService;
import com.talytica.integration.partners.FountainPartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;
import com.talytica.integration.partners.fountain.FountainWebHook;
import com.talytica.integration.service.WorkflowService;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/fountain")
@Api( value="/v1/fountain", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)

public class FountainResource {
	
	private static final ResponseBuilder ACCOUNT_NOT_FOUND = Response.status(Response.Status.NOT_FOUND).entity("Account Not Found");
	private static final ResponseBuilder APIKEY_NOT_ACCEPTED = Response.status(Response.Status.UNAUTHORIZED).entity("API key not accepted");
	
	@Context
	private SecurityContext sc;
	@Autowired
	private PartnerService partnerService;
	@Autowired
	private AccountService accountService;
	@Autowired
	WorkflowService workflowService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;


	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Order new assessment and create candidate")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 401, message = "API key not accepted"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/stagechange/{apiKey}")
	public Response webhook(
			@PathParam("apiKey") String apiKey,
			@ApiParam (value = "WebHook", type="FountainWebHook")  @RequestBody FountainWebHook webhook
			) throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(apiKey);
		if (partner == null) return APIKEY_NOT_ACCEPTED.build();
		FountainPartnerUtil fpu = (FountainPartnerUtil) partnerUtilityRegistry.getUtilFor(partner);

		Account account = accountService.getByPartnerId(partner.getId());
		if (account == null) return ACCOUNT_NOT_FOUND.build();
		
		JSONObject jOrder = webhook.toJson();
		if (null != partner.getApiKey()) {
			log.debug("Setting up for postback");
			JSONObject delivery = new JSONObject();

			String appId = webhook.getApplicant().getId();
			if (null != appId) {
				String scorePostMethod = fpu.getApplicantUpdateMethod(appId);
				delivery.put("scores_post_url", scorePostMethod);
				jOrder.put("delivery", delivery);
			}
		}
		Respondant candidate = fpu.createRespondantFrom(jOrder, account);		
		workflowService.executePreScreenWorkflows(candidate);

		return Response.status(Response.Status.OK).build();
	}	

}