package com.employmeo.admin;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.employmeo.objects.Position;
import com.employmeo.objects.Respondant;
import com.employmeo.objects.User;

@Path("getscore")
public class GetRespondantScore {

	private static final Logger log = LoggerFactory.getLogger(GetRespondantScore.class);
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String doPost(
			@Context final HttpServletRequest reqt,
			@Context final HttpServletResponse resp,
			@FormParam("respondant_id") Long respondantId, 
			@FormParam("respondant_uuid") UUID respondantUuid) {

		JSONObject json = new JSONObject();
		
		HttpSession sess = reqt.getSession();
		User user = (User) sess.getAttribute("User");
		if (user == null) {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			json.put("message", "Access Restricted");
			return json.toString();
		} 
		
		Respondant respondant = null;
		if (respondantId != null) {
			respondant = Respondant.getRespondantById(respondantId);
		} else if (respondantUuid != null) {
			respondant = Respondant.getRespondantByUuid(respondantUuid);
		}

		if (respondant != null) {
			respondant.refreshMe();
			if (!user.canView(respondant)) {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				log.warn("Unauthorized Access Attempted by User: " + user.getUserId());
				json.put("message", "Access Restricted");
				return json.toString();			
			}
			if (respondant.getRespondantStatus() < Respondant.STATUS_PREDICTED) {
				respondant.refreshMe();
			}
			json.put("respondant", respondant.getJSON());
			json.put("scores", respondant.getAssessmentScore());
			json.put("detailed_scores", respondant.getAssessmentDetailedScore());
			Position position = Position.getPositionById(respondant.getRespondantPositionId());
			if (position != null)
				json.put("position", position.getJSON());
		} else {
			json.put("message", "Applicant not found");
		}

		return json.toString();
	}
}