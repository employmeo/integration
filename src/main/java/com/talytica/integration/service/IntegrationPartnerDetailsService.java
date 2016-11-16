package com.talytica.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.Partner;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.integration.objects.PartnerPrincipal;

@Service
public class IntegrationPartnerDetailsService implements UserDetailsService {
	private static final Logger log = LoggerFactory.getLogger(IntegrationPartnerDetailsService.class);
	
	@Autowired
	private PartnerRepository partnerRepository;

    @Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
    	log.debug("User Lookup Attempt for: {}", login);
		Partner partner = partnerRepository.findByLogin(login);
		if (partner != null) {
	    	log.debug("Found User: {}", partner);
			return new PartnerPrincipal(partner);
		} else {
		    throw new UsernameNotFoundException(login);
		}
	}
    
}
