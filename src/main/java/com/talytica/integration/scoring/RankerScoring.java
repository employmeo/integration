package com.talytica.integration.scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Answer;
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
public class RankerScoring implements ScoringModelEngine {
	
	@Autowired
	private QuestionService questionService;
	@Autowired
	private CorefactorService corefactorService;
	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = new ArrayList<RespondantScore>(); 
		HashMap<Corefactor, List<Double>> rankings = new HashMap<Corefactor, List<Double>>();
		
		responses.forEach(response -> {
			Question question = questionService.getQuestionById(response.getQuestionId());
			Set<Answer> answers = question.getAnswers();
			String[] priorities = Integer.toString(response.getResponseValue()).split("(?!^)");
			for (int i=0; i<priorities.length; i++) {				
				Integer value = Integer.valueOf(priorities[i]);
				Optional<Answer> answer = answers.stream().filter(ans -> value.equals(ans.getAnswerValue())).findFirst();
				if (answer.isPresent()) {
					Corefactor corefactor = corefactorService.findCorefactorById(answer.get().getCorefactorId());
					List<Double> ranks;
					if (rankings.containsKey(corefactor)) {
						ranks = rankings.get(corefactor);
					} else {
						ranks = new ArrayList<Double>();
						rankings.put(corefactor, ranks);
					}
					ranks.add(new Double((double) i / (double) (priorities.length-1)));
				}
			}
		});

		for (Map.Entry<Corefactor, List<Double>> pair : rankings.entrySet()) {
			Corefactor corefactor = pair.getKey();
			List<Double> ranks = pair.getValue();
			double total = 0;
			for (Double rank : ranks) {
				total += rank;
			}
			double percentage = total / (double) ranks.size();
			RespondantScore rs = new RespondantScore();
			rs.setId(new RespondantScorePK(corefactor.getId(), respondant.getId()));
			rs.setQuestionCount(responses.size());
			rs.setValue(percentage * (double)(corefactor.getHighValue()-corefactor.getLowValue())+corefactor.getLowValue());
			log.debug("Corefactor {} scored as {}: ", corefactor.getName(), rs.getValue());
			rs.setRespondant(respondant);
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

	}

}