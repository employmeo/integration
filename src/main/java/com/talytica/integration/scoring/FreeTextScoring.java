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
import com.talytica.common.service.TextAnalyticsService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FreeTextScoring implements ScoringModelEngine {
	
	@Autowired
	private QuestionService questionService;
	@Autowired
	private CorefactorService corefactorService;
	@Autowired
	private TextAnalyticsService textAnalyticsService;
	
	private String modelName = ScoringModelType.TRAIT.getValue();
	private final int MIN_TEXT_LENGTH = 50;
	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = new ArrayList<RespondantScore>(); 
		HashMap<Corefactor, List<String>> responseTable = new HashMap<Corefactor, List<String>>();

		responses.forEach(response -> {
			Question question = questionService.getQuestionById(response.getQuestionId());
			Corefactor corefactor = corefactorService.findCorefactorById(question.getCorefactorId());
			List<String> responseSet;
			if (responseTable.containsKey(corefactor)) {
				responseSet = responseTable.get(corefactor);
			} else {
				responseSet = new ArrayList<String>();
				responseTable.put(corefactor, responseSet);
			}
			responseSet.add(response.getResponseText());
		});
	
		for (Map.Entry<Corefactor, List<String>> pair : responseTable.entrySet()) {
			
			Corefactor corefactor = pair.getKey();
			List<String> responseSet = pair.getValue();
			StringBuffer sb = new StringBuffer();
			for (String response : responseSet) {
				sb.append(response);
				sb.append(" ");
			}
			if (sb.length() <= MIN_TEXT_LENGTH) continue; // because not enough text for sentiment
			Double percentage = textAnalyticsService.analyzeSentiment(sb.toString());
			RespondantScore rs = new RespondantScore();
			rs.setId(new RespondantScorePK(corefactor.getId(), respondant.getId()));
			rs.setQuestionCount(responseSet.size());
			rs.setValue(percentage *(corefactor.getHighValue()-corefactor.getLowValue())+corefactor.getLowValue());
			rs.setRespondant(respondant);
			log.debug("Corefactor {} scored with {} questons as {}: ", corefactor.getName(), rs.getQuestionCount(), rs.getValue());
			scores.add(rs);
		}
		
		// if type hexaco - calculate hexaco parent scores before returning collection of scores.
		 
		return scores;
	}

	@Override
	public String getModelName() {
		return this.modelName;
	}

	@Override
	public void initialize(String modelName) {
		this.modelName = modelName;
	}

}