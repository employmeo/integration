package com.talytica.integration.service;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.ResponseRepository;
import com.employmeo.data.service.CorefactorService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class BeyondVerbalService {
	
	@Value("${com.talytica.apis.beyondverbal.key}")
	private String beyondVerbalKey;
	private String beyondVerbalToken = null;
	private final String BV_API = "https://apiv3.beyondverbal.com/v3/recording";
	private final String START = "/start";
	private final String ANALYSIS = "/analysis";
	private final Long TEMPER_COREFACTOR = 10l; // Friendliness
	private final Long VALENCE_COREFACTOR = 24l; // Optimism
	private final Long AROUSAL_COREFACTOR = 75l; // Enthusiasm
	
	
	@Autowired
	private CorefactorService corefactorService;

	@Autowired
	private ResponseRepository responseRepository;
	
	public JSONObject analyzeResponse(Long responseId) {

		Response response = responseRepository.findOne(responseId);
		log.debug("New Analysis requested for: {}" , response);
		String recordingId = startAnalysis(response.getId());
		JSONObject json = analyzeMedia(response.getResponseMedia(), recordingId);

		if (recordingId != null) {
			response.setResponseText(recordingId);
			responseRepository.save(response);
		}
		
		return json;
		
	}	

	public String startAnalysis(Long responseId) {
		String recordingId = null;	
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(BV_API+START);
		try {
			JSONObject request = new JSONObject();
			JSONObject format = new JSONObject();
			JSONObject metadata = new JSONObject();
			format.put("type", "WAV");
			metadata.put("clientID", responseId);
			request.put("dataFormat", format);
			request.put("metadata", metadata);
			
			javax.ws.rs.core.Response result = target.request(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + getBeyondVerbalToken())
					.post(Entity.entity(request.toString(2), MediaType.APPLICATION_JSON));

			if (result.getStatus() >= 300) {
				log.error("Failed to call start API {}", result.getStatusInfo().getReasonPhrase());
			} else {
				String resp = result.readEntity(String.class);
				log.debug("Start Analysis request {} resulted in: \n {}",target.toString(), resp);
				JSONObject json = new JSONObject(resp);
				
				recordingId = json.optString("recordingId");
				
			}
		} catch (Exception e) {
			log.error("Failed to start analysis for response {}, with {} ", responseId, e.getMessage());
		}
			
		return recordingId;
	}
	

	public JSONObject analyzeMedia(String responseMedia, String recordingId) {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(BV_API+"/"+recordingId).register(MultiPartFeature.class);
		try {
			URL mediaUrl = new URL(responseMedia);
			javax.ws.rs.core.Response result = target.request(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + getBeyondVerbalToken())
			.post( Entity.entity(mediaUrl.openStream(),MediaType.APPLICATION_OCTET_STREAM_TYPE));
			
			if (result.getStatus() >= 300) {
				log.error("Failed to call analyze API {}", result.getStatusInfo().getReasonPhrase());
				log.error(result.readEntity(String.class));
			} else {
				String resp = result.readEntity(String.class);
				log.debug("Post Media request {} resulted in: \n {}",target.toString(), resp);
				JSONObject json = new JSONObject(resp);
				
				return json;
			}
		} catch (Exception e) {
			log.error("Failed to analyze {}, with {} ", recordingId, e.getMessage());
		}
		return null;		
	}
	
	public JSONObject getAnalysis(String recordingId) {
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(BV_API+"/"+recordingId+ANALYSIS);
		try {
			javax.ws.rs.core.Response result = target.request(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + getBeyondVerbalToken()).get();
			
			if (result.getStatus() >= 300) {
				log.error("Failed to call analyze API {}", result.getStatusInfo().getReasonPhrase());
				log.error(result.readEntity(String.class));
			} else {
				String resp = result.readEntity(String.class);
				log.debug("Get Analysis request {} resulted in: \n {}",target.toString(), resp);
				JSONObject json = new JSONObject(resp);
				return json;
			}
		} catch (Exception e) {
			log.error("Failed to retrive analysis for {}, with {} ", recordingId, e.getMessage());
		}
		return null;		
	}
	
	
	public List<RespondantScore> analyzeResponses(List<Response> responses) {
		List<RespondantScore> scores = Lists.newArrayList();
		Set<JSONObject> segments = Sets.newHashSet();
		
		for (Response response : responses) {
			JSONObject json = null;
			if (response.getResponseText() != null) {
				json = getAnalysis(response.getResponseText());
			}
			if (json == null) {
				log.debug("no recording id for {}", response);
				json = analyzeResponse(response.getId());
			}
			JSONObject result = json.getJSONObject("result");
			JSONArray analysisSegments = result.getJSONArray("analysisSegments");
			for (int i=0;i<analysisSegments.length();i++) {
				segments.add(analysisSegments.getJSONObject(i));
			}
		}
		log.debug("full output is: {}",segments);

		Double totalduration = 0d;
		Double tempertotal = 0d;
		Double valencetotal = 0d;
		Double arousaltotal = 0d;
		HashMap<Corefactor,Double> hashtable = Maps.newHashMap();
		
		for (JSONObject segment : segments) {
			//long offset = segment.getLong("offset");
			Double duration = segment.getDouble("duration");
			totalduration += duration;
			JSONObject analysis = segment.getJSONObject("analysis");
			JSONObject temper = analysis.getJSONObject("Temper");
			tempertotal += duration * temper.getDouble("Value");
			JSONObject valence = analysis.getJSONObject("Valence");
			valencetotal += duration * valence.getDouble("Value");
			JSONObject arousal = analysis.getJSONObject("Arousal");
			arousaltotal += duration * arousal.getDouble("Value");
			
			JSONObject primary = analysis.getJSONObject("Mood").getJSONObject("Group11").getJSONObject("Primary");
			Corefactor primaryCorefactor = corefactorService.getByForeignId("BV"+primary.getLong("Id"));
			Double factorDuration = 0d;			
			if (hashtable.containsKey(primaryCorefactor)) factorDuration = hashtable.get(primaryCorefactor);
			factorDuration += 2d * duration;
			hashtable.put(primaryCorefactor, factorDuration);
			
			JSONObject secondary = analysis.getJSONObject("Mood").getJSONObject("Group11").getJSONObject("Secondary");
			Corefactor secondaryCorefactor = corefactorService.getByForeignId("BV"+secondary.getLong("Id"));
			factorDuration = 0d;			
			if (hashtable.containsKey(secondaryCorefactor)) factorDuration = hashtable.get(secondaryCorefactor);
			factorDuration += 1.25d * duration;// half value if duration isn't where we want it.
			hashtable.put(secondaryCorefactor, factorDuration);
			
		}
		
		RespondantScore temperScore = new RespondantScore();
		temperScore.setId(new RespondantScorePK(TEMPER_COREFACTOR,responses.get(0).getRespondantId()));
		temperScore.setValue(tempertotal/(10*totalduration));
		temperScore.setQuestionCount(responses.size());
		scores.add(temperScore);
		
		RespondantScore valenceScore = new RespondantScore();
		valenceScore.setId(new RespondantScorePK(VALENCE_COREFACTOR,responses.get(0).getRespondantId()));
		valenceScore.setValue(valencetotal/(10*totalduration));
		valenceScore.setQuestionCount(responses.size());
		scores.add(valenceScore);

		RespondantScore arousalScore = new RespondantScore();
		arousalScore.setId(new RespondantScorePK(AROUSAL_COREFACTOR,responses.get(0).getRespondantId()));
		arousalScore.setValue(arousaltotal/(10*totalduration));
		arousalScore.setQuestionCount(responses.size());
		scores.add(arousalScore);
		
		for (Map.Entry<Corefactor, Double> pair : hashtable.entrySet()) {
			RespondantScore moodScore = new RespondantScore();
			Corefactor moodFactor = pair.getKey();	
			Double value = 10 * pair.getValue() / totalduration; // sort of scales to 10?
			moodScore.setId(new RespondantScorePK(moodFactor.getId(),responses.get(0).getRespondantId()));
			moodScore.setValue(Math.max(Math.min(10d, value),1d)); /// force range to 1 to 10;
			moodScore.setQuestionCount(responses.size());
			scores.add(moodScore);
		}

		return scores;
	}

	
	private String getBeyondVerbalToken() {
		
		if (this.beyondVerbalToken != null) return this.beyondVerbalToken;	
		log.debug("no token found");
		Form form = new Form();
		form.param("grant_type", "client_credentials");
		form.param("apikey", beyondVerbalKey);
		Client client = ClientBuilder.newClient();
		javax.ws.rs.core.Response result = client.target("https://token.beyondverbal.com/token")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
		
		JSONObject json = new JSONObject(result.readEntity(String.class));
		log.debug("token request returned: {}", json);		
		beyondVerbalToken = json.optString("access_token");
		return this.beyondVerbalToken;		
		
	}	
	
}
