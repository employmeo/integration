package com.talytica.integration.scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.Question;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.RespondantScorePK;
import com.employmeo.data.model.Response;
import com.employmeo.data.service.CorefactorService;
import com.employmeo.data.service.QuestionService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NoScoring implements ScoringModelEngine {
	

	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = new ArrayList<RespondantScore>(); 

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