package com.talytica.integration.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantNVP;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.Response;
import com.employmeo.data.repository.ResponseRepository;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.SpeechToTextService;
import com.talytica.common.service.TextAnalyticsService;
import com.talytica.integration.scoring.ScoringModelEngine;
import com.talytica.integration.scoring.ScoringModelRegistry;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


import org.json.JSONObject;

@Slf4j
@Component
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
@Path("/1/test")
@Api( value="/1/test", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_FORM_URLENCODED)
public class TestResource {
	
	@Autowired
	private SpeechToTextService speechToTextService;
	
	@Autowired
	private TextAnalyticsService textAnalyticsService;

	@Autowired
	private RespondantService respondantService;
	
	@Autowired
	private ResponseRepository responseRepository;

	@Autowired
	private ScoringModelRegistry scoringModelRegistry;
	
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Creates and saves audio scores for a respondant", response=RespondantScore.class, responseContainer="list")
	   @ApiResponses(value = {
			   @ApiResponse(code = 200, message = "Analyzed!"),
			   @ApiResponse(code = 400, message = "Failed")
	   })
	@Path("/score")
	public List<RespondantScore> getAudioScores(@ApiParam(name="id",value="Respondant Id") @FormParam("id") Long id) {
		log.debug("id provided is: {}", id);
		Respondant respondant = respondantService.getRespondantById(id);
		
		List<Response> responses = Lists.newArrayList(respondantService.getAudioResponses(id));
		ScoringModelEngine scoringModelEngine = scoringModelRegistry.getScoringModelEngineByName("audio-").get();		
		List<RespondantScore> scores = scoringModelEngine.scoreResponses(respondant, responses);
		log.debug("engine returned: {} Scores", scores.size());
		for (RespondantScore score : scores) {
			log.debug("Saving: {} with value {}", score.getId().getCorefactorId(), score.getValue());
			respondantService.save(score);
		}
		
		return scores;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/videototext")
	@ApiOperation(value = "analyzes media file", response=String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "File Analyzed"),
	   })
	public String videoToText(@ApiParam(name="responseId",value="Response Id") @FormParam("responseId") Long responseId) {
		Response response = responseRepository.findById(responseId).get();
		String text = speechToTextService.translateMedia(response.getResponseMedia(),SpeechToTextService.VIDEO_WEBM);
		if ((null != text) && (!text.isEmpty())) {
			Respondant respondant = respondantService.getRespondantById(response.getRespondantId());
			RespondantNVP nvp = new RespondantNVP();
			nvp.setName(response.getQuestion().getQuestionText());
			nvp.setValue(text);
			nvp.setRespondantId(respondant.getId());
			respondantService.save(nvp);
		}
		
		return "{ translation : " + text + "} ";
	}	
		
	
	@POST
	@Path("/sentimentfortarget")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "analyzes media file", response=String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "File Analyzed"),
	   })
	public String sentimentFromText(@ApiParam(name="text") @FormParam("text") String text,
			@ApiParam(name="target") @FormParam("target") String target) {

		JSONObject json = textAnalyticsService.analyzeSentimentForTarget(text, target);
		
		return json.toString();
	}	
	
}