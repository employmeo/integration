package com.talytica.integration.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Position;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.integration.util.DefaultPartnerUtil;
import com.talytica.integration.util.PartnerUtil;

import io.swagger.annotations.Api;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/getpositions")
@Api( value="/getpositions", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class GetPositionsResource {

	private final Response MISSING_REQUIRED_PARAMS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Missing Required Parameters' }").build();
	private static final Logger log = LoggerFactory.getLogger(GetPositionsResource.class);
	@Context
	private SecurityContext sc;
	@Autowired
	PartnerRepository partnerRepository;
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String doPost(JSONObject json) {
		log.debug("processing with: " + json.toString());
		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = new DefaultPartnerUtil(partner);
		Account account = null;

		try { // the required parameters
			account = pu.getAccountFrom(json.getJSONObject("account"));
		} catch (Exception e) {
			throw new WebApplicationException(e, MISSING_REQUIRED_PARAMS);
		}

		JSONArray response = new JSONArray();

		if (account.getPositions().size() > 0) {
			Set<Position> positions = account.getPositions();
			for (Position pos : positions) {
				JSONObject position = new JSONObject();
				position.put("position_name", pos.getPositionName());
				position.put("position_description", pos.getDescription());
				position.put("position_id", pos.getId());
				response.put(position);
			}
		}
		return response.toString();
	}
}