package com.talytica.integration.partners;

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
	public Respondant getRespondantFrom(JSONObject applicant, Account account);
	public Respondant createRespondantFrom(JSONObject json, Account account);
	public JSONObject prepOrderResponse(JSONObject json, Respondant respondant);
	public JSONObject getScoresMessage(Respondant respondant);
	public String getScoreNotesFormat(Respondant respondant);
	public JSONObject getScreeningMessage(Respondant respondant);
	public void inviteCandidate(Respondant respondant);
	public void changeCandidateStatus(Respondant respondant, String status);
	public void postScoresToPartner(Respondant respondant, JSONObject message);
	public void postScoresToPartner(String postmethod, JSONObject message);
}