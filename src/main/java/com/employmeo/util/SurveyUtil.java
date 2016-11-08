package com.employmeo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.employmeo.objects.Answer;
import com.employmeo.objects.Question;
import com.employmeo.objects.Survey;
import com.employmeo.objects.SurveySection;

/**
 * Static utility class for survey migrations - serialization/deserialization
 * and persistence)
 * 
 * @author NShah
 *
 */
public class SurveyUtil {

	private static final Logger log = LoggerFactory.getLogger(SurveyUtil.class);

	public static void persistSurvey(Survey survey) throws IllegalStateException {
		log.debug("Proceeding to persist survey with id: " + survey.getSurveyId());
		
		validateSurvey(survey);

		EntityManager em = DBUtil.getEntityManager();
		EntityTransaction txn = em.getTransaction();
		if (!txn.isActive()) 
			txn.begin();
		
		try {		
			persistSurvey(survey, em);

			txn.commit();
			log.debug("Survey with id " + survey.getSurveyId() + " persisted");
		} catch (Exception e) {
			log.warn("Failed to persist survey, rolling back." + e);
			if (txn.isActive()) {
				txn.rollback();
			}

			throw new IllegalStateException("Failed to persist survey", e);
		}
	}

	private static void validateSurvey(Survey survey) {
		Survey existingSurvey = Survey.getSurveyById(survey.getSurveyId());
		if (null != existingSurvey) {
			throw new IllegalStateException("Survey with survey id " + survey.getSurveyId() + " already exists");
		}
		
		log.debug("Validated that survey with id " + survey.getSurveyId() + " does not exist, proceeding further");
	}

	private static void persistSurvey(Survey survey, EntityManager em) {
		em.persist(survey);
		log.debug("Survey entity persisted");
	}


}
