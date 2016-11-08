package com.talytica.services.integration;

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

import com.employmeo.objects.Account;
import com.employmeo.objects.Partner;
import com.employmeo.objects.Position;
import com.employmeo.util.PartnerUtil;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("getpositions")
public class GetPositions {

	private final Response MISSING_REQUIRED_PARAMS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Missing Required Parameters' }").build();
	private static final Logger log = LoggerFactory.getLogger(GetPositions.class);
	@Context
	private SecurityContext sc;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String doPost(JSONObject json) {
		log.debug("processing with: " + json.toString());
		PartnerUtil pu = ((Partner) sc.getUserPrincipal()).getPartnerUtil();
		Account account = null;

		try { // the required parameters
			account = pu.getAccountFrom(json.getJSONObject("account"));
		} catch (Exception e) {
			throw new WebApplicationException(e, MISSING_REQUIRED_PARAMS);
		}

		JSONArray response = new JSONArray();

		if (account.getPositions().size() > 0) {
			List<Position> positions = account.getPositions();
			for (int i = 0; i < positions.size(); i++) {
				JSONObject position = new JSONObject();
				position.put("position_name", positions.get(i).getPositionName());
				position.put("position_description", positions.get(i).getPositionDescription());
				position.put("position_id", positions.get(i).getPositionId());
				response.put(position);
			}
		}
		return response.toString();
	}
}