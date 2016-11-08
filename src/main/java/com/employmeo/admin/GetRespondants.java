package com.employmeo.admin;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.objects.Respondant;
import com.employmeo.objects.User;
import com.employmeo.util.DBUtil;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("getrespondants")
public class GetRespondants {
	
	private static final long ONE_DAY = 24*60*60*1000; // one day in milliseconds
	private static final Logger log = LoggerFactory.getLogger(GetRespondants.class);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String doPost(@Context final HttpServletRequest reqt, @Context final HttpServletResponse resp,
			@DefaultValue("-1") @FormParam("respondant_status_low") int statusLow,
			@DefaultValue("99") @FormParam("respondant_status_high") int statusHigh,
			@DefaultValue("-1") @FormParam("location_id") Long locationId,
			@DefaultValue("-1") @FormParam("position_id") Long positionId,
			@DefaultValue("2015-01-01") @FormParam("fromdate") String fromDate,
			@DefaultValue("2020-12-31") @FormParam("todate") String toDate) {
		JSONArray response = new JSONArray();

		HttpSession sess = reqt.getSession();
		User user = (User) sess.getAttribute("User");
		if (user == null) {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		} // else if (false) { //
			// {resp.setStatus(HttpServletResponse.SC_FORBIDDEN); return null;}

		log.debug("Fetching respondants");
		Timestamp from = new Timestamp(Date.valueOf(fromDate).getTime());
		Timestamp to = new Timestamp(Date.valueOf(toDate).getTime() + ONE_DAY);

		String locationSQL = "";
		String positionSQL = "";
		String statusSQL = "AND r.respondantStatus >= :statusLow AND r.respondantStatus <= :statusHigh ";
		
		if (locationId > -1)
			locationSQL = "AND r.respondantLocationId = :locationId ";
		if (positionId > -1)
			positionSQL = "AND r.respondantPositionId = :positionId ";


		EntityManager em = DBUtil.getEntityManager();
		String dateSQL = "AND r.respondantCreatedDate >= :fromDate AND r.respondantCreatedDate <= :toDate ";
		String sql = "SELECT r from Respondant r WHERE r.respondantAccountId = :accountId "
				+ locationSQL + positionSQL + statusSQL 
				+ dateSQL + "ORDER BY r.respondantCreatedDate DESC";
		TypedQuery<Respondant> query = em.createQuery(sql, Respondant.class);
		query.setParameter("accountId", user.getAccount().getAccountId());
		if (locationId > -1)
			query.setParameter("locationId", locationId);
		if (positionId > -1)
			query.setParameter("positionId", positionId);
		query.setParameter("statusLow", statusLow);
		query.setParameter("statusHigh", statusHigh);
		query.setParameter("fromDate", from);
		query.setParameter("toDate", to);

		List<Respondant> respondants = query.getResultList();
		for (int j = 0; j < respondants.size(); j++) {
			respondants.get(j).getAssessmentScore();
			JSONObject jresp = respondants.get(j).getJSON();
			jresp.put("scores", respondants.get(j).getAssessmentScore());
			jresp.put("detailed_scores", respondants.get(j).getAssessmentDetailedScore());
			jresp.put("position", respondants.get(j).getPosition().getJSON());

			response.put(jresp);
		}

		return response.toString();
	}

}