package com.employmeo.objects;

import java.util.List;

import org.json.JSONObject;

public class PositionProfile extends JSONObject {
	public static final String PROFILE_A = "profile_a";
	public static final String PROFILE_B = "profile_b";
	public static final String PROFILE_C = "profile_c";
	public static final String PROFILE_D = "profile_d";
	public static final String UNSCORED = "unscored";

	public static final String aColor = "#5cb85c";
	public static final String aOverlay = "rgba(92, 184, 92,0.3)";
	public static final String aHighlight = "#4cae4c";

	public static final String bColor = "#5bc0de";
	public static final String bOverlay = "rgba(91, 192, 222,0.3)";
	public static final String bHighlight = "#46b8da";

	public static final String cColor = "#f0ad4e";
	public static final String cOverlay = "rgba(240, 173, 78, 0.3)";
	public static final String cHighlight = "#eea236";

	public static final String dColor = "#d9534f";
	public static final String dOverlay = "rgba(217, 83, 79,0.3)";
	public static final String dHighlight = "#d43f3a";

	public static final String uColor = "rgba(120,60,100,1)";
	public static final String uOverlay = "rgba(120,60,100,0.3)";
	public static final String uHighlight = "rgba(120,60,100,1)";

	public PositionProfile() {
	}

	public static PositionProfile getProfileA(Position position) {
		return getProfile(PROFILE_A, position);
	}

	public static PositionProfile getProfileB(Position position) {
		return getProfile(PROFILE_B, position);
	}

	public static PositionProfile getProfileC(Position position) {
		return getProfile(PROFILE_C, position);
	}

	public static PositionProfile getProfileD(Position position) {
		return getProfile(PROFILE_D, position);
	}

	public static PositionProfile getProfile(String profileName, Position position) {
		PositionProfile profile = getProfileDefaults(profileName);
		List<PredictiveModel> pmList = position.getPmFactors();
		JSONObject scores = new JSONObject();
		for (int i = 0; i < pmList.size(); i++) {
			scores.put(pmList.get(i).getCorefactor().getCorefactorName(), pmList.get(i).getPmProfileScore(profileName));
		}
		return profile;
	}

	public static PositionProfile getProfileDefaults(String profileName) {
		PositionProfile profile = new PositionProfile();
		switch (profileName) {
		case PROFILE_A:
			profile.put("profile_name", "Rising Star");
			profile.put("profile_class", "btn-success");
			profile.put("profile_color", aColor);
			profile.put("profile_highlight", aHighlight);
			profile.put("profile_overlay", aOverlay);
			profile.put("profile_icon", "fa-rocket");
			break;
		case PROFILE_B:
			profile.put("profile_name", "Long timer");
			profile.put("profile_class", "btn-info");
			profile.put("profile_color", bColor);
			profile.put("profile_highlight", bHighlight);
			profile.put("profile_overlay", bOverlay);
			profile.put("profile_icon", "fa-user-plus");
			break;
		case PROFILE_C:
			profile.put("profile_name", "Churner");
			profile.put("profile_class", "btn-warning");
			profile.put("profile_color", cColor);
			profile.put("profile_highlight", cHighlight);
			profile.put("profile_overlay", cOverlay);
			profile.put("profile_icon", "fa-warning");
			break;
		case PROFILE_D:
			profile.put("profile_name", "Red Flag");
			profile.put("profile_class", "btn-danger");
			profile.put("profile_color", dColor);
			profile.put("profile_highlight", dHighlight);
			profile.put("profile_overlay", dOverlay);
			profile.put("profile_icon", "fa-hand-stop-o");
			break;
		default:
			profile.put("profile_name", "Not Scored");
			profile.put("profile_class", "btn-default");
			profile.put("profile_color", uColor);
			profile.put("profile_highlight", uHighlight);
			profile.put("profile_overlay", uOverlay);
			profile.put("profile_icon", "fa-question-circle-o");
			break;
		}

		return profile;
	}

}
