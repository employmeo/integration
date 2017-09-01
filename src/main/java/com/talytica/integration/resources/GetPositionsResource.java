package com.talytica.integration.resources;

import java.util.Set;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.service.PartnerService;
import com.talytica.integration.partners.PartnerUtil;
import com.talytica.integration.partners.PartnerUtilityRegistry;

import io.swagger.annotations.Api;

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
	PartnerService partnerService;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String doPost(String body) throws JSONException  {
		JSONObject json = new JSONObject(body);
		log.debug("processing with: " + json.toString());
		Partner partner = partnerService.getPartnerByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
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