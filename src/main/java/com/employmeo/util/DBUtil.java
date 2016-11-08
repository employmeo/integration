package com.employmeo.util;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

@SuppressWarnings({ "rawtypes", "unchecked" })
@PersistenceContext(unitName = "employmeo")
public class DBUtil {

	private static EntityManagerFactory emf;
	private static final ThreadLocal<EntityManager> threadLocal;

	static {

		Map properties = new HashMap();

		// Get database connection details from ENV VARIABLES
		String dbuser = System.getenv("DB_USERNAME");
		String dbpass = System.getenv("DB_PASSWORD");
		String dburl = System.getenv("DB_URL");// + "?currentSchema=employmeo&sslmode=require";
				
		properties.put("javax.persistence.jdbc.user", dbuser);
		properties.put("javax.persistence.jdbc.password", dbpass);
		properties.put("javax.persistence.jdbc.url", dburl);
		properties.put("eclipselink.connection-pool.node2.url", dburl);

		emf = Persistence.createEntityManagerFactory("employmeo", properties);
		threadLocal = new ThreadLocal<EntityManager>();
	}


    public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();

        if (em == null) {
            em = emf.createEntityManager();
            threadLocal.set(em);
        }
        return em;
    }

    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.set(null);
        }
    }

    public static void closeEntityManagerFactory() {
        emf.close();
    }

    public static void beginTransaction() {
    	if(!isTxnActive()) {
    		getEntityManager().getTransaction().begin();
    	}
    }

    public static void rollback() {
    	if(isTxnActive()) {
    		getEntityManager().getTransaction().rollback();
    	}
    }

    public static void commit() {
    	if(isTxnActive()) {
    		getEntityManager().getTransaction().commit();
    	}
    }
	
	public static boolean isTxnActive() {
		return getEntityManager().getTransaction().isActive();
	}
}
