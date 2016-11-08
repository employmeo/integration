package com.employmeo.testing;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.objects.Person;
import com.employmeo.objects.User;
import com.employmeo.util.AddressUtil;

public class RandomizerUtil {

	// Basic Tool Settings
	public static JSONObject account = new JSONObject().put("account_ats_id", "1111");

	public static Random rand = new Random();
	public static JSONArray jMnames = arrayFromRandomizer("names-male.json");
	public static JSONArray jFnames = arrayFromRandomizer("names-female.json");
	public static JSONArray jLnames = arrayFromRandomizer("names-surnames.json");
	public static JSONArray jCities = arrayFromRandomizer("zip-codes.json");
	public static JSONArray jStreets = arrayFromRandomizer("streets.json");
	public static List<String> domains = Arrays.asList(
			  "company.com", "aol.com", "att.net", "comcast.net", "facebook.com", "gmail.com", "gmx.com", "googlemail.com",
			  "google.com", "hotmail.com", "hotmail.co.uk", "mac.com", "me.com", "mail.com", "msn.com",
			  "live.com", "sbcglobal.net", "verizon.net", "yahoo.com", "yahoo.co.uk",
			  "email.com", "games.com", "gmx.net", "hush.com", "hushmail.com", "icloud.com", "inbox.com",
			  "lavabit.com", "love.com", "outlook.com", "pobox.com", "rocketmail.com",
			  "safe-mail.net", "wow.com", "ygm.com", "ymail.com", "zoho.com", "fastmail.fm",
			  "yandex.com", "bellsouth.net", "charter.net", "comcast.net", "cox.net", "earthlink.net", "juno.com"
			  );

	public static synchronized JSONArray arrayFromRandomizer(String service) {
		Client client = ClientBuilder.newClient();
		String postmethod = "https://randomlists.com/data/" + service;
		WebTarget target = client.target(postmethod);
		String result = target.request(MediaType.APPLICATION_JSON).get(String.class);
		
		return new JSONObject(result).getJSONArray("data");

	}	
	
	public static String randomFname() {
		if(rand.nextDouble() > 0.5) {
			return jFnames.getString(rand.nextInt(jFnames.length()));
		}
		return jMnames.getString(rand.nextInt(jMnames.length()));

	}

	public static String randomLname() {
		return jLnames.getString(rand.nextInt(jLnames.length()));
	}

	public static String randomEmail(User user) {
		int i = rand.nextInt(domains.size());
		String email = user.getUserFname() + "_" + user.getUserLname() + "@" + domains.get(i);
		return email;
	}

	public static String randomEmail(Person person) {
		int i = rand.nextInt(domains.size());
		String email = person.getPersonFname() + "_" + person.getPersonLname() + "@" + domains.get(i);
		return email;
	}
	
	public static String randomEmail(String fname, String lname) {
		int i = rand.nextInt(domains.size());
		String email = fname + "_" + lname + "@" + domains.get(i);
		return email;
	}
	
	public static JSONObject randomAddress(double lat, double lng, double dist) {
		return AddressUtil.getAddressFromLatLng(lat, lng);
	}
	
	public static int randomResponse(JSONObject question) {
		
		if (question.has("answers")) {
			JSONObject answers = randomJson(question.getJSONArray("answers"));
			return answers.getInt("answer_value");
		}
		return rand.nextInt(11);
	}
	

	public static JSONObject randomJson(JSONArray array) {
		return array.getJSONObject(rand.nextInt(array.length()));
	}
	

	public static String randomStreet() {
		
		int strAdd = rand.nextInt(9999);
		return strAdd + " " + jStreets.getString(rand.nextInt(jStreets.length()));
	}
	
	public static void randomAddress(JSONObject address) {
		JSONObject zipCode = jCities.getJSONObject(rand.nextInt(jCities.length()));
		String cityState = zipCode.getString("detail");
		address.put("street", randomStreet());
		address.put("city", cityState.split(", ")[0]);
		address.put("state", cityState.split(", ")[1]);
		address.put("zip", zipCode.getString("name"));
		
	}
}
