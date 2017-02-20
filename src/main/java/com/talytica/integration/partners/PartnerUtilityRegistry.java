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

		if ("ICIMS".equalsIgnoreCase(lookupPartner.getPartnerName())) {
			ICIMSPartnerUtil icimsUtil = applicationContext.getBean(ICIMSPartnerUtil.class);
			icimsUtil.setPartner(lookupPartner);
			util = icimsUtil;
			log.trace("Returning ICIMSPartnerUtil for partner {}", lookupPartner.getPartnerName());
		} else if ("JAZZ".equalsIgnoreCase(lookupPartner.getPartnerName())) { 
			JazzPartnerUtil jazzUtil = applicationContext.getBean(JazzPartnerUtil.class);
			jazzUtil.setPartner(lookupPartner);
			util = jazzUtil;
			log.trace("Returning JazzPartnerUtil for partner {}", lookupPartner.getPartnerName());
		} else {
			DefaultPartnerUtil defaultUtil = applicationContext.getBean(DefaultPartnerUtil.class);
			defaultUtil.setPartner(lookupPartner);
			util = defaultUtil;
			log.trace("Returning DefaultPartnerUtil for partner {}", lookupPartner.getPartnerName());
		}

		return util;
	}
	
}
