package com.talytica.integration.partners.greenhouse;

import java.util.List;
import java.util.Set;

import com.employmeo.data.model.Grader;
import com.employmeo.data.model.Prediction;
import com.employmeo.data.model.Respondant;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
public class GreenhouseStatusResponse {

	private String partner_status; // assessment asid
	private String partner_profile_url;
	private Double partner_score;
	private Metadata metadata;
	
	public GreenhouseStatusResponse (Respondant respondant) {
		setMetadata(new Metadata());
		if (respondant.getRespondantStatus() >= Respondant.STATUS_PREDICTED) {
			setPartner_status("complete");
			setPartner_score(respondant.getCompositeScore());
			String category = respondant.getAccount().getCustomProfile().getName(respondant.getProfileRecommendation());	
			setMetaPredictions(respondant.getPredictions(),respondant.getPerson().getFirstName());
			setMetaCategory(category);
		} else {
			setPartner_status("incomplete");
		}
	}
	
	public void setMetaCategory (String category) {
		getMetadata().setCategory(category);
	}
	
	public void setMetaPredictions (Set<Prediction> preds, String firstName) {
		List<String> predictions = Lists.newArrayList();
		for (Prediction prediction : preds) {
			StringBuffer pred = new StringBuffer();
			pred.append(String.format("%.1f", 100*prediction.getPredictionScore()));
			pred.append("% chance that ");
			pred.append(firstName);
			pred.append(" ");
			pred.append(prediction.getPositionPredictionConfig().getPredictionTarget().getLabel());
			pred.append(".");
			predictions.add(pred.toString());
		}
		getMetadata().setPredictions(predictions);
	}
	
	public void setMetaReferences (List<Grader> graders) {
		List<String> references = Lists.newArrayList();
		for (Grader reference : graders) {
			StringBuffer grader = new StringBuffer();
			grader.append(reference.getPerson().getFirstName());
			grader.append(" ");
			grader.append(reference.getPerson().getFirstName());
			grader.append(" (");
			grader.append(reference.getRelationship());
			grader.append(") - ");
			grader.append(reference.getSummaryScore());
			references.add(grader.toString());
		}
		getMetadata().setReferences(references);
	}
	
	public void setMetaScores (List<String> scores) {
		getMetadata().setScores(scores);
	}
	
	@Data
	@ToString
	@NoArgsConstructor
	private class Metadata {
		String category;
		List<String> predictions;
		List<String> references;
		List<String> scores;
	}
	
}
