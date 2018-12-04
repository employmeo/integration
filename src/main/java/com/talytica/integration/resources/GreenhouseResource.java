package com.talytica.integration.resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.talytica.integration.partners.GreenhousePartnerUtil;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;
import com.talytica.integration.partners.greenhouse.GreenhouseApplication;
import com.talytica.integration.partners.greenhouse.GreenhouseAssessmentOrder;
import com.talytica.integration.partners.greenhouse.GreenhouseErrorNotice;
import com.talytica.integration.partners.greenhouse.GreenhousePolling;
import com.talytica.integration.partners.greenhouse.GreenhouseStatusResponse;
import com.talytica.integration.partners.greenhouse.GreenhouseWebHook;

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
	private GraderService graderService;
	@Autowired
	private CorefactorService corefactorService;
	@Autowired
	private ExternalLinksService externalLinksService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;
	@Autowired
	private GreenhousePolling greenhousePolling;

	
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
	@ApiOperation(value = "Checks Status of Respondant", response = GreenhouseStatusResponse.class)
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/status")
	public GreenhouseStatusResponse getStatus(@ApiParam (value = "Candiate ID")  @QueryParam("partner_interview_id") UUID respondantUuid) throws JSONException {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		if (account == null)throw new WebApplicationException(ACCOUNT_NOT_FOUND);

		Respondant respondant = respondantService.getRespondant(respondantUuid);
		if (respondant == null)throw new WebApplicationException(RESPONDANT_NOT_FOUND);

		if (account.getId() != respondant.getAccountId()) {
			log.warn("Account does not match Applicant");
			throw new WebApplicationException(ACCOUNT_MATCH);
		}
		
		GreenhouseStatusResponse response = new GreenhouseStatusResponse(respondant);
		response.setPartner_profile_url(externalLinksService.getPortalLink(respondant));
		
		List<Grader> graders = graderService.getGradersByRespondantId(respondant.getId())
				.stream()
				.filter(g -> g.getStatus() == Grader.STATUS_COMPLETED)
				.collect(Collectors.toList());	
		if (!graders.isEmpty()) response.setMetaReferences(graders);
		
		List<String> scores = Lists.newArrayList();
		for (RespondantScore rs : respondant.getRespondantScores()) {
			Corefactor cf = corefactorService.findCorefactorById(rs.getId().getCorefactorId());
			scores.add(cf.getName() + ": " + rs.getValue() + " of " + cf.getHighValue());
		}
		if (!graders.isEmpty()) response.setMetaScores(scores);
		
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
	
	
	@POST
	@Path("/pullapplicants")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "calls greenhouse polling", response=GreenhouseApplication.class, responseContainer="list")
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Success"),
	   })
	public List<GreenhouseApplication> pollGreenhouse(
			@ApiParam(name="accountId") @FormParam("accountId") Long accountId,
			@ApiParam(name="positionId") @FormParam("positionId") Long positionId,
			@ApiParam(name="Start Date") @FormParam("start") Date startDate,
			@ApiParam(name="End Date") @FormParam("end") Date endDate) {

		Account account = accountService.getAccountById(accountId);
		Position position = accountService.getPositionById(positionId);
		Range<Date> dates = new Range<Date>(startDate,endDate);
		List<GreenhouseApplication> apps = greenhousePolling.getGreenhousePastCandidates(account, position, dates);
		
		for (GreenhouseApplication app : apps) {
			log.info("full app: {}", getApplication(app.getId())); 
		}
		
		return apps;
	}	
	
	
	@POST
	@Path("/webhook")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "calls greenhouse polling")
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Success")
	   })
	public void webhook(@ApiParam(value="GreenHouse WebHook", type="GreenhouseWebhook") @RequestBody GreenhouseWebHook webhook) {
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Account account = accountService.getByPartnerId(partner.getId());
		GreenhousePartnerUtil pu = (GreenhousePartnerUtil) partnerUtilityRegistry.getUtilFor(partner);

		switch (webhook.getAction()) {
		case "ping":
			log.debug("Ping posted: {} ", webhook.getAction(), webhook.getPayload());
			break;
		case "new_candidate_application":
			log.debug("New Candidate {}",webhook.getPayload().getApplication().getId());
			GreenhouseApplication app = pu.getApplicationDetail(webhook.getPayload().getApplication().getId());
			Respondant newResp = pu.createPrescreenCandidate(app, account);
			log.debug("Created respondant: {}", newResp.getId());
			break;
		case "candidate_stage_change":
			log.debug("Candidate {} stage change to {}",webhook.getPayload().getApplication().getId(),webhook.getPayload().getApplication().getCurrent_stage().getName());
			Respondant respondant = pu.getRespondant(webhook.getPayload().getApplication());			
			log.warn("Found respondant: {}, but didn't process", respondant.getId());
			break;
		default:
			log.warn("Unprocessed {} webhook posted with payload: {} ", webhook.getAction(), webhook.getPayload());
			break;
		}
		return;
	}	

	@GET
	@Path("/application/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "calls greenhouse polling")
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Success")
	   })
	public GreenhouseApplication getApplication(@ApiParam(value="GreenHouse WebHook", type="GreenhouseWebhook") @PathParam("id") Long id) {
//		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		Partner partner = partnerService.getPartnerByLogin("greenhouse-sample-api-key");	
		//Account account = accountService.getByPartnerId(partner.getId());
		GreenhousePartnerUtil pu = (GreenhousePartnerUtil) partnerUtilityRegistry.getUtilFor(partner);
		GreenhouseApplication app = pu.getApplicationDetail(id);
		
		return app;
	}	

	
}
