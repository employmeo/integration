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

import com.employmeo.data.model.*;
import com.employmeo.data.service.AccountService;
import com.employmeo.data.service.PartnerService;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.ExternalLinksService;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;
import com.talytica.integration.partners.workable.WorkableAssessmentOrder;
import com.talytica.integration.partners.workable.WorkableStatusResponse;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/workable")
@Api( value="/1/workable", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class WorkableResource {

	
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
	@ApiOperation(value = "Gets list of assessments for logged in account")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/tests")
	public Response getAssessments() throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		Account account = accountService.getByPartnerId(partner.getId());
		log.debug("API called by {} for account {}",partner,account);
		if (account == null) {
			return ACCOUNT_NOT_FOUND;
		}

		JSONObject response = new JSONObject();
		JSONArray tests = pu.formatSurveyList(account.getAccountSurveys());
		response.put("tests", tests);
		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets list of assessments for logged in account")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/jobs")
	public Response getPositions() throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		log.debug("API called by {} for account {}",partner,account);
		if (account == null) {
			return ACCOUNT_NOT_FOUND;
		}

		JSONObject response = new JSONObject();
		JSONArray jobs = new JSONArray();
		for (Position position : account.getPositions()){
			JSONObject job = new JSONObject();
			job.put("id", position.getId().toString());
			job.put("name", position.getPositionName());	
		}
		response.put("jobs", jobs);
		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Checks Status of Respondant")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/assessments/id")
	public WorkableStatusResponse getStatus(@ApiParam (value = "Candiate ID")  @QueryParam("Assessment Id") Long respondantId) throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		log.debug("API called by {} for account {}",partner,account);
		if (account == null)throw new WebApplicationException(ACCOUNT_NOT_FOUND);

		Respondant respondant = respondantService.getRespondantById(respondantId);
		if (respondant == null)throw new WebApplicationException(RESPONDANT_NOT_FOUND);

		if (account.getId() != respondant.getAccountId()) {
			log.warn("Account does not match Applicant");
			throw new WebApplicationException(ACCOUNT_MATCH);
		}
		
		return new WorkableStatusResponse(respondant);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Checks Status of Respondant", response = WorkableStatusResponse.class)
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/assessments/{id}/shared-link")
	public JSONObject getLink(@ApiParam (value = "Candiate ID")  @QueryParam("Assessment Id") Long respondantId) throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		if (account == null)throw new WebApplicationException(ACCOUNT_NOT_FOUND);

		Respondant respondant = respondantService.getRespondantById(respondantId);
		if (respondant == null)throw new WebApplicationException(RESPONDANT_NOT_FOUND);

		if (account.getId() != respondant.getAccountId()) {
			log.warn("Account does not match Applicant");
			throw new WebApplicationException(ACCOUNT_MATCH);
		}
		
		JSONObject response = new JSONObject();
		response.put("url", externalLinksService.getPortalLink(respondant));
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
	@Path("/assessments")
	public Response postOrder(@ApiParam (value = "Order Object", type="WorkableAssessmentOrder")  @RequestBody WorkableAssessmentOrder order ) throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		if (account == null)throw new WebApplicationException(ACCOUNT_NOT_FOUND);

		Respondant respondant = pu.createRespondantFrom(order.toJson(), account);		
		JSONObject output = pu.prepOrderResponse(order.toJson(), respondant);
		
		return Response.status(Response.Status.OK).entity(output.toString()).build();
	}
}
