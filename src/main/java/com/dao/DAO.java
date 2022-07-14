package com.dao;

import javax.persistence.EntityManager;

import com.model.People;
import com.util.DAOUtil;

public class DAO {
	
	DAOUtil em = new DAOUtil();
	
	public People getSingle(int id) {
		EntityManager entityManager = em.entity();
		People p = em.entity().find(People.class, id);
		entityManager.getTransaction().commit();
		entityManager.close();
		return p;
	};
	
	public void removeSingle(int id) {
		EntityManager entityManager = em.entity();
		People p = entityManager.find(People.class, id);
		entityManager.remove(entityManager.contains(p) ? p : entityManager.merge(p));
		entityManager.getTransaction().commit();
		entityManager.close();
	};
	
	public void updatePeople(int id, People n) {
		EntityManager entityManager = em.entity();
		People p = entityManager.find(People.class, id);
		p.setName(n.getName());
		p.setSurname(n.getSurname());
		p.setAge(n.getAge());
		entityManager.persist(p);
		entityManager.getTransaction().commit();
		entityManager.close();
		
	}
	
	

}
