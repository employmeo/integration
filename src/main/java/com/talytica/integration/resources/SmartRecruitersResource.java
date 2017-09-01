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
import com.talytica.integration.partners.SmartRecruitersPartnerUtil;
import com.talytica.integration.partners.greenhouse.GreenhouseAssessmentOrder;
import com.talytica.integration.partners.greenhouse.GreenhouseErrorNotice;
import com.talytica.integration.partners.greenhouse.GreenhouseStatusResponse;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersAssessmentNotification;
import com.talytica.integration.partners.smartrecruiters.SmartRecruitersAssessmentOrder;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/smartrecruiters")
@Api( value="/1/smartrecruiters", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)

public class SmartRecruitersResource {

	
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
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Order new assessment to be emailed to Greenhouse candidate")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 401, message = "API key not accepted"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/notify")
	public Response postNotification(@ApiParam (value = "Order Notification", type="SmartRecruitersAsssessmentNotification")  @RequestBody SmartRecruitersAssessmentNotification notification) throws JSONException {
		log.info("Received Notification From SR: {}", notification);
		Partner partner = partnerService.getPartnerByLogin("smartrecruiters"); // Anonymous posting
		SmartRecruitersPartnerUtil pu = (SmartRecruitersPartnerUtil) partnerUtilityRegistry.getUtilFor(partner);
		
		SmartRecruitersAssessmentOrder order = pu.fetchIndividualOrder(notification);
		log.info("Order associated is: {}", order);
		//Account account = accountService.getAccountByAtsId(pu.addPrefix(order.getCompany().getId()));
		//if (account == null)throw new WebApplicationException(ACCOUNT_NOT_FOUND);
		//Respondant respondant = pu.createRespondantFrom(order.toJson(), account);		
		//JSONObject output = pu.prepOrderResponse(order.toJson(), respondant); // sends email
		
		//pu.changeCandidateStatus(respondant, output.getString("message")); // using post scores method maybe use change Status
		
		return Response.status(Response.Status.OK).build();
	}
		
}
