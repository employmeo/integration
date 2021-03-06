package com.talytica.integration.partners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Partner;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PartnerUtilityRegistry {

	@Autowired
	private ApplicationContext applicationContext;

	public PartnerUtil getUtilFor(@NonNull Partner lookupPartner) {
		log.trace("Request for partner utility for {}", lookupPartner.getPartnerName());
		PartnerUtil util = null;

		switch (lookupPartner.getPartnerName().toUpperCase()) {
		case "FOUNTAIN":
			FountainPartnerUtil fnUtil = applicationContext.getBean(FountainPartnerUtil.class);
			fnUtil.setPartner(lookupPartner);
			util = fnUtil;
			break;
		case "GREENHOUSE":
			GreenhousePartnerUtil ghUtil = applicationContext.getBean(GreenhousePartnerUtil.class);
			ghUtil.setPartner(lookupPartner);
			util = ghUtil;
			break;
		case "ICIMS":
			ICIMSPartnerUtil icimsUtil = applicationContext.getBean(ICIMSPartnerUtil.class);
			icimsUtil.setPartner(lookupPartner);
			util = icimsUtil;
		break;
		case "JAZZ":
			JazzPartnerUtil jazzUtil = applicationContext.getBean(JazzPartnerUtil.class);
			jazzUtil.setPartner(lookupPartner);
			util = jazzUtil;
			break;
		case "SMARTRECRUITERS":
			SmartRecruitersPartnerUtil srUtil = applicationContext.getBean(SmartRecruitersPartnerUtil.class);
			srUtil.setPartner(lookupPartner);
			util = srUtil;
			break;
		case "WORKABLE":
			WorkablePartnerUtil wkUtil = applicationContext.getBean(WorkablePartnerUtil.class);
			wkUtil.setPartner(lookupPartner);
			util = wkUtil;
			break;
		case "USACS-SFDC":
			SalesforcePartnerUtil sfUtil = applicationContext.getBean(SalesforcePartnerUtil.class);
			sfUtil.setPartner(lookupPartner);
			util = sfUtil;
			break;
		default:
			DefaultPartnerUtil defaultUtil = applicationContext.getBean(DefaultPartnerUtil.class);
			defaultUtil.setPartner(lookupPartner);
			util = defaultUtil;
			break;
		}

		log.trace("Returning {} for partner {}", util.getClass(), lookupPartner.getPartnerName());

		return util;
	}
	
}
