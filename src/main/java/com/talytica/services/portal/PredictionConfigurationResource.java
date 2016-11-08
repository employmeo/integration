package com.talytica.services.portal;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.PositionPredictionConfiguration;
import com.employmeo.data.model.PredictionModel;
import com.employmeo.data.model.PredictionTarget;
import com.employmeo.data.service.PredictionConfigurationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/predictionconfiguration")
@Api( value="/1/predictionconfiguration", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class PredictionConfigurationResource {
	private static final Logger log = LoggerFactory.getLogger(PredictionConfigurationResource.class);

	@Autowired
	private PredictionConfigurationService predictionConfigurationService;

	@GET
	@Path("/predictionmodel")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the list of all PredictionModels", response = PredictionModel.class, responseContainer = "List")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "PredictionModels found"),
	     @ApiResponse(code = 404, message = "PredictionModels not found")
	   })	
	public Iterable<PredictionModel> getAllPredictionModels() {
		return predictionConfigurationService.getAllPredictionModels();
	}
	
	@GET
	@Path("/predictionmodel/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the predictionModel by provided Id", response = PredictionModel.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "PredictionModel found"),
	     @ApiResponse(code = 404, message = "No such PredictionModel found")
	   })	
	public Response getPredictionModel(@ApiParam(value = "predictionModel id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested predictionModel by id {}", id);
		
		PredictionModel predictionModel = predictionConfigurationService.getPredictionModelById(id);
		log.debug("Returning predictionModel by id {} as {}", id, predictionModel);
		
		if(null != predictionModel) {
			return Response.status(Status.OK).entity(predictionModel).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	
	
	@POST
	@Path("/predictionmodel")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Persists the provided predictionModel", response = PredictionModel.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "PredictionModel saved"),
	   })	
	public Response savePredictionModel(PredictionModel predictionModel) {
		log.debug("Requested predictionModel save: {}", predictionModel);
		
		PredictionModel savedPredictionModel = predictionConfigurationService.save(predictionModel);
		log.debug("Saved predictionModel {}", savedPredictionModel);
		
		return Response.status(Status.CREATED).entity(savedPredictionModel).build();
	}	
	
// --------------------------------
	
	@GET
	@Path("/predictiontarget")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the list of all PredictionTargets", response = PredictionTarget.class, responseContainer = "List")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "PredictionTargets found"),
	     @ApiResponse(code = 404, message = "PredictionTargets not found")
	   })	
	public Iterable<PredictionTarget> getAllPositionTargets() {
		return predictionConfigurationService.getAllPredictionTargets();
	}
	
	@GET
	@Path("/predictiontarget/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Gets the predictionTarget by provided Id", response = PredictionTarget.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "PredictionTarget found"),
	     @ApiResponse(code = 404, message = "No such PredictionTarget found")
	   })	
	public Response getPredictionTarget(@ApiParam(value = "predictionTarget id") @PathParam("id") @NotNull Long id) {
		log.debug("Requested predictionTarget by id {}", id);
		
		PredictionTarget predictionTarget = predictionConfigurationService.getPredictionTargetById(id);
		log.debug("Returning predictionTarget by id {} as {}", id, predictionTarget);
		
		if(null != predictionTarget) {
			return Response.status(Status.OK).entity(predictionTarget).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}	
	
	@POST
	@Path("/predictiontarget")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Persists the provided predictionTarget", response = PredictionTarget.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "PredictionTarget saved"),
	   })	
	public Response savePredictionTarget(PredictionTarget predictionTarget) {
		log.debug("Requested predictionTarget save: {}", predictionTarget);
		
		PredictionTarget savedpredictionTarget = predictionConfigurationService.save(predictionTarget);
		log.debug("Saved predictionTarget {}", savedpredictionTarget);
		
		return Response.status(Status.CREATED).entity(savedpredictionTarget).build();
	}	
	
	// --------------------------------
	
		@GET
		@Path("/positionpredictionconfiguration")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(value = "Gets the list of all PositionPredictionConfigurations", response = PositionPredictionConfiguration.class, responseContainer = "List")
		   @ApiResponses(value = {
		     @ApiResponse(code = 200, message = "PositionPredictionConfigurations found"),
		     @ApiResponse(code = 404, message = "PositionPredictionConfigurations not found")
		   })	
		public Iterable<PositionPredictionConfiguration> getAllPositionPredictionConfigurations() {
			return predictionConfigurationService.getAllPositionPredictionConfigurations();
		}
		
		@GET
		@Path("/positionpredictionconfiguration/{id}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(value = "Gets the positionPredictionConfiguration by provided Id", response = PositionPredictionConfiguration.class)
		   @ApiResponses(value = {
		     @ApiResponse(code = 200, message = "PositionPredictionConfiguration found"),
		     @ApiResponse(code = 404, message = "No such PositionPredictionConfiguration found")
		   })	
		public Response getPositionPredictionConfiguration(@ApiParam(value = "positionPredictionConfiguration id") @PathParam("id") @NotNull Long id) {
			log.debug("Requested positionPredictionConfiguration by id {}", id);
			
			PositionPredictionConfiguration positionPredictionConfiguration = predictionConfigurationService.getPositionPredictionConfigurationById(id);
			log.debug("Returning positionPredictionConfiguration by id {} as {}", id, positionPredictionConfiguration);
			
			if(null != positionPredictionConfiguration) {
				return Response.status(Status.OK).entity(positionPredictionConfiguration).build();
			} else {
				return Response.status(Status.NOT_FOUND).build();
			}
		}	
		
		@POST
		@Path("/positionpredictionconfiguration")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(value = "Persists the provided positionPredictionConfiguration", response = PositionPredictionConfiguration.class)
		   @ApiResponses(value = {
		     @ApiResponse(code = 201, message = "PositionPredictionConfiguration saved"),
		   })	
		public Response savePositionPredictionConfiguration(PositionPredictionConfiguration positionPredictionConfiguration) {
			log.debug("Requested positionPredictionConfiguration save: {}", positionPredictionConfiguration);
			
			PositionPredictionConfiguration savedPositionPredictionConfiguration = predictionConfigurationService.save(positionPredictionConfiguration);
			log.debug("Saved positionPredictionConfiguration {}", savedPositionPredictionConfiguration);
			
			return Response.status(Status.CREATED).entity(savedPositionPredictionConfiguration).build();
		}	
}
