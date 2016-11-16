package com.talytica.integration.util;

import java.util.HashMap;

import com.employmeo.data.model.Account;
import com.employmeo.data.model.AccountSurvey;
import com.employmeo.data.model.Location;
import com.employmeo.data.model.Partner;
import com.employmeo.data.model.Position;
import com.employmeo.data.model.Respondant;

import org.json.JSONObject;


public interface PartnerUtil {
	public static HashMap<Partner,PartnerUtil> utils = new HashMap<Partner,PartnerUtil>();
	
	public static PartnerUtil getUtilFor(Partner lookupPartner) {
		// TODO make this a little more dynamic for multiple partners, move to Partner object?
		if (!utils.containsKey(lookupPartner)) {
			if ("ICIMS".equalsIgnoreCase(lookupPartner.getPartnerName())) {
				utils.put(lookupPartner, new ICIMSPartnerUtil(lookupPartner));
			} else {
				utils.put(lookupPartner, new DefaultPartnerUtil(lookupPartner));
			}
		}
		return utils.get(lookupPartner);
	}
	
	public String getPrefix();
	public String addPrefix(String id);
	public String trimPrefix(String id);
	public Account getAccountFrom(JSONObject jAccount);
	public Location getLocationFrom(JSONObject jLocation, Account account);
	public Position getPositionFrom(JSONObject position, Account account);
	public AccountSurvey getSurveyFrom(JSONObject assessment, Account account);
	public Respondant getRespondantFrom(JSONObject applicant);
	public Respondant createRespondantFrom(JSONObject json, Account account);
	public JSONObject prepOrderResponse(JSONObject json, Respondant respondant);
	public JSONObject getScoresMessage(Respondant respondant);
	public void postScoresToPartner(Respondant respondant, JSONObject message);

}
