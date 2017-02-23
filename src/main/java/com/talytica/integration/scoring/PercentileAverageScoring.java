package com.talytica.integration.scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
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
public class PercentileAverageScoring implements ScoringModelEngine {
	
	@Autowired
	private QuestionService questionService;
	@Autowired
	private CorefactorService corefactorService;
	
	private double MEAN;
	private double STDEV;
	private NormalDistribution normalDistribution;
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
			double value = (double)response.getResponseValue();
			responseSet.add(value);
		});
	
		for (Map.Entry<Corefactor, List<Double>> pair : responseTable.entrySet()) {
			Corefactor corefactor = pair.getKey();
			List<Double> responseSet = pair.getValue();
			double total = 0;
			for (Double response : responseSet) {
				total += response;
			}
			double average = total  / (double) responseSet.size();
			double percentile = normalDistribution.cumulativeProbability(average);
			RespondantScore rs = new RespondantScore();
			rs.setId(new RespondantScorePK(corefactor.getId(), respondant.getId()));
			rs.setQuestionCount(responseSet.size());
			rs.setValue(percentile *(corefactor.getHighValue()-corefactor.getLowValue())+corefactor.getLowValue());
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
		case "workingmem":
			MEAN = 5.3d;
			STDEV = 2.39d;
			break;	
		case "selective":
			MEAN = 19.57d;
			STDEV = 4.51d;
			break;
		case "reaction":
			MEAN = 25.65d;
			STDEV = 5.38d;
			break;
		default:
			MEAN = 6d;
			STDEV = 1.5d;
		}
		normalDistribution = new NormalDistribution(MEAN,STDEV);
	}

}