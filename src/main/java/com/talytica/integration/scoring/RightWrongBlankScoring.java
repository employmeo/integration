package com.talytica.integration.scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Corefactor;
import com.employmeo.data.model.Question;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.RespondantScorePK;
import com.employmeo.data.model.Response;
import com.employmeo.data.model.SurveyQuestion;
import com.employmeo.data.service.CorefactorService;
import com.employmeo.data.service.QuestionService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RightWrongBlankScoring implements ScoringModelEngine {
	
	@Autowired
	private QuestionService questionService;
	@Autowired
	private CorefactorService corefactorService;
	
	
	public int CORRECTVALUE = 11;
	public double POINTSBLANK = 0.3333d;
	public double POINTSWRONG = -0.3333d;
	public double POINTSCORRECT = 0.6667d;
	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = new ArrayList<RespondantScore>(); 
		Set<SurveyQuestion> questions = respondant.getAccountSurvey().getSurvey().getSurveyQuestions();
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
			double value = POINTSWRONG;
			if (response.getResponseValue() == CORRECTVALUE) value = POINTSCORRECT;
			responseSet.add(value);
		});
	
		for (Map.Entry<Corefactor, List<Double>> pair : responseTable.entrySet()) {
			Corefactor corefactor = pair.getKey();
			Long questionCount = questions.stream().filter(ques -> corefactor.getId().equals(ques.getQuestion().getCorefactorId())).count();
			List<Double> responseSet = pair.getValue();
			double total = POINTSBLANK * (double) questionCount;
			for (Double value : responseSet) {
				total += value;
			}
			double percentage = total / (double) questionCount;

			RespondantScore rs = new RespondantScore();
			rs.setId(new RespondantScorePK(corefactor.getId(), respondant.getId()));
			rs.setQuestionCount(responseSet.size());
			rs.setValue(percentage *(corefactor.getHighValue()-corefactor.getLowValue())+corefactor.getLowValue());
			rs.setRespondant(respondant);
			log.debug("Corefactor {} scored with {} responses of {} questons as {}: ", corefactor.getName(), rs.getQuestionCount(), questionCount, rs.getValue());
			scores.add(rs);
		}
		
		return scores;
	}

	@Override
	public String getModelName() {
		return ScoringModelType.RIGHTWRONGBLANK.getValue();
	}

	@Override
	public void initialize(String modelName) {

	}

}