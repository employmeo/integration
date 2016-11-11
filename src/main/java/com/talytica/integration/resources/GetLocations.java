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

import com.employmeo.objects.Account;
import com.employmeo.objects.Location;
import com.employmeo.objects.Partner;
import com.employmeo.util.PartnerUtil;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("getlocations")
public class GetLocations {

	private final Response MISSING_REQUIRED_PARAMS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Missing Required Parameters' }").build();
	private static final Logger log = LoggerFactory.getLogger(GetLocations.class);
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

		if (account.getLocations().size() > 0) {
			List<Location> locations = account.getLocations();
			for (int i = 0; i < locations.size(); i++) {
				JSONObject location = new JSONObject();
				location.put("location_name", locations.get(i).getLocationName());
				if (locations.get(i).getLocationAtsId() != null)
					location.put("location_ats_id", pu.trimPrefix(locations.get(i).getLocationAtsId()));
				location.put("location_id", locations.get(i).getLocationId());
				response.put(location);
			}
		}
		return response.toString();
	}
}