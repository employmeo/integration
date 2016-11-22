package com.talytica.integration.util;

import org.json.JSONObject;

import com.employmeo.data.model.*;


public interface PartnerUtil {

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
