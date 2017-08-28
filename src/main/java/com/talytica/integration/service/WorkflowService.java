package com.talytica.integration.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.CustomWorkflow;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.model.RespondantScore;
import com.employmeo.data.repository.RespondantScoreRepository;
import com.employmeo.data.service.RespondantService;
import com.talytica.common.service.EmailService;
import com.talytica.integration.objects.GradingResult;
import com.talytica.integration.objects.PredictionResult;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class WorkflowService {

	@Autowired
	private RespondantService respondantService;
	@Autowired
	private ScoringService scoringService;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private PredictionService predictionService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;
	@Autowired
	private RespondantScoreRepository respondantScoreRepository;


	public void executePreScreenWorkflows(Respondant respondant) {
		List<CustomWorkflow> workflows = respondant.getPosition().getCustomWorkflows();
		log.debug("WORKFLOW: {} workflows found", workflows.size());
		Collections.sort(workflows);
		PartnerUtil pu = null;
		for (CustomWorkflow workflow : workflows) {
			if ((null != workflow.getProfile()) && (!workflow.getProfile().equalsIgnoreCase(respondant.getProfileRecommendation()))) continue;
			if (!workflow.getActive()) continue;
			if ((null != workflow.getTriggerPoint()) &&( CustomWorkflow.TRIGGER_POINT_CREATION == workflow.getTriggerPoint())) {
				switch (workflow.getType()) {
					case CustomWorkflow.TYPE_ATSUPDATE:
						if (respondant.getPartner() == null) break;
						pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
						pu.changeCandidateStatus(respondant, workflow.getAtsId());
						log.debug("WORKFLOW: Changed respondant {} status to {}", respondant.getId(), workflow.getText());
						break;
					case CustomWorkflow.TYPE_EMAIL:
						if (respondant.getPartner() != null) {
							pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
							pu.inviteCandidate(respondant);
						} else {
							emailService.sendEmailInvitation(respondant);												
						}
						log.debug("WORKFLOW: Sent email to respondant {}", respondant.getId());
						respondant.setRespondantStatus(Respondant.STATUS_INVITED);
						break;
					default:
						log.warn("WORKFLOW: No action at creation trigger point for: {}", workflow);
						break;
				}
			} else {
				log.debug("Different Trigger Point: {}", workflow.getTriggerPoint());
			}
		}	
	}

	public void executeAdvanceWorkflows(Respondant respondant) {
		List<CustomWorkflow> workflows = respondant.getPosition().getCustomWorkflows();
		log.debug("WORKFLOW: {} workflows found", workflows.size());
		Collections.sort(workflows);
		PartnerUtil pu = null;
		for (CustomWorkflow workflow : workflows) {
			if ((null != workflow.getProfile()) && (!workflow.getProfile().equalsIgnoreCase(respondant.getProfileRecommendation()))) continue;
			if (!workflow.getActive()) continue;
			if ((null != workflow.getTriggerPoint()) &&( CustomWorkflow.TRIGGER_POINT_ADVANCE == workflow.getTriggerPoint())) {
				switch (workflow.getType()) {
					case CustomWorkflow.TYPE_EMAIL:
						if (respondant.getPartner() != null) {
							pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
							pu.inviteCandidate(respondant);
						} else {
							emailService.sendEmailInvitation(respondant);												
						}
						log.debug("WORKFLOW: Sent email to respondant {}", respondant.getId());
						respondant.setRespondantStatus(Respondant.STATUS_ADVINITIATED);
						break;
					default:
						log.warn("WORKFLOW: No action at creation trigger point for: {}", workflow);
						break;
				}
			} else {
				log.debug("Different Trigger Point: {}", workflow.getTriggerPoint());
			}
		}	
	}

}
