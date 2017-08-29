package com.talytica.integration.scoring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.Response;
import com.employmeo.data.model.ScoringModelType;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NoScoring implements ScoringModelEngine {
	

	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = new ArrayList<RespondantScore>(); 
		log.debug("Scoring Model NONE: {} Responses not scored", responses.size());
		return scores;
	}

	@Override
	public String getModelName() {
		return ScoringModelType.NONE.getValue();
	}

	@Override
	public void initialize(String modelName) {

	}

}