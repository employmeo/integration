package com.talytica.integration.resources;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONObject;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.common.service.EmailService;
import com.talytica.integration.util.ICIMSPartnerUtil;
import com.talytica.integration.util.PartnerUtil;;

@Path("icimsapplicationcomplete")
public class ICIMSApplicationComplete {

	@Context
	private SecurityContext sc;
	@Autowired
	PartnerRepository partnerRepository;
	@Autowired
	EmailService emailService;
	
	private static final Logger log = LoggerFactory.getLogger(ICIMSApplicationComplete.class);

	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPost(JSONObject json) {
		log.debug("ICIMS Application Complete with: " +json);
		
		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = new ICIMSPartnerUtil(partner);	
		
		Account account = pu.getAccountFrom(json);
		Respondant applicant = pu.createRespondantFrom(json, account);

		URI link = null;
		try {
			link = new URI(emailService.getAssessmentLink(applicant));
		} catch (Exception e) {
			log.warn("Failed to URI-ify link: " + emailService.getAssessmentLink(applicant));			
		}
		
		return Response.seeOther(link).build();

	}
}
