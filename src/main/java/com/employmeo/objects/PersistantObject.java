package com.employmeo.objects;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.json.JSONObject;

import com.employmeo.util.DBUtil;

public abstract class PersistantObject {

	public void persistMe() {
		EntityManager em = DBUtil.getEntityManager();
		EntityTransaction txn = em.getTransaction();
		if (!txn.isActive()) txn.begin();
		em.persist(this);
		if (txn.isActive()) txn.commit();
	}

	public void mergeMe() {
		EntityManager em = DBUtil.getEntityManager();
		EntityTransaction txn = em.getTransaction();
		if (!txn.isActive()) txn.begin();
		em.merge(this);
		if (txn.isActive()) txn.commit();
	}

	public void refreshMe() {
		EntityManager em = DBUtil.getEntityManager();
		EntityTransaction txn = em.getTransaction();
		if (!txn.isActive()) txn.begin();
		em.refresh(this);
	}

	public String getJSONString() {

		return getJSON().toString();
	}

	public abstract JSONObject getJSON();

}
