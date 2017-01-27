package com.talytica.integration.util;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.data.domain.Range;

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
	
	//public List<PartnerApplicant> fetchPartnerApplicants(String[] statuses, Optional<Range<Date>> period);

}
