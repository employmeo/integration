package com.talytica.integration.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Partner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PartnerUtilityRegistry {

	@Autowired
	private ApplicationContext applicationContext;

	public PartnerUtil getUtilFor(Partner lookupPartner) {
		log.debug("Request for partner utility for {}", lookupPartner.getPartnerName());
		PartnerUtil util = null;

		if ("ICIMS".equalsIgnoreCase(lookupPartner.getPartnerName())) {
			ICIMSPartnerUtil icimsUtil = applicationContext.getBean(ICIMSPartnerUtil.class);
			icimsUtil.setPartner(lookupPartner);
			util = icimsUtil;
			log.debug("Returning ICIMSPartnerUtil for partner {}", lookupPartner.getPartnerName());
		} else {
			DefaultPartnerUtil defaultUtil = applicationContext.getBean(DefaultPartnerUtil.class);
			defaultUtil.setPartner(lookupPartner);
			util = defaultUtil;
			log.debug("Returning DefaultPartnerUtil for partner {}", lookupPartner.getPartnerName());
		}

		return util;
	}
}
