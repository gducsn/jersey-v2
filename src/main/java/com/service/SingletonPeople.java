package com.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.model.People;
import com.util.JPAutil;
import com.util.RandomUtil;

public class SingletonPeople {

	private static List<People> all;

	private static SingletonPeople instance;

	private SingletonPeople() {
		createPeople();
		createDBPeople(all);

	}

	public static SingletonPeople getInstance() {
		if (instance == null) {
			instance = new SingletonPeople();
		}

		return instance;
	}


		

	private static void createPeople() {

		all = new ArrayList<People>();
		for (int i = 1; i <= 26; i++) {
			People p = null;
			try {
				p = new People(i, RandomUtil.randomStr(), RandomUtil.randomStr(), RandomUtil.randomAge());
				all.add(p);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public static List<People> getPeoples() {
		return all;
	}

	private void createDBPeople(List<People> list) {

		EntityManager entityManager = JPAutil.getEntityManagerFactory().createEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		People p = null;

		for (People data : list) {
			p = new People();
			p.setName(data.getName());
			p.setSurname(data.getSurname());
			p.setAge(data.getAge());
			entityManager.persist(p);
		}
		entityManager.flush();
		entityManager.getTransaction().commit();
		entityManager.close();

	}
	

	
	
	

}
