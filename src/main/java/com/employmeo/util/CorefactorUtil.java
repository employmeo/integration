package com.employmeo.util;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.json.JSONArray;
import org.json.JSONObject;

import com.employmeo.objects.Corefactor;
import com.employmeo.objects.CorefactorDescription;

/**
 * Static utility class for corefactor upserts and migrations
 * 
 * @author NShah
 *
 */
public class CorefactorUtil {
	private static final Logger log = LoggerFactory.getLogger(CorefactorUtil.class);
	
	public static void persistCorefactors(String corefactorDefinitions) throws IllegalStateException {
		EntityManager em = DBUtil.getEntityManager();
		EntityTransaction txn = em.getTransaction();
		if (!txn.isActive()) 
			txn.begin();

		try {	
			List<Corefactor> corefactors = getCorefactorsFromJson(corefactorDefinitions);
			
			persistCoreFactors(corefactors, em);
			txn.commit();
			
		} catch (Exception e) {
			log.debug("Failed to persist corefactors, rolling back." + e);
			txn.rollback();
			throw new IllegalStateException("Failed to persist corefactors", e);
		}		
	}

	private static void persistCoreFactors(List<Corefactor> corefactors, EntityManager em) {		
		corefactors.forEach(corefactor -> {
			corefactor.getCorefactorDescriptions().forEach(description -> {
				em.merge(description);
				log.trace("Merged corefactor description with id: " + description.getCfdescId());
			});
			em.merge(corefactor);
			log.trace("Merged corefactor with id: " + corefactor.getCorefactorId());
		});
		log.debug("Corefactors persisted successfully");		
	}
		
	public static JSONArray getJsonCorefactors() {
		JSONArray jsonCorefactors = new JSONArray();
		List<Corefactor> corefactors = Corefactor.getAllCorefactors();
		corefactors.forEach(corefactor -> {
			JSONObject jsonCorefactor = corefactor.getJSON();
			
			List<CorefactorDescription> descriptions = corefactor.getCorefactorDescriptions();
			JSONArray jsonCorefactorDescriptions = new JSONArray();
			descriptions.forEach(description -> {
				JSONObject jsonDescription = description.getJSON();
				jsonCorefactorDescriptions.put(jsonDescription);
			});
			jsonCorefactor.put("descriptions", jsonCorefactorDescriptions);
			jsonCorefactors.put(jsonCorefactor);
			log.trace("Corefactor: {}", jsonCorefactor);
		});
		return jsonCorefactors;
	}
	
	private static List<Corefactor> getCorefactorsFromJson(String corefactorDefinitions) {
		JSONArray jsonArray = new JSONArray(corefactorDefinitions);
		// log.debug("Hydrated JSONObject: " + jsonArray);
		
		List<Corefactor> corefactors = new ArrayList<Corefactor>();
		for(int i=0; i < jsonArray.length(); i++) {
			JSONObject json = jsonArray.getJSONObject(i);
			Corefactor corefactor = Corefactor.fromJSON(json);
			JSONArray jsonDescriptions = json.getJSONArray("descriptions");
			List<CorefactorDescription> descriptions = new ArrayList<CorefactorDescription>();
			for(int j=0; j < jsonDescriptions.length(); j++) {
				JSONObject jsonDescription = jsonDescriptions.getJSONObject(j);
				CorefactorDescription description = CorefactorDescription.fromJSON(jsonDescription);
				descriptions.add(description);
			}
			corefactor.setCorefactorDescriptions(descriptions);
			corefactors.add(corefactor);
		}
		
		return corefactors;
	}	
}
