package com.service;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dao.DAO;
import com.model.People;

import com.util.DAOUtil;
import com.util.RandomUtil;

@Path("/service")
public class Service {
	
	SingletonPeople util = SingletonPeople.getInstance();
	DAO crud = new DAO();
	DAOUtil em = new DAOUtil();
	
	public Service() {
		
	}
	

	
	private List<People> allPeoples() {
		EntityManager entityManager = em.entity();
		List<People> listDB = entityManager.createQuery("select p from People p ", People.class).getResultList();
		return listDB;
	}

	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getpeople")
	public List<People> getPeople() {
		return allPeoples();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getsingle/{id}")
	public People onePersona(@PathParam(value = "id") int id) {
		People a = crud.getSingle(id);
		return a;
	}
	
	@Path("/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@DELETE
	public List<People> deletePersona(@PathParam(value = "id") int id) {
		crud.removeSingle(id);
		return allPeoples();

	}
	
	@Path("/update/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@PUT
	public List<People> updatePersona(@PathParam(value = "id") int id) {
		EntityManager entityManager = em.entity();
		People p = entityManager.find(People.class, id);
		People n = RandomUtil.peopleRandom(p.getAge());
		crud.updatePeople(id,n);
		return allPeoples();
	}
	
	
	
	
	
	
}
