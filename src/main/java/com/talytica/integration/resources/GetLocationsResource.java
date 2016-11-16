package com.talytica.integration.resources;

import java.util.Set;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.employmeo.data.model.*;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.integration.util.PartnerUtil;
import com.talytica.integration.util.PartnerUtilityRegistry;

import io.swagger.annotations.Api;

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/getlocations")
@Api( value="/getlocations", produces=MediaType.APPLICATION_JSON, consumes=MediaType.APPLICATION_JSON)
public class GetLocationsResource {

	private final Response MISSING_REQUIRED_PARAMS = Response.status(Response.Status.BAD_REQUEST)
			.entity("{ message: 'Missing Required Parameters' }").build();
	private static final Logger log = LoggerFactory.getLogger(GetLocationsResource.class);
	@Context
	private SecurityContext sc;
	@Autowired
	PartnerRepository partnerRepository;
	@Autowired
	private PartnerUtilityRegistry partnerUtilityRegistry;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String doPost(String body) {
		JSONObject json = new JSONObject(body);
		log.debug("processing with: {} ", json);
		Partner partner = partnerRepository.findByLogin(sc.getUserPrincipal().getName());
		PartnerUtil pu = partnerUtilityRegistry.getUtilFor(partner);
		Account account = null;

		try { // the required parameters
			account = pu.getAccountFrom(json.getJSONObject("account"));
		} catch (Exception e) {
			throw new WebApplicationException(e, MISSING_REQUIRED_PARAMS);
		}

		JSONArray response = new JSONArray();

		Set<Location> locations = account.getLocations();
		for (Location loc : locations) {
				JSONObject location = new JSONObject();
				location.put("location_name", loc.getLocationName());
				if (loc.getAtsId() != null) {
					location.put("location_ats_id", pu.trimPrefix(loc.getAtsId()));
				}
				location.put("location_id", loc.getId());
				response.put(location);
		}
		return response.toString();
	}
}