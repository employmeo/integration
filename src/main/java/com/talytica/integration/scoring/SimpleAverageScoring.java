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
public class SimpleAverageScoring implements ScoringModelEngine {
	
	@Autowired
	private QuestionService questionService;
	@Autowired
	private CorefactorService corefactorService;
	
	public double MAXVAL;
	public double MINVAL;
	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = new ArrayList<RespondantScore>(); 
		HashMap<Corefactor, List<Response>> responseTable = new HashMap<Corefactor, List<Response>>();

		responses.forEach(response -> {
			Question question = questionService.getQuestionById(response.getQuestionId());
			Corefactor corefactor = corefactorService.findCorefactorById(question.getCorefactorId());
			List<Response> responseSet;
			if (responseTable.containsKey(corefactor)) {
				responseSet = responseTable.get(corefactor);
			} else {
				responseSet = new ArrayList<Response>();
				responseTable.put(corefactor, responseSet);
			}
			responseSet.add(response);
		});

		
		for (Map.Entry<Corefactor, List<Response>> pair : responseTable.entrySet()) {
			Corefactor corefactor = pair.getKey();
			List<Response> responseSet = pair.getValue();
			double total = 0;
			for (Response response : responseSet) {
				total += response.getResponseValue();
			}
			double percentage = (double) (total - ((double) responseSet.size() * MINVAL )) / ((double) responseSet.size() * (MAXVAL-MINVAL));
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
		return ScoringModelType.AVERAGE.getValue();
	}

	@Override
	public void initialize(String modelName) {
		switch (modelName) {
		case "likertfive":
			MAXVAL = 10;
			MINVAL = 0;
			break;
		case "slidersixty":
		case "knockout":
			MINVAL = 0;
			MAXVAL = 60;
			break;
		case "average":
		default:
			MAXVAL = 11;
			MINVAL = 1;
		}
	}

}