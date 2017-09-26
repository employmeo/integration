package com.talytica.integration.partners.greenhouse;

import java.util.Date;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Position;
import com.employmeo.data.service.PartnerService;
import com.google.common.collect.Lists;
import com.talytica.integration.IntegrationClientFactory;
import com.talytica.integration.partners.GreenhousePartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GreenhousePolling {
	
	@Autowired
	IntegrationClientFactory integrationClientFactory;

	@Autowired
	PartnerService partnerService;
	
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;
	
	/**
	 * Accepts a configuration object to pull applicants from Jazz and
	 * Place "ats-orders" to the integration server using integrationClient
	 * @param Configuration - contains Account, Position, date range
	 */
	public List<GreenhouseApplication> getGreenhousePastCandidates(Account account, Position position, Range<Date> dates) {
		Partner greenhouse = partnerService.getPartnerById(account.getAtsPartnerId());
		GreenhousePartnerUtil pu = (GreenhousePartnerUtil) partnerUtilityRegistry.getUtilFor(greenhouse);
		List<GreenhouseApplication> applicants = Lists.newArrayList();

		Client client = pu.getPartnerClient();
		
		boolean limit = true;
		int page = 1;
		int perPage = 500;
		while (limit) {
			List<GreenhouseApplication> applicantsPage = client.target("https://harvest.greenhouse.io/v1/applications")
					.queryParam("created_after", dates.getLowerBound())
					.queryParam("created_before", dates.getUpperBound())
					.queryParam("page", page)
					.queryParam("per_page", perPage)
					.queryParam("job_id", position.getAtsId()).request()
					.get(new GenericType<List<GreenhouseApplication>>(){});
			if (applicantsPage.size() < 100) limit = false;
			applicants.addAll(applicantsPage);
			page++;	
		}				
		
		client.close();

		log.debug("Retrieved {} new applicants", applicants.size());
		return applicants;
	}
	
}
