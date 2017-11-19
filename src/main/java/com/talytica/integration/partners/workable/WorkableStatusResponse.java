package com.talytica.integration.partners.workable;

import java.util.List;

import com.employmeo.data.model.Prediction;
import com.employmeo.data.model.Respondant;

import jersey.repackaged.com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class WorkableStatusResponse {

	private String partner_status; // assessment asid
	private String partner_profile_url;
	private Double partner_score;
	
	public WorkableStatusResponse (Respondant respondant) {
		if (respondant.getRespondantStatus() >= Respondant.STATUS_PREDICTED) {
			setPartner_status("complete");
			setPartner_score(respondant.getCompositeScore());
			List<String> predictions = Lists.newArrayList();
			for (Prediction prediction : respondant.getPredictions()) {
				StringBuffer pred = new StringBuffer();
				pred.append(String.format("%.0d%", 100*prediction.getPredictionScore()));
				pred.append(" chance that ");
				pred.append(respondant.getPerson().getFirstName());
				pred.append(" ");
				pred.append(prediction.getPositionPredictionConfig().getPredictionTarget().getLabel());
				predictions.add(pred.toString());
			}
		} else {
			setPartner_status("incomplete");
		}
	}
	

}
