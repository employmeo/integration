package com.talytica.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class AddressService {
	
	@Value("https://maps.googleapis.com/maps/api/geocode/json")
	private String MAPS_SERVICE;
	private static final Logger log = LoggerFactory.getLogger(AddressService.class);

	@Value("${com.talytica.apis.googlemaps}")
	private String googleApiKey = System.getenv("GOOGLE_MAPS_KEY");

	public String getMapsKey() {
		return googleApiKey;
	}

	public void validate(JSONObject address) {
		String formattedAddress = address.optString("street") +
				" " + address.optString("city") + 
				", " + address.optString("state") + 
				" " + address.optString("zip");
		address.put("formatted_address", formattedAddress);
		
		if (address.has("lat") && address.has("lng")) return;

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(MAPS_SERVICE).queryParam("key", getMapsKey());
		address.put("formatted_address", formattedAddress);
		try {
			;
			String result = target.queryParam("address", formattedAddress).request(MediaType.APPLICATION_JSON)
					.get(String.class);
			JSONObject json = new JSONObject(result);
			JSONArray results = json.getJSONArray("results");
			if (results.length() != 1) {
				// TODO - either multiple results, or no result. error handling
				// needed?
				log.warn("Address retrieved " + results.length() + "results: " + formattedAddress);
			} else {
				address.put("formatted_address", results.getJSONObject(0).getString("formatted_address"));
				JSONObject geo = results.getJSONObject(0).getJSONObject("geometry");
				address.put("lat", geo.getJSONObject("location").getDouble("lat"));
				address.put("lng", geo.getJSONObject("location").getDouble("lng"));
				address.put("results", results.getJSONObject(0));
			}
		} catch (Exception e) {
			// TODO failed to validate address with lat & lng
			log.warn(e.getMessage() + " (lookup failed) for: " + formattedAddress);
		}

	}

	public JSONObject getAddressFromLatLng(double lat, double lng) {

		String latLng = lat + "," + lng;

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("https://maps.googleapis.com/maps/api/geocode/json").queryParam("key",
				getMapsKey());
		JSONObject address = new JSONObject();
		try {
			String result = target.queryParam("latlng", latLng).request(MediaType.APPLICATION_JSON).get(String.class);
			JSONObject json = new JSONObject(result);
			JSONArray results = json.getJSONArray("results");
			if (results.length() != 1) {
				// TODO handle when zero or multiple addresses hit with lat &
				// lng
				System.out.println("Multiple Options Found!!!");
			} else {
				address.put("formatted_address", results.getJSONObject(0).getString("formatted_address"));
				JSONObject geo = results.getJSONObject(0).getJSONObject("geometry");
				address.put("lat", geo.getJSONObject("location").getString("lat"));
				address.put("lng", geo.getJSONObject("location").getString("lng"));
			}
		} catch (Exception bre) {
			// TODO handle when failed to find an address with lat & lng
		}
		return address;
	}
}
