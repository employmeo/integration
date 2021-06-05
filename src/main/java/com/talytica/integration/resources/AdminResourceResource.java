package com.talytica.integration.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.employmeo.data.service.AccountSurveyService;
import com.employmeo.data.service.CorefactorService;
import com.employmeo.data.service.QuestionService;
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.triggers.RespondantPipelineTriggers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

@Component
@Slf4j
@Path("/1/admin")
@Api(value="/1/admin")
public class AdminResourceResource {

	@Autowired
	QuestionService questionService;

	@Autowired
	CorefactorService corefactorService;
	
	@Autowired
	AccountSurveyService accountSurveyService;
	
	@Autowired
	RespondantPipelineTriggers respondantPipelineTriggers;
	
	@Autowired
	RespondantService respondantService;
	
	@Context
	SecurityContext sc;
	
	@POST
	@Path("/clearcache")
	@ApiOperation(value = "Clears System Caches")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "OK - Cache Cleared")
	   })
	public void clearCache() {
		log.debug("Cache clear called by: {}", sc.getUserPrincipal().getName());
		questionService.clearCache();
		corefactorService.clearCache();
		accountSurveyService.clearCache();
	}
	
	@POST
	@Path("/trigger/prescreen")
	@ApiOperation(value = "Triggers PreScreen Predictions")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "OK - PreSreen Predictions Triggered")
	   })
	public void triggerPreScreen() {
		log.debug("Pipeline: Pre-Screen Triggered by {}", sc.getUserPrincipal().getName());
		respondantPipelineTriggers.triggerRespondantPreScreen();
	}
	
	@POST
	@Path("/trigger/scoring")
	@ApiOperation(value = "Triggers Scoring")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "OK - Scoring Triggered")
	   })
	public void triggerScoring() {
		log.debug("Pipeline: Scoring Triggered by {}", sc.getUserPrincipal().getName());
		respondantPipelineTriggers.triggerRespondantAssessmentScoring();
	}
	
	@POST
	@Path("/trigger/grading")
	@ApiOperation(value = "Triggers Pipeline Grading")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "OK - Grading Triggered")
	   })
	public void triggerGrading() {
		log.debug("Pipeline: Grading Triggered by {}", sc.getUserPrincipal().getName());
		respondantPipelineTriggers.triggerGraderCompute();
	}
	
	@POST
	@Path("/trigger/predictions")
	@ApiOperation(value = "Triggers")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "OK - Predictions Triggered")
	   })
	public void triggerPredictions() {
		log.debug("Pipeline: Predictions Triggered by {}", sc.getUserPrincipal().getName());
		respondantPipelineTriggers.triggerPredictions();
	}
	
	@POST
	@Path("/trigger/all")
	@ApiOperation(value = "Triggers all pipeline activities")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "OK - All Actions Triggered")
	   })
	public void triggerAll() {
		log.debug("Pipeline: All Actions Triggered by {}", sc.getUserPrincipal().getName());
		respondantPipelineTriggers.triggerRespondantPreScreen();
		respondantPipelineTriggers.triggerRespondantAssessmentScoring();
		respondantPipelineTriggers.triggerGraderCompute();;
		respondantPipelineTriggers.triggerPredictions();
	}
	
	@POST
	@Path("/reset")
	@Consumes(MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Triggers all pipeline activities")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "OK - Respondant Error Reset")
	   })
	public void resetErrorStatus(@ApiParam("Respondant ID") Long respondantId) {
		log.debug("Reset respondant {} Triggered by {}", respondantId, sc.getUserPrincipal().getName());
		respondantService.clearError(respondantId);

	}
	
	@POST
	@Path("/resetgroup")
	@ApiOperation(value = "Triggers all pipeline activities")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "OK - Respondant Errors Reset")
	   })
	public void resetErrorStatus(@ApiParam("Respondant IDs") List<Long> respondantIds) {
		log.debug("Reset respondant list {} Triggered by {}", respondantIds, sc.getUserPrincipal().getName());
		respondantService.clearErrors(respondantIds);

	}

}