package com.talytica.integration.resources;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.service.AccountService;
import com.employmeo.data.service.PartnerService;
import com.talytica.integration.objects.*;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/account")
@Api( value="/1/account", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)

public class AccountResource {

	private static final Response ACCOUNT_NOT_FOUND = Response.status(Response.Status.NOT_FOUND).entity("Account Not Found").build();

	@Context
	private SecurityContext sc;
	@Autowired
	private PartnerService partnerService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets list of locations", response = ATSLocation.class, responseContainer = "List")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/{atsId}/locations")
	public Response getLocations(@ApiParam (value = "Account ID")  @PathParam("atsId") String atsId) throws JSONException {
		log.debug("Get Locations called with: {}" , atsId);
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		Account account = accountService.getAccountByAtsId(pu.addPrefix(atsId));

		if (account == null) {
			return ACCOUNT_NOT_FOUND;
		}

		JSONArray response = new JSONArray();

		Set<Location> locations = account.getLocations();
		for (Location loc : locations) {
				JSONObject location = new JSONObject();
				location.put("location_name", loc.getLocationName());
				if (loc.getAtsId() != null) {
					location.put("location_ats_id", pu.trimPrefix(loc.getAtsId()));
				}
				location.put("location_id", loc.getId());
				response.put(location);
		}

		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets list of positions", response = ATSPosition.class, responseContainer = "List")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/{atsId}/positions")
	public Response getPositions(@ApiParam (value = "Account ID")  @PathParam("atsId") String atsId) {
		log.debug("Get Positions called with: {}" , atsId);
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		Account account = accountService.getAccountByAtsId(pu.addPrefix(atsId));

		if (account == null) {
			return ACCOUNT_NOT_FOUND;
		}

		List<ATSPosition> response = new ArrayList<>();
		Set<Position> positions = account.getPositions();
		for (Position pos : positions) {
			ATSPosition position = new ATSPosition();
			position.positionName = pos.getPositionName();
			position.description = pos.getDescription();
			position.id = pos.getId();
			response.add(position);
		}

		return Response.status(Response.Status.OK).entity(response).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets list of assessments", response = ATSAssessment.class, responseContainer = "List")
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/{atsId}/asssessments")
	public Response getAssessments(@ApiParam (value = "Account ID")  @PathParam("atsId") String atsId) throws JSONException {
		log.debug("Get Assessments called with: {}" , atsId);
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		Account account = accountService.getAccountByAtsId(pu.addPrefix(atsId));

		if (account == null) {
			return ACCOUNT_NOT_FOUND;
		}

		JSONArray response = new JSONArray();

		Set<AccountSurvey> surveys = account.getAccountSurveys();
		for (AccountSurvey as : surveys) {
			JSONObject survey = new JSONObject();
			as.setAccount(account);
			survey.put("assessment_name", as.getDisplayName());
			survey.put("assessment_asid", as.getId());
			response.put(survey);
		}

		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}

}
