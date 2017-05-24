package com.talytica.integration.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantNVP;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.RespondantScorePK;
import com.employmeo.data.model.Response;
import com.employmeo.data.repository.ResponseRepository;
import com.employmeo.data.service.CorefactorService;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.SpeechToTextService;
import com.talytica.common.service.TextAnalyticsService;
import com.talytica.common.service.BeyondVerbalService;

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
	private SpeechToTextService speechToTextService;
	
	@Autowired
	private TextAnalyticsService textAnalyticsService;

	@Autowired
	private RespondantService respondantService;
	
	@Autowired
	private ResponseRepository responseRepository;
	
	@Autowired
	private CorefactorService corefactorService;
	
	private String WATSON_SENTIMENT = "WatsonSentiment";
	
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

		List<Response> responses = Lists.newArrayList(respondantService.getAudioResponses(respondantId));
		return beyondVerbalService.analyzeResponses(responses);	
	}
	

	@POST
	@Path("/speechtotext")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "analyzes media file", response=String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "File Analyzed"),
	   })
	public String speechToText(@ApiParam(name="responseid") @FormParam("responseid") Long responseId) {
		Response response = responseRepository.findOne(responseId);
		String text = speechToTextService.translateMedia(response.getResponseMedia(),SpeechToTextService.AUDIO_WAV);
		
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
	@Path("/videototext")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "analyzes media file", response=String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "File Analyzed"),
	   })
	public String videoToText(@ApiParam(name="responseid") @FormParam("responseid") Long responseId) {
		Response response = responseRepository.findOne(responseId);
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
	@Path("/respondantsentiment")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "analyzes text", response=RespondantScore.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "File Analyzed"),
	   })
	public RespondantScore respondantSentiment(@ApiParam(name="respondantId") @FormParam("respondantId") Long respondantId) {		
		Set<RespondantNVP> nvps = respondantService.getNVPsForRespondant(respondantId);
		log.debug("Found {} nvps for respondant #{}", nvps.size(), respondantId);
		StringBuffer text = new StringBuffer();
		for (RespondantNVP nvp : nvps) {
			if ((nvp.getNameId() == 149L) || (nvp.getId() == 149L) || (nvp.getId() == 149L)) {
				text.append(nvp.getValue());		
			}
		}
		String allText = text.toString();
		
		if (allText.isEmpty()) return null;
		Double score = textAnalyticsService.analyzeSentiment(allText);
		log.debug("Respondant {} has sentiment score of: {}", respondantId, score);
		Corefactor sentiment = corefactorService.getByForeignId(WATSON_SENTIMENT);
		RespondantScorePK pk = new RespondantScorePK(sentiment.getId(),respondantId);
		RespondantScore rs = new RespondantScore();
		rs.setId(pk);
		rs.setValue(score);
		rs.setQuestionCount(3);	
		RespondantScore saved = respondantService.save(rs);
		
		return saved;
	}	
	
	@POST
	@Path("/sentimentfromtext")	
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "analyzes text", response=Double.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "File Analyzed"),
	   })
	public Double sentimentFromText(@ApiParam(name="text") @FormParam("text") String text) {

		Double score = textAnalyticsService.analyzeSentiment(text);
		
		return score;
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