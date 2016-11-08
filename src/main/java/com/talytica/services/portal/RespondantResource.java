package com.talytica.services.portal;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.service.RespondantService;
import com.employmeo.util.DBUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;



@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/respondant")
@Api( value="/1/respondant", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class RespondantResource {
	private static final Logger log = LoggerFactory.getLogger(RespondantResource.class);
	private static final long ONE_DAY = 24*60*60*1000; // one day in milliseconds

	@Autowired
	private RespondantService respondantService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the list of all Respondants", response = Respondant.class, responseContainer = "List")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Respondants found"),
	     @ApiResponse(code = 404, message = "Respondants not found")
	   })	
	public Iterable<Respondant> getAllRespondants() {
		return respondantService.getAllRespondants();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the respondant by provided Id", response = Respondant.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Respondant found"),
	     @ApiResponse(code = 404, message = "No such Respondant found")
	   })	
	public Response getRespondant(@ApiParam(value = "respondant id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested respondant by id {}", id);
		
		Respondant respondant = respondantService.getRespondantById(id);
		log.debug("Returning respondant by id {} as {}", id, respondant);
		
		if(null != respondant) {
			return Response.status(Status.OK).entity(respondant).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Persists the provided respondant", response = Respondant.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Respondant saved"),
	   })	
	public Response saveRespondant(Respondant respondant) {
		log.debug("Requested respondant save: {}", respondant);
		
		Respondant savedRespondant = respondantService.save(respondant);
		log.debug("Saved respondant {}", savedRespondant);
		
		return Response.status(Status.CREATED).entity(savedRespondant).build();
	}
	
	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "Searches for respondants", response = Response.class)
	   @ApiResponses(value = {
			     @ApiResponse(code = 200, message = "Respondant found"),
			     @ApiResponse(code = 404, message = "No such Respondant found")
			   })	
	public Response searchRespondants(
			@ApiParam(value = "account id") @QueryParam("id") Long accountId,
			@DefaultValue("-1") @ApiParam(value = "status low") @QueryParam("low") int statusLow,
			@DefaultValue("99") @ApiParam(value = "status high") @QueryParam("high") int statusHigh,
			@DefaultValue("-1") @ApiParam(value = "location id") @QueryParam("location") Long locationId,
			@DefaultValue("-1") @ApiParam(value = "position id") @QueryParam("position") Long positionId,
			@DefaultValue("2015-01-01") @ApiParam(value = "from date") @QueryParam("fromdate") String fromDate,
			@DefaultValue("2020-12-31") @ApiParam(value = "to date") @QueryParam("todate") String toDate) {

		log.debug("Fetching respondants");
		Timestamp from = new Timestamp(Date.valueOf(fromDate).getTime());
		Timestamp to = new Timestamp(Date.valueOf(toDate).getTime() + ONE_DAY);

		String locationSQL = "";
		String positionSQL = "";
		String statusSQL = "AND r.respondantStatus >= :statusLow AND r.respondantStatus <= :statusHigh ";
		
		if (locationId > -1)
			locationSQL = "AND r.respondantLocationId = :locationId ";
		if (positionId > -1)
			positionSQL = "AND r.respondantPositionId = :positionId ";


		EntityManager em = DBUtil.getEntityManager();
		String dateSQL = "AND r.respondantCreatedDate >= :fromDate AND r.respondantCreatedDate <= :toDate ";
		String sql = "SELECT r from Respondant r WHERE r.respondantAccountId = :accountId "
				+ locationSQL + positionSQL + statusSQL 
				+ dateSQL + "ORDER BY r.respondantCreatedDate DESC";
		TypedQuery<com.employmeo.objects.Respondant> query = em.createQuery(sql, com.employmeo.objects.Respondant.class);
		query.setParameter("accountId", accountId);
		if (locationId > -1)
			query.setParameter("locationId", locationId);
		if (positionId > -1)
			query.setParameter("positionId", positionId);
		query.setParameter("statusLow", statusLow);
		query.setParameter("statusHigh", statusHigh);
		query.setParameter("fromDate", from);
		query.setParameter("toDate", to);

		List<com.employmeo.objects.Respondant> respondants = query.getResultList();
		if(null != respondants) {
			JSONArray response = new JSONArray();
			for (int j = 0; j < respondants.size(); j++) {
				respondants.get(j).getAssessmentScore();
				JSONObject jresp = respondants.get(j).getJSON();
				jresp.put("scores", respondants.get(j).getAssessmentScore());
				jresp.put("detailed_scores", respondants.get(j).getAssessmentDetailedScore());
				jresp.put("position", respondants.get(j).getPosition().getJSON());

				response.put(jresp);
			}

			return Response.status(Status.OK).entity(response.toString()).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}

	}
}
