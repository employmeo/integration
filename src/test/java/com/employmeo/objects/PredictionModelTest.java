package com.employmeo.objects;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.employmeo.objects.PredictionModel.PredictionModelType;
import com.employmeo.util.DBUtil;

public class PredictionModelTest {

	public void createNew() {
		PredictionModel model = new PredictionModel();

		model.setName("junit test prediction model");
		model.setVersion(2);
		model.setDescription("test model for mapping validation");
		model.setModelType(PredictionModelType.LINEAR_REGRESSION);
		model.setActive(Boolean.FALSE);

		model.persistMe();

		List<PredictionModel> persistedModels = DBUtil.getEntityManager()
				.createNamedQuery("PredictionModel.findAll", PredictionModel.class)
				.getResultList();

		assertNotNull(persistedModels);
		Boolean found = persistedModels.stream()
				.anyMatch(pm -> "junit test prediction model".equals(pm.getName()) && 2 == (model.getVersion()));
		assertTrue(found);

	}

}
