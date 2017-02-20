package com.talytica.integration.partners;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Partner;
import com.employmeo.data.service.PartnerService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.talytica.integration.IntegrationClientFactory;
import com.talytica.integration.objects.ATSAssessmentOrder;
import com.talytica.integration.objects.ATSStatusUpdate;
import com.talytica.integration.objects.JazzApplicantPollConfiguration;
import com.talytica.integration.objects.JazzHire;
import com.talytica.integration.objects.JazzJobApplicant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JazzPolling {
	
	@Autowired
	IntegrationClientFactory integrationClientFactory;

	@Autowired
	PartnerService partnerService;
	
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;
	
	/**
	 * Accepts a configuration object to pull applicants from Jazz and
	 * Place "ats-orders" to the integration server using integrationClient
	 * @param Configuration - contains account, statuses, date range
	 */
	public void pollJazzApplicants(JazzApplicantPollConfiguration configuration) {
		Partner jazz = partnerService.getPartnerById(configuration.getAccount().getAtsPartnerId());
		JazzPartnerUtil pu = (JazzPartnerUtil) partnerUtilityRegistry.getUtilFor(jazz);
		String accountApiKey = pu.trimPrefix(configuration.getAccount().getAtsId());		
		
		String applicantsServiceEndpoint = "applicants/from_apply_date/" + configuration.getLookbackBeginDate() + "/to_apply_date/" + configuration.getLookbackEndDate();
		Set<JazzJobApplicant> applicants = unpageRequest(pu, applicantsServiceEndpoint, accountApiKey);

		log.debug("Retrieved {} new applicants", applicants.size());	
		
		List<ATSAssessmentOrder> orders = Lists.newArrayList();
		for(JazzJobApplicant applicant: applicants) {
			ATSAssessmentOrder order = new ATSAssessmentOrder(applicant, accountApiKey);
			//order.setEmail(configuration.getSendEmail());
			order.setEmail(Boolean.FALSE);
			orders.add(order);
		}
					
		orderAssessments(orders, jazz);
	}	


	/**
	 * Accepts a configuration object to pull applicants based on status from Jazz
	 * And place "ats-status-updates" to the integration server using integrationClient
	 * @param Configuration - contains account, statuses, date range
	 */	
	public void pollJazzApplicantsByStatus(JazzApplicantPollConfiguration configuration) {
		Partner jazz = partnerService.getPartnerById(configuration.getAccount().getAtsPartnerId());
		JazzPartnerUtil pu = (JazzPartnerUtil) partnerUtilityRegistry.getUtilFor(jazz);
		String accountApiKey = pu.trimPrefix(configuration.getAccount().getAtsId());
		
		Set<JazzJobApplicant> applicants = Sets.newHashSet();
		for (String status : configuration.getWorkFlowIds()) {
			String applicantsServiceEndpoint = "applicants/from_apply_date/" + configuration.getLookbackBeginDate() + "/to_apply_date/" + configuration.getLookbackEndDate() + "/status/" + status;
			applicants.addAll(unpageRequest(pu, applicantsServiceEndpoint, accountApiKey));//fetchJobApplicantsByStatus(status, configuration.getAccount(), configuration.getLookbackBeginDate(), configuration.getLookbackEndDate()));
		}
		
		log.debug("Retrieved invited status for {} applicants", applicants.size());	
		
		List<ATSStatusUpdate> updates = Lists.newArrayList();
		for(JazzJobApplicant applicant: applicants) {
			ATSStatusUpdate update = new ATSStatusUpdate(applicant, accountApiKey, configuration.getStatus(), new Date());
			updates.add(update);
		}
				
		log.debug("Prepared {} ATS status update requests", updates.size());		
		postStatusUpdates(updates, jazz);
	}	
	
	/**
	 * Accepts a configuration object to pull applicants based on status from Jazz
	 * And place "ats-status-updates" to the integration server using integrationClient
	 * @param Configuration - contains account, statuses, date range
	 */	
	public void pollJazzHires(JazzApplicantPollConfiguration configuration) {
		Partner jazz = partnerService.getPartnerById(configuration.getAccount().getAtsPartnerId());
		JazzPartnerUtil pu = (JazzPartnerUtil) partnerUtilityRegistry.getUtilFor(jazz);
		String accountApiKey = pu.trimPrefix(configuration.getAccount().getAtsId());
		ObjectMapper mapper = new ObjectMapper();
		mapper = mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		String applicantsServiceEndpoint = "hires/";

		Range<Date> window = new Range<Date>(pu.getDateFrom(configuration.getLookbackBeginDate()), pu.getDateFrom(configuration.getLookbackEndDate()));
		
		boolean limit = true;
		int page = 1;
		Set<JazzHire> hirenotices = Sets.newHashSet();
		while (limit) {
			int counter = 0;
			try {
				String applicantsServiceResponse = pu.jazzGet(applicantsServiceEndpoint+"page/"+page, accountApiKey, null);		
				if (null != applicantsServiceResponse) {
					List<JazzHire> allhires = Lists.newArrayList();
					try {
						allhires = mapper.readValue(
								applicantsServiceResponse,
								new TypeReference<List<JazzHire>>() {
								});
					} catch(Exception e) {
						log.warn("Failed to deserialize fetchApplicants api response to a collection, will try as single object next.", e);
						JazzHire singleHire = mapper.readValue(applicantsServiceResponse, JazzHire.class);
						allhires.add(singleHire);
					}
					for (JazzHire hire : allhires) {
						if (window.contains(hire.getHired_date())) {
							counter++;
							hirenotices.add(hire);
						} else {
							log.debug("ignored everyone prior to: {}", hire.getHired_date());
							break;
						}
					}
				}
			} catch (Exception e) {
				log.warn("Failed to retrieve/process applicants from Jazz API service", e.getMessage());
			}
			if (counter < 99) limit = false;
			page++;
		}
				
		log.debug("Retrieved hire notice for {} hires", hirenotices.size());	
		
		List<ATSStatusUpdate> updates = Lists.newArrayList();
		for(JazzHire hire: hirenotices) {
			ATSStatusUpdate update = new ATSStatusUpdate(hire, accountApiKey);
			updates.add(update);
		}
				
		log.debug("Prepared {} ATS status update requests", updates.size());		
		postStatusUpdates(updates, jazz);
	}		
	
	public Set<JazzJobApplicant> unpageRequest(JazzPartnerUtil pu, String endpoint, String accountApiKey) {
		boolean limit = true;
		int page = 1;
		Set<JazzJobApplicant> applicants = Sets.newHashSet();
		while (limit) {
			Set<JazzJobApplicant> applicantsPage = Sets.newHashSet();		
			try {
				String applicantsServiceResponse = pu.jazzGet(endpoint+"/page/"+page, accountApiKey, null);
	
				if (null != applicantsServiceResponse) {
					applicantsPage = parseApplicants(applicantsServiceResponse);
				}
	
			} catch (Exception e) {
				log.warn("Failed to retrieve/process applicants from Jazz API service", e.getMessage());
			}
			if (applicantsPage.size() < 100) limit = false;
			applicants.addAll(applicantsPage);
			page++;
		}
		return applicants;
	}
	
	Set<JazzJobApplicant> parseApplicants(String applicantsServiceResponse)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper = mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		Set<JazzJobApplicant> applicants = Sets.newHashSet();
		List<JazzJobApplicant> partnerApplicants = Lists.newArrayList();
		try {
			partnerApplicants = mapper.readValue(
					applicantsServiceResponse,
					new TypeReference<List<JazzJobApplicant>>() {
					});
		} catch(Exception e) {
			log.warn("Failed to deserialize getHires api response to a collection, will try as single object next.", e.getMessage());
			
			JazzJobApplicant singleApplicant = mapper.readValue(applicantsServiceResponse, JazzJobApplicant.class);
			partnerApplicants.add(singleApplicant);
		}

		log.trace("Parsed Jazz applicants: {}", partnerApplicants);
		applicants.addAll(partnerApplicants);
		return applicants;
	}


	private void orderAssessments(List<ATSAssessmentOrder> orders, Partner jazz) {
		log.debug("Ordering assessments...");

		Client client = integrationClientFactory.newInstance(jazz.getLogin(), jazz.getPassword());
		String serverHost = integrationClientFactory.getServer();
		String orderAssessmentsUriPath = "/integration/atsorder";
		String targetPath = serverHost + orderAssessmentsUriPath;

		int counter = 0;
		int ordered = 0;
		for (ATSAssessmentOrder order : orders) {
			String applicantName = "'" + order.getFirst_name() + " " + order.getLast_name() + "'";
			log.info("Placing order #{} for {}", ++counter, applicantName);

			WebTarget target = client.target(targetPath);
			try {
				Response response = target
									.request(new String[] { "application/json" })
									.post(Entity.entity(order, "application/json"));
				Boolean success = (null == response || response.getStatus() >= 300) ? false : true;
				String serviceResponse = response.readEntity(String.class); 
				if (success) {
					log.debug("Posted ATS order to integration server for applicant {}", applicantName);
					ordered++;
				} else {
					log.warn("Failed to post ATS order successfully for {}. Server response: {}", applicantName, serviceResponse);
				}
			} catch(Exception e) {
				log.warn("Failed to post ATS order to integration server: {}", order, e );
			}
		}
		log.info("Placed {} of {} ATS order requests complete.", ordered, orders.size());

		try {
			client.close();
			log.debug("Integration client closed successfully");
		} catch (Exception e) {
			log.warn("Failed to close integrationClient cleanly. Watch for leaks", e);
		}

	}

	private void postStatusUpdates(List<ATSStatusUpdate> updates, Partner jazz) {
		log.debug("Updating Statuses...");

		Client client = integrationClientFactory.newInstance(jazz.getLogin(), jazz.getPassword());
		String serverHost = integrationClientFactory.getServer();
		String orderAssessmentsUriPath = "/integration/hirenotice";
		String targetPath = serverHost + orderAssessmentsUriPath;

		int counter = 0;
		int saved = 0;
		for (ATSStatusUpdate update : updates) {
			log.info("Attempting update #{}", ++counter);

			WebTarget target = client.target(targetPath);
			try {
				Response response = target
									.request(new String[] { "application/json" })
									.post(Entity.entity(update, "application/json"));
				Boolean success = (null == response || response.getStatus() >= 300) ? false : true;
				String serviceResponse = response.readEntity(String.class); 
				if (success) {
					log.debug("Posted status update {} succeeded", counter);
					saved++;
				} else {
					log.warn("Status not updated. Server response: {}", serviceResponse);
				}
			} catch(Exception e) {
				log.warn("Failed to post ATS status update to integration server: {}", update, e );
			}
		}
		log.info("Updated {} of {} ATS requests complete.", saved, updates.size());

		try {
			client.close();
			log.debug("Integration client closed successfully");
		} catch (Exception e) {
			log.warn("Failed to close integrationClient cleanly. Watch for leaks", e);
		}

	}

}
