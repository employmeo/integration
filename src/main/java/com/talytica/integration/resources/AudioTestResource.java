package com.talytica.integration.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.Response;
import com.employmeo.data.repository.ResponseRepository;
import com.employmeo.data.service.RespondantService;
import com.talytica.integration.service.BeyondVerbalService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jersey.repackaged.com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

@Slf4j
@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/audio")
@Api( value="/1/audio", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class AudioTestResource {

	@Autowired
	private BeyondVerbalService beyondVerbalService;
	
	@Autowired
	private RespondantService respondantService;
	
	@Autowired
	private ResponseRepository responseRepository;
		
	@POST
	@Path("/analyze")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "analyzes media file")
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "File Analyzed"),
	   })
	public JSONObject analyzeResponse(@ApiParam(name="responseid") @FormParam("responseid") Long responseId) {
		Response response = responseRepository.findOne(responseId);
		String recordingId = response.getResponseText();
		if (recordingId != null) return beyondVerbalService.getAnalysis(recordingId);
		
		return beyondVerbalService.analyzeResponse(responseId);		
	}
	
	@POST
	@Path("/start")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "analyzes media file", response = String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "File Analyzed"),
	   })
	public String startRecording(@ApiParam(name="responseid") @FormParam("responseid") Long responseId) {
		Response response = responseRepository.findOne(responseId);
		String recordingId = beyondVerbalService.startAnalysis(responseId);
		if (recordingId != null) {
			response.setResponseText(recordingId);
			responseRepository.save(response);
		}
		return beyondVerbalService.startAnalysis(responseId);
	}
	
	@POST
	@Path("/upstream")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "analyzes recording id for response id")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Yay!"),
	   })
	public JSONObject analyzeMedia(
			@ApiParam(name="mediaUrl") @FormParam("mediaUrl") String responseMedia,
			@ApiParam(name="recordingId") @FormParam("recordingId") String recordingId) {
		return beyondVerbalService.analyzeMedia(responseMedia, recordingId);
	}

	@POST
	@Path("/analyzerespondant")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "analyzes recording id for response id", response=RespondantScore.class, responseContainer="list")
	   @ApiResponses(value = {
	     @ApiResponse(code = 200, message = "Yay!"),
	   })
	public List<RespondantScore> analyzeResponses(@ApiParam(name="respondantId") @FormParam("respondantId") Long respondantId) {

		List<Response> responses = Lists.newArrayList(respondantService.getGradeableResponses(respondantId));
		return beyondVerbalService.analyzeResponses(responses);	
	}
	

	
	
}