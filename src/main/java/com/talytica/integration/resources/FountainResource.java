package com.talytica.integration.resources;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.employmeo.data.model.*;
import com.employmeo.data.service.AccountService;
import com.employmeo.data.service.CorefactorService;
import com.employmeo.data.service.GraderService;
import com.employmeo.data.service.PartnerService;
import com.employmeo.data.service.RespondantService;
import com.google.common.collect.Lists;
import com.talytica.common.service.ExternalLinksService;
import com.talytica.integration.objects.*;
import com.talytica.integration.partners.FountainPartnerUtil;
import com.talytica.integration.partners.GreenhousePartnerUtil;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;
import com.talytica.integration.partners.fountain.FountainWebHook;
import com.talytica.integration.partners.greenhouse.GreenhouseApplication;
import com.talytica.integration.partners.greenhouse.GreenhouseAssessmentOrder;
import com.talytica.integration.partners.greenhouse.GreenhouseErrorNotice;
import com.talytica.integration.partners.greenhouse.GreenhousePolling;
import com.talytica.integration.partners.greenhouse.GreenhouseStatusResponse;
import com.talytica.integration.partners.greenhouse.GreenhouseWebHook;
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
	private static final ResponseBuilder RESPONDANT_NOT_FOUND = Response.status(Response.Status.NOT_FOUND).entity("Candidate Not Found");
	private static final ResponseBuilder ACCOUNT_MATCH = Response.status(Response.Status.CONFLICT).entity("{ message: 'Applicant ID not found for Account ID' }");
	
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
	@Path("/webhookold")
	public Response postOrder(@ApiParam (value = "WebHook", type="FountainWebHook")  @RequestBody String webhook) throws JSONException {
		log.info(webhook);
		
		return Response.status(Response.Status.OK).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Order new assessment and create candidate")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 401, message = "API key not accepted"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/webhook")
	public Response postOrder(@ApiParam (value = "WebHook", type="FountainWebHook")  @RequestBody FountainWebHook webhook) throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		FountainPartnerUtil fpu = (FountainPartnerUtil) partnerUtilityRegistry.getUtilFor(partner);
		if (account == null)throw new WebApplicationException(ACCOUNT_NOT_FOUND.build());
		
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
		if (candidate.getRespondantStatus() == Respondant.STATUS_CREATED) {
			workflowService.executeCreatedWorkflows(candidate);
		}
		return Response.status(Response.Status.OK).build();
	}
}
