package com.talytica.integration.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Location;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Position;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.integration.util.DefaultPartnerUtil;
import com.talytica.integration.util.PartnerUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/account")
@Api( value="/1/account", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)

public class AccountResource {

	private static final Logger log = LoggerFactory.getLogger(GetAssessmentsResource.class);
	@Context
	private SecurityContext sc;
	@Autowired
	PartnerRepository partnerRepository;
		
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets list of locations", response = String.class)
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/{atsId}/locations")
	public Response getLocations(@ApiParam (value = "Account ID")  @PathParam("atsId") String atsId) {
		log.debug("Get Assessments called with: {}" , atsId);
		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = new DefaultPartnerUtil(partner);
		Account account = null;

		JSONObject json = new JSONObject();
		json.put("account_ats_id", atsId);
		account = pu.getAccountFrom(json);
		if (account == null) return Response.status(Response.Status.NOT_FOUND).build();

		JSONArray response = new JSONArray();

		Set<Location> locations = account.getLocations();
		for (Location loc : locations) {
				JSONObject location = new JSONObject();
				location.put("location_name", loc.getLocationName());
				if (loc.getAtsId() != null)
					location.put("location_ats_id", pu.trimPrefix(loc.getAtsId()));
				location.put("location_id", loc.getId());
				response.put(location);
		}

		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets list of positions", response = String.class)
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/{atsId}/positions")
	public Response getPositions(@ApiParam (value = "Account ID")  @PathParam("atsId") String atsId) {
		log.debug("Get Assessments called with: {}" , atsId);
		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = new DefaultPartnerUtil(partner);
		Account account = null;

		JSONObject json = new JSONObject();
		json.put("account_ats_id", atsId);
		account = pu.getAccountFrom(json);
		if (account == null) return Response.status(Response.Status.NOT_FOUND).build();

		JSONArray response = new JSONArray();

		Set<Position> positions = account.getPositions();
		for (Position pos : positions) {
			JSONObject position = new JSONObject();
			position.put("position_name", pos.getPositionName());
			position.put("position_description", pos.getDescription());
			position.put("position_id", pos.getId());
			response.put(position);
		}

		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets list of assessments", response = String.class)
	   @ApiResponses(value = {
		  @ApiResponse(code = 200, message = "Request Processed"),
		  @ApiResponse(code = 404, message = "Account Not Found")
	   })
	@Path("/{atsId}/asssessments")
	public Response getAssessments(@ApiParam (value = "Account ID")  @PathParam("atsId") String atsId) {
		log.debug("Get Assessments called with: {}" , atsId);
		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = new DefaultPartnerUtil(partner);
		Account account = null;

		JSONObject json = new JSONObject();
		json.put("account_ats_id", atsId);
		account = pu.getAccountFrom(json);
		if (account == null) return Response.status(Response.Status.NOT_FOUND).build();

		JSONArray response = new JSONArray();

		Set<AccountSurvey> surveys = account.getAccountSurveys();
		for (AccountSurvey as : surveys) {
			JSONObject survey = new JSONObject();
			survey.put("assessment_name", as.getDisplayName());
			survey.put("assessment_asid", as.getId());
			response.put(survey);
		}

		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}	

}
