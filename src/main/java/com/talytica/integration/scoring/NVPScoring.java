package com.talytica.integration.scoring;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Question;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.Response;
import com.employmeo.data.model.ScoringModelType;
import com.employmeo.data.service.QuestionService;
import com.employmeo.data.service.RespondantService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NVPScoring implements ScoringModelEngine {
	
	@Autowired
	RespondantService respondantService;
	@Autowired
	QuestionService questionService;

	private String modelName;
	private Boolean display;
	private Boolean inModel;

	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		List<RespondantScore> scores = new ArrayList<RespondantScore>(); 
		responses.forEach(response -> {
			Question question = questionService.getQuestionById(response.getQuestionId());
			String value = response.getResponseText();
			if ((null == value) || (value.isEmpty())) value = String.format("%d", response.getResponseValue());
			respondantService.addNVPToRespondant(respondant, question.getQuestionText(), value, inModel, display);
		});
		log.debug("Saved {} responses as NVPs", responses.size());
		return scores;
	}

	@Override
	public String getModelName() {
		return this.modelName;
	}

	@Override
	public void initialize(String modelName) {
		this.modelName = modelName;
		this.display = (ScoringModelType.CUSTOMHIDDEN.getValue() != this.modelName); // display unless model is hidden
		this.inModel = false;
	}
}