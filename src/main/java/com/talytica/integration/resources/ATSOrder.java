package com.talytica.integration.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Respondant;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.integration.objects.PartnerPrincipal;
import com.talytica.integration.util.DefaultPartnerUtil;
import com.talytica.integration.util.PartnerUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/atsorder")
@Api( value="/atsorder", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class ATSOrder {

	@Context
	private SecurityContext sc;
	@Autowired
	PartnerRepository partnerRepository;

	private static final Logger log = LoggerFactory.getLogger(ATSOrder.class);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Orders an assessment for a particular respondant", response = String.class)
	   @ApiResponses(value = {
	     @ApiResponse(code = 201, message = "Order Processed"),
	   })	
	public String doPost(JSONObject json) {
		log.debug("ATS Requesting Assessment with: " + json.toString());

		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = new DefaultPartnerUtil(partner);
		
		Account account = pu.getAccountFrom(json.getJSONObject("account"));
		Respondant respondant = pu.createRespondantFrom(json, account);
		JSONObject output = pu.prepOrderResponse(json, respondant);

		log.debug("ATS Request for Assessment Complete: " + respondant.getAtsId());
		return output.toString();
	}

}