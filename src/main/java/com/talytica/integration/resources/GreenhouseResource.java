package com.talytica.integration.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.employmeo.data.model.*;
import com.employmeo.data.service.AccountService;
import com.employmeo.data.service.AccountSurveyService;
import com.employmeo.data.service.PartnerService;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.ExternalLinksService;
import com.talytica.integration.objects.*;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;
import com.talytica.integration.partners.greenhouse.GreenhouseAssessmentOrder;
import com.talytica.integration.partners.greenhouse.GreenhouseErrorNotice;
import com.talytica.integration.partners.greenhouse.GreenhouseStatusResponse;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/greenhouse")
@Api( value="/1/greenhouse", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)

public class GreenhouseResource {

	
	private static final Response ACCOUNT_NOT_FOUND = Response.status(Response.Status.NOT_FOUND).entity("Account Not Found").build();
	private static final Response RESPONDANT_NOT_FOUND = Response.status(Response.Status.NOT_FOUND).entity("Candidate Not Found").build();
	private static final Response ACCOUNT_MATCH = Response.status(Response.Status.CONFLICT).entity("{ message: 'Applicant ID not found for Account ID' }").build();
	
	@Context
	private SecurityContext sc;
	@Autowired
	private PartnerService partnerService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private RespondantService respondantService;
	@Autowired
	private ExternalLinksService externalLinksService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets list of assessments for logged in account", response = ATSAssessment.class, responseContainer = "List")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/list")
	public Response getAssessments() throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		Account account = accountService.getByPartnerId(partner.getId());
		if (account == null) {
			return ACCOUNT_NOT_FOUND;
		}

		JSONArray response = pu.formatSurveyList(account.getAccountSurveys());	
		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets list of assessments for logged in account", response = GreenhouseStatusResponse.class)
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/status")
	public GreenhouseStatusResponse getStatus(@ApiParam (value = "Candiate ID")  @QueryParam("partner_interview_id") Long respondantId) throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		if (account == null)throw new WebApplicationException(ACCOUNT_NOT_FOUND);

		Respondant respondant = respondantService.getRespondantById(respondantId);
		if (respondant == null)throw new WebApplicationException(RESPONDANT_NOT_FOUND);

		if (account.getId() != respondant.getAccountId()) {
			log.warn("Account does not match Applicant");
			throw new WebApplicationException(ACCOUNT_MATCH);
		}
		
		GreenhouseStatusResponse response = new GreenhouseStatusResponse(respondant);
		response.setPartner_profile_url(externalLinksService.getPortalLink(respondant));
		return response;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Order new assessment to be emailed to Greenhouse candidate")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 401, message = "API key not accepted"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/order")
	public Response postOrder(@ApiParam (value = "Order Object", type="GreenhouseAssessmentOrder")  @RequestBody GreenhouseAssessmentOrder order) throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		if (account == null)throw new WebApplicationException(ACCOUNT_NOT_FOUND);

		Respondant respondant = pu.createRespondantFrom(order.toJson(), account);		
		JSONObject output = pu.prepOrderResponse(order.toJson(), respondant);
		
		return Response.status(Response.Status.OK).entity(output.toString()).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Logs errors experienced by Greenhouse API")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 401, message = "API key not accepted"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/errors")
	public Response postError(@ApiParam (value = "Error Object")  @RequestBody GreenhouseErrorNotice notice) throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		if (account == null)throw new WebApplicationException(ACCOUNT_NOT_FOUND);
		log.error("Greenhouse reported error: {}",notice);
		return Response.status(Response.Status.OK).build();
	}
	
}
