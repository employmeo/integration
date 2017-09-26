package com.talytica.integration.service;

import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.CustomWorkflow;
import com.employmeo.data.model.Respondant;
import com.talytica.common.service.EmailService;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class WorkflowService {

	@Autowired
	private EmailService emailService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

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

	
	public void executeInvitedWorkflows(Respondant respondant) {
		List<CustomWorkflow> workflows = respondant.getPosition().getCustomWorkflows();
		log.debug("WORKFLOW: {} workflows found", workflows.size());
		Collections.sort(workflows);
		for (CustomWorkflow workflow : workflows) {
			if ((null != workflow.getProfile()) && (!workflow.getProfile().equalsIgnoreCase(respondant.getProfileRecommendation()))) continue;
			if (!workflow.getActive()) continue;
			if ((null != workflow.getTriggerPoint()) &&( CustomWorkflow.TRIGGER_POINT_INVITATIONSENT == workflow.getTriggerPoint())) {
				switch (workflow.getType()) {
					case CustomWorkflow.TYPE_ATSUPDATE:
						if (respondant.getPartner() == null) break;
						PartnerUtil pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
						pu.changeCandidateStatus(respondant, workflow.getAtsId());
						log.debug("WORKFLOW: Changed respondant {} status to {}", respondant.getId(), workflow.getText());
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
	
	public void executeSubmittedWorkflows(Respondant respondant) {
		List<CustomWorkflow> workflows = respondant.getPosition().getCustomWorkflows();
		log.debug("WORKFLOW: {} workflows found", workflows.size());
		Collections.sort(workflows);
		for (CustomWorkflow workflow : workflows) {
			if ((null != workflow.getProfile()) && (!workflow.getProfile().equalsIgnoreCase(respondant.getProfileRecommendation()))) continue;
			if (!workflow.getActive()) continue;
			if ((null != workflow.getTriggerPoint()) &&( CustomWorkflow.TRIGGER_POINT_ASSESSMENT == workflow.getTriggerPoint())) {
				switch (workflow.getType()) {
					case CustomWorkflow.TYPE_ATSUPDATE:
						if (respondant.getPartner() == null) break;
						PartnerUtil pu = partnerUtilityRegistry.getUtilFor(respondant.getPartner());
						pu.changeCandidateStatus(respondant, workflow.getAtsId());
						log.debug("WORKFLOW: Changed respondant {} status to {}", respondant.getId(), workflow.getText());
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

	public void executePredictionWorkflows(Respondant respondant) {
		List<CustomWorkflow> workflows = respondant.getPosition().getCustomWorkflows();
		log.debug("WORKFLOW: {} workflows found", workflows.size());
		Collections.sort(workflows);
		for (CustomWorkflow workflow : workflows) {
			if ((null != workflow.getProfile()) && (!workflow.getProfile().equalsIgnoreCase(respondant.getProfileRecommendation()))) continue;
			if (!workflow.getActive()) continue;
			if ((null != workflow.getTriggerPoint()) &&( CustomWorkflow.TRIGGER_POINT_PREDICTION == workflow.getTriggerPoint())) {
				switch (workflow.getType()) {
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

	public void executeAdvPredictionWorkflows(Respondant respondant) {
		List<CustomWorkflow> workflows = respondant.getPosition().getCustomWorkflows();
		log.debug("WORKFLOW: {} workflows found", workflows.size());
		Collections.sort(workflows);
		for (CustomWorkflow workflow : workflows) {
			if ((null != workflow.getProfile()) && (!workflow.getProfile().equalsIgnoreCase(respondant.getProfileRecommendation()))) continue;
			if (!workflow.getActive()) continue;
			if ((null != workflow.getTriggerPoint()) &&( CustomWorkflow.TRIGGER_POINT_ADVPREDICTION == workflow.getTriggerPoint())) {
				switch (workflow.getType()) {
					case CustomWorkflow.TYPE_NOTIFY:
						respondant.setEmailRecipient(workflow.getText());
						emailService.sendResults(respondant);												
						log.debug("WORKFLOW: Sent notification email to {}", respondant.getEmailRecipient());
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