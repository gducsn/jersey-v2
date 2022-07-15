package com.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class DAOUtil {
	
	public EntityManager entity() {
		
		EntityManager entityManager = JPAutil.getEntityManagerFactory().createEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		return entityManager;
		
	}
	

}
