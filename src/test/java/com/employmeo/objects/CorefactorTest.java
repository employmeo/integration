package com.employmeo.objects;

import org.junit.Test;

import org.junit.Before;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.After;

public class CorefactorTest {
	private Corefactor cf = null;

	
	@Before
	public void setup() {
		this.cf = newCorefactor();
	}
	

	@After
	public void tearDown() {
		cf = null;
	}
	
	private Corefactor newCorefactor() {
		cf = new Corefactor();
		cf.setCorefactorId(1);
		cf.setCfHigh(1.0D);
		cf.setCfHighDescription("cf high description");
		cf.setCfLow(0.1D);
		cf.setCfLowDescription("cf low description");
		cf.setCfMeanScore(0.5D);
		cf.setCfMeasurements(2L);
		cf.setCfScoreDeviation(0.25D);
		cf.setCfSource("cf source");
		cf.setCorefactorDescription("cf description");
		cf.setCorefactorName("cf name");
		cf.setCorefactorForeignId("cf foreign id");
		cf.setCfDisplayGroup("cf display group");		
		
		return cf;
	}


	@Test
	public void toJson() {
		JSONObject json = cf.getJSON();
		
		assertTrue(cf.getCorefactorId().equals(json.getInt("corefactor_id")));
		assertEquals(cf.getCorefactorName(), json.getString("corefactor_name"));
		assertEquals(cf.getCfDisplayGroup(), json.getString("corefactor_display_group"));
		assertEquals(cf.getCorefactorDescription(), json.getString("corefactor_description"));
		assertEquals(0, Double.compare(cf.getCfHigh(), json.getDouble("corefactor_high")));
		assertEquals(0, Double.compare(cf.getCfLow(), json.getDouble("corefactor_low")));
		assertEquals(cf.getCfLowDescription(), json.get("corefactor_low_desc"));
		assertEquals(cf.getCfHighDescription(), json.get("corefactor_high_desc"));
		assertEquals(0, Double.compare(cf.getCfMeanScore(), json.getDouble("corefactor_mean_score")));
		assertEquals(0, Double.compare(cf.getCfScoreDeviation(), json.getDouble("corefactor_score_deviation")));
		assertTrue(cf.getCfMeasurements().equals(json.getLong("corefactor_measurements")));
		assertEquals(cf.getCfSource(), json.get("corefactor_source"));
		assertEquals(cf.getCorefactorForeignId(), json.get("corefactor_foreign_id"));
	
	}
	
	@Test
	public void fromJson() {
		JSONObject json = new JSONObject();
		json.put("corefactor_name", cf.getCorefactorName());
		json.put("corefactor_display_group", cf.getCfDisplayGroup());
		json.put("corefactor_id", cf.getCorefactorId());
		json.put("corefactor_description", cf.getCorefactorDescription());
		json.put("corefactor_high", cf.getCfHigh());
		json.put("corefactor_low", cf.getCfLow());
		json.put("corefactor_high_desc", cf.getCfHighDescription());
		json.put("corefactor_low_desc", cf.getCfLowDescription());
		json.put("corefactor_mean_score", cf.getCfMeanScore());
		json.put("corefactor_score_deviation", cf.getCfScoreDeviation());
		json.put("corefactor_measurements", cf.getCfMeasurements());
		json.put("corefactor_source", cf.getCfSource());
		json.put("corefactor_foreign_id", cf.getCorefactorForeignId());	
		
		Corefactor cfFromJson = Corefactor.fromJSON(json);
		assertEquals(cf, cfFromJson);
	}
}
