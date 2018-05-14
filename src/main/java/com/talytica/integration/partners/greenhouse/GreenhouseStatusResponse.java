package com.talytica.integration.partners.greenhouse;

import java.util.List;

import com.employmeo.data.model.Prediction;
import com.employmeo.data.model.Respondant;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GreenhouseStatusResponse {

	private String partner_status; // assessment asid
	private String partner_profile_url;
	private Double partner_score;
	private Metadata metadata;
	
	public GreenhouseStatusResponse (Respondant respondant) {
		if (respondant.getRespondantStatus() >= Respondant.STATUS_PREDICTED) {
			setPartner_status("complete");
			setPartner_score(respondant.getCompositeScore());
			String category = respondant.getAccount().getCustomProfile().getName(respondant.getProfileRecommendation());
			List<String> predictions = Lists.newArrayList();
			for (Prediction prediction : respondant.getPredictions()) {
				StringBuffer pred = new StringBuffer();
				pred.append(String.format("%.1f", 100*prediction.getPredictionScore()));
				pred.append("% chance that ");
				pred.append(respondant.getPerson().getFirstName());
				pred.append(" ");
				pred.append(prediction.getPositionPredictionConfig().getPredictionTarget().getLabel());
				pred.append(".");
				predictions.add(pred.toString());
			}
			setMetadata(new Metadata(category,predictions));
		} else {
			setPartner_status("incomplete");
		}
	}
	
	@Data
	@ToString
	@AllArgsConstructor
	private class Metadata {
		String category;
		List<String> predictions;	
	}
	
}
