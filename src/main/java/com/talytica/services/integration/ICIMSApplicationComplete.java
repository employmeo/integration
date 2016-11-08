package com.talytica.services.integration;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONObject;

import com.employmeo.objects.Account;
import com.employmeo.objects.Partner;
import com.employmeo.objects.Respondant;
import com.employmeo.util.ExternalLinksUtil;
import com.employmeo.util.PartnerUtil;;

@Path("icimsapplicationcomplete")
public class ICIMSApplicationComplete {

	@Context
	private SecurityContext sc;
	private static final Logger log = LoggerFactory.getLogger(ICIMSApplicationComplete.class);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doPost(JSONObject json) {
		log.debug("ICIMS Application Complete with: " +json);
	
		PartnerUtil pu = PartnerUtil.getUtilFor((Partner) sc.getUserPrincipal());
		Account account = pu.getAccountFrom(json);
		Respondant applicant = pu.createRespondantFrom(json, account);
		applicant.refreshMe();

		URI link = null;
		try {
			link = new URI(ExternalLinksUtil.getAssessmentLink(applicant));
		} catch (Exception e) {
			log.warn("Failed to URI-ify link: " + ExternalLinksUtil.getAssessmentLink(applicant));			
		}
		
		return Response.seeOther(link).build();

	}
}
