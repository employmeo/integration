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
public class KnockoutScoring implements ScoringModelEngine {
	
	@Autowired
	private QuestionService questionService;
	@Autowired
	private CorefactorService corefactorService;
	
	public double THRESHOLD;
	private String modelName;
	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = new ArrayList<RespondantScore>(); 
		HashMap<Corefactor, List<Double>> responseTable = new HashMap<Corefactor, List<Double>>();

		responses.forEach(response -> {
			Question question = questionService.getQuestionById(response.getQuestionId());
			Corefactor corefactor = corefactorService.findCorefactorById(question.getCorefactorId());
			List<Double> responseSet;
			if (responseTable.containsKey(corefactor)) {
				responseSet = responseTable.get(corefactor);
			} else {
				responseSet = new ArrayList<Double>();
				responseTable.put(corefactor, responseSet);
			}
			double value = 0d; // value 0 means not paying attention or exaggerating answer.
			
			// if direction is negative, that means we want them to give a low score. 
			if ((question.getDirection()<0) && ((100 - response.getResponseValue()) >= THRESHOLD)) value = 1d;			
			// if direction is positive, that means we want them to answer with a high score.
			if ((question.getDirection()>=0) && (response.getResponseValue() >= THRESHOLD)) value = 1d;

			responseSet.add(value);
		});
	
		for (Map.Entry<Corefactor, List<Double>> pair : responseTable.entrySet()) {
			Corefactor corefactor = pair.getKey();
			List<Double> responseSet = pair.getValue();
			double total = 0;
			for (Double response : responseSet) total += response;
			double percentage = total / (double) responseSet.size();
			RespondantScore rs = new RespondantScore();
			rs.setId(new RespondantScorePK(corefactor.getId(), respondant.getId()));
			rs.setQuestionCount(responseSet.size());
			rs.setValue(percentage *(corefactor.getHighValue()-corefactor.getLowValue())+corefactor.getLowValue());
			rs.setRespondant(respondant);
			log.debug("Corefactor {} scored with {} questons as {}: ", corefactor.getName(), rs.getQuestionCount(), rs.getValue());
			scores.add(rs);
		}
		
		return scores;
	}

	@Override
	public String getModelName() {
		return this.modelName;
	}

	@Override
	public void initialize(String modelName) {
		this.modelName = modelName;
		switch (modelName) {
		case "deception":
			THRESHOLD = 15;
			break;
		case "knockout":
		default:
			THRESHOLD = 85;
			break;
		}
	}

}