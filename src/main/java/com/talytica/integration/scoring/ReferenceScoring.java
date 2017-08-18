package com.talytica.integration.scoring;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Grader;
import com.employmeo.data.model.Person;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.model.RespondantScorePK;
import com.employmeo.data.model.Response;
import com.employmeo.data.model.ScoringModelType;
import com.employmeo.data.service.GraderService;
import com.employmeo.data.service.PersonService;
import com.talytica.common.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ReferenceScoring implements ScoringModelEngine {

	private static final Long RANKER_CFID = 99l;
	
	@Autowired
	private PersonService personService;
	@Autowired
	private GraderService graderService;
	@Autowired
	private EmailService emailService;

	
	@Override
	public List<RespondantScore> scoreResponses(Respondant respondant, List<Response> responses) {
		Double referenceSent = 0d;
		List<RespondantScore> scores = new ArrayList<RespondantScore>();

		for (Response response : responses) {
			String email = response.getResponseText();
			Person person = new Person();
			if (email.contains("<")) {
				String[] strings = email.split("<");
				String fullname = strings[0].trim();
				if (fullname.contains(" ")) {
					String [] names = fullname.split(" ");
					person.setFirstName(names[0]);
					person.setLastName(names[1]);
				} else {
					person.setFirstName(fullname);
				}
				person.setEmail(strings[1].substring(0, strings[1].length()-1));
			} else {
				person.setEmail(email);				
			}
			Person reference = personService.save(person);

			Grader grader = new Grader();
			grader.setAccount(respondant.getAccount());
			grader.setAccountId(respondant.getAccountId());
			grader.setType(Grader.TYPE_PERSON);
			grader.setStatus(Grader.STATUS_NEW);
			grader.setPerson(reference);
			grader.setRespondantId(respondant.getId());
			grader.setRespondant(respondant);
			grader.setPersonId(reference.getId());
			grader.setResponse(response);
			grader.setResponseId(response.getId());
			grader.setQuestionId(response.getQuestionId());
			Grader savedGrader = graderService.save(grader);

			emailService.sendReferenceRequest(savedGrader);
			referenceSent++;
		}
		if (referenceSent>0) {
			RespondantScore ranker = new RespondantScore();
			ranker.setId(new RespondantScorePK(RANKER_CFID,respondant.getId()));
			ranker.setValue(referenceSent);
			scores.add(ranker); // Indicates that there are outstanding "graders"
		}
		
		log.debug("{} reference requests sent", referenceSent);
		return scores;
	}

	@Override
	public String getModelName() {
		return ScoringModelType.REFERENCE.getValue();
	}

	@Override
	public void initialize(String modelName) {
		// TODO Auto-generated method stub
		
	}

}
