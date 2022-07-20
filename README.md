# Jersey - crud v2

Ho rivisitato tutto il primo progetto in modo da aggiungere anche il layer 
di persistenza con Hibernate e le annotazioni JPA.
Inoltre ho implementato le Singleton, cioè quelle classi che possono 
essere istanziate una singola volta. 

```java
public static Singleton getIstance() {
    if(istance==null)
      istance = new Singleton();
    return istance;
  }
```

Il metodo vedi se l’istanza è presente o meno. 

Per rendere una classe Singleton basta rendere il costruttore privato e 
delegare la creazione dell’istanza ad un metodo di tipo statico.
Mi è servita per poter creare dinamicamente la lista di persone per 
popolare il database. Le persone sono create dinamicamente e ognuna di 
loro è diversa. Anche la lista non ha un numero fisso.

Ho approfittato di questo per capire meglio il funzionamento del pattern 
Singleton e per evitare di riempire ad ogni chiamata il database.

I package:

- com.model
- com.dao
- com.service
- com.util

---

com.model

```java
package com.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name="peoples")
public class People {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private int id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="surname")
	private String surname;
	
	@Column(name="age")
	private int age;
	
	public People(int id, String name, String surname, int age) {
		super();
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.age = age;
	}
	
	public People(String name, String surname, int age) {
		super();
		this.name = name;
		this.surname = surname;
		this.age = age;
	}
	
	public People() {}
	
	@XmlElement
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@XmlElement
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	@XmlElement
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	

}
```

La classe che ci servirà da modello per istanziare le nostre persone. 


Utilizziamo le annotazioni JAXB per poter fare 'marshalling e unmarshalling' della nostra classe.

```xml
@XmlRootElement -> sulla classe
@XmlElement -> su i metodi get della classe
```

Adesso dobbiamo mappare la classe in modo da poterla relazionare con il 
database. Utilizziamo le annotazioni JPA:

```xml
@Entity -> sopra la classe rendendola entità
@Table(name="peoples") -> mappiamo la tabella nel db
@Id -> definiamo la primarykey
@GeneratedValue(strategy = GenerationType.IDENTITY) -> la strategia di 
incremento
@Column(name="") -> mappiamo ogni proprietà della classe alle colonne del 
db
```

---

com.dao

La classe DAO gestisce i metodi che saranno collegati ai verbi HTTP. Ad 
esempio, quando vorremo avere qualcosa dal db aggiungeremo il metodo sul 
verbo GET. Sarà più chiaro dopo.

```java
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
		entityManager.remove(entityManager.contains(p) ? p : 
entityManager.merge(p));
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
```

I metodi gestiscono le operazioni CRUD. In ogni metodo è presente l’entity 
manager definito nella classe Util. Quindi la connessione è gestita da 
‘em.entity()’ dalla classe DAOUtil.

```java
public People getSingle(int id) {
		EntityManager entityManager = em.entity();
		People p = em.entity().find(People.class, id);
		entityManager.getTransaction().commit();
		entityManager.close();
		return p;
	};
```

Il primo ci serve per ritornare un singolo record dal db. Facciamo partire 
la connessione e utilizziamo il metodo find per ritornare il record che 
vogliamo.

`http://localhost:8080/jersey-crud-v2/people/service/getsingle/1`

Quando clicchiamo su quel link si avvia il metodo ‘getSingle()’ e viene 
passato il numero ‘1’ come argomento.

Per ogni metodo, infine, c’è la chiusura della connessione.

```java
public void removeSingle(int id) {
		EntityManager entityManager = em.entity();
		People p = entityManager.find(People.class, id);
		entityManager.remove(entityManager.contains(p) ? p : 
entityManager.merge(p));
		entityManager.getTransaction().commit();
		entityManager.close();
	};
```

Avviamo la connessione, troviamo il record tramite il suo ID, lo 
rimuoviamo.

Nel metodo ‘remove’ abbiamo un ternario che dice:  nel db è presente 
quello che cerchi? Se si allora passa come argomento al metodo remove 
quello che vuoi cancellare, altrimenti prima unisci al db quello che hai 
cercato e dopo cancelli. In questo modo evitiamo errori durante la 
chiamata.

`http://localhost:8080/jersey-crud-v2/people/service/delete/1`

```java
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
```

Il metodo per aggiornare il nostro record accetta due argomenti, l’id e 
una nuova persona. La nuova persona viene creata in modo random nella 
classe che gestisce Jersey. Sarà più chiaro dopo.

Il metodo avvia la connessione, trova il record tramite l’id che gli 
passiamo. Per ogni proprietà del record che abbiamo recuperato inseriamo 
nuovi valori che prendiamo dalla persona che abbiamo passato come 
argomento. Persistiamo e chiudiamo la connessione.

---

com.service

→ Service.java

→ SingletonPeople.java

Service.java

```java
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
		List<People> listDB = entityManager.createQuery("select p 
from People p ", People.class).getResultList();
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
	public List<People> deletePersona(@PathParam(value = "id") int id) 
{
		crud.removeSingle(id);
		return allPeoples();

	}
	
	@Path("/update/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@PUT
	public List<People> updatePersona(@PathParam(value = "id") int id) 
{
		EntityManager entityManager = em.entity();
		People p = entityManager.find(People.class, id);
		People n = RandomUtil.peopleRandom(p.getAge());
		crud.updatePeople(id,n);
		return allPeoples();
	}

	
}
```

Prima di chiarire i metodi chiariamo le annotazioni sopra ad esse. Le 
annotazioni sono quelle di Jax-rs, le quali ci permettono di gestire le 
risorse attraverso i verbi HTTP. 

```xml
@Path("/service") -> la prima annotazione da aggiungere sopra 
la classe così da mapparla a questo indirizzo

@GET -> il tipo di verbo al quale colleghiamo il metodo

@Produces(MediaType.APPLICATION_JSON) -> il tipo di risultato che avremo, 
in questo caso di tipo JSON

@Path("/getsingle/{id}") -> l'indirizzo a cui sarà possibile accedere. E' 
possibile
inserire dinamicamente l'id:
http://localhost:8080/jersey-crud-v2/people/service/delete/{id} (es.)
```

Una volta annotati i nostri metodi possiamo implementarli.

```java
	SingletonPeople util = SingletonPeople.getInstance();
	DAO crud = new DAO();
	DAOUtil em = new DAOUtil();

	private List<People> allPeoples() {
		EntityManager entityManager = em.entity();
		List<People> listDB = entityManager.createQuery("select p 
from People p ", People.class).getResultList();
		return listDB;
	}
```

Creiamo un’istanza Singleton così all’avvio dell’app avremo il nostro 
database popolato. Sarà più chiaro dopo.

La prima cosa che ci serve è recuperare tutti i record dal database e lo 
facciamo tramite il metodo ‘allPeoples()’.

Il metodo ritorna tutti i record i una lista tramite una query che appunto 
li seleziona tutti. Abbiamo così la nostra lista dinamica. 

```java
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getpeople")
	public List<People> getPeople() {
		return allPeoples();
	}
```

Questo metodo è collegato all’indirizzo `/getpeople/`. Ogni volta che 
andremo visualizzeremo tutti i record nel database in formato JSON. 

```java
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getsingle/{id}")
	public People onePersona(@PathParam(value = "id") int id) {
		People a = crud.getSingle(id);
		return a;
	}
```

Il metodo ritorna un singolo record passando al metodo nel DAO l’id, cioè 
quel numero passato nel link:

`http://localhost:8080/jersey-crud-v2/people/service/getsingle/1`

In questo modo otterremo il record con ID 1. 

```java
	@Path("/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@DELETE
	public List<People> deletePersona(@PathParam(value = "id") int id) 
{
		crud.removeSingle(id);
		return allPeoples();

	}
```

Con lo stesso principio del precedente eliminiamo un record passando l’id 
al metodo nel DAO, successivamente ritorniamo tutti i record per 
verificare che sia stato correttamente eliminato.

Infine il metodo per aggiornare un’entità:

```java
	@Path("/update/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@PUT
	public List<People> updatePersona(@PathParam(value = "id") int id) 
{
		EntityManager entityManager = em.entity();
		People p = entityManager.find(People.class, id);
		People n = RandomUtil.peopleRandom(p.getAge());
		crud.updatePeople(id,n);
		return allPeoples();
	}
```

Passiamo sempre l’id del record che vogliamo editare. Avviamo la 
connessione e cerchiamo il record associato all’id passato come argomento 
del metodo. Una volta trovato creiamo un utente random con il metodo nella 
classe RandomUtil.java (dopo…) passandogli solo la proprietà age così 
avremo un nuovo utente ma con la stessa età. Passiamo la nuova persona al 
metodo update del DAO e ritorniamo la lista dal DB. 

SingletonPeople.java

```java
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
		for (int i = 1; i <= RandomUtil.randomPeoples(); i++) {
			People p = null;
			try {
				p = new People(i, RandomUtil.randomStr(), 
RandomUtil.randomStr(), RandomUtil.randomAge());
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

		EntityManager entityManager = 
JPAutil.getEntityManagerFactory().createEntityManager();
		EntityTransaction entityTransaction = 
entityManager.getTransaction();
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
```

Il pattern Singleton ci permette di istanziare una singola volta una 
classe, in questo caso all’avvio dell’app. Ci serve per popolare il 
database una singola volta. Ripeto: è solo per avere più pulito il db, e 
anche per capire come funziona il pattern.

```java
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
```

Creiamo un’istanza della nostra classe privata. Il costruttore lo rendiamo 
privato e gli passiamo di due metodi: creazione persone - aggiunta nel db. 
Questi due metodi verrano chiamati una singola volta. 

Creiamo un nuovo metodo publico che ritorna l’istanza della classe se non 
è presente oppure null se è già presente. In questo modo otteniamo il 
pattern Singleton.

```java
private static List<People> all;

private static void createPeople() {

		all = new ArrayList<People>();
		for (int i = 1; i <= RandomUtil.randomPeoples(); i++) {
			People p = null;
			try {
				p = new People(i, RandomUtil.randomStr(), 
RandomUtil.randomStr(), RandomUtil.randomAge());
				all.add(p);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
```

Creiamo una lista e un metodo privato. Il metodo crea dinamicamente una 
lista di persone di grandezza diversa ogni volta. Ogni proprietà di ogni 
persona è differente. La coerenza si trova solo sulla proprietà ID, la 
quale è collegata alla primary key del database. Così avremo gli ID in 
ordine crescente e più facilmente gestibile.

Una volta creati li aggiungiamo alla lista.

```java
private void createDBPeople(List<People> list) {

		EntityManager entityManager = 
JPAutil.getEntityManagerFactory().createEntityManager();
		EntityTransaction entityTransaction = 
entityManager.getTransaction();
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
```

Aggiungiamo la lista utenti al db passandola come argomento del metodo.

Avviamo la connessione e istanziamo una nuova persona. Con un ciclo for 
cicliamo ogni elemento della lista. Ogni elemento della lista verrà 
gestito e persistito nel database. Infine chiudiamo la connessione.

---

L’ultimo package è quello ‘util’, il quale contiene tutti quei metodi di 
utilità separati dagli altri package per avere tutto più in ordine.

- DAOUtil.java
- JPAutil.java
- RandomUtil.java

DAOUtil.java

```java
package com.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class DAOUtil {
	
	public EntityManager entity() {
		
		EntityManager entityManager = 
JPAutil.getEntityManagerFactory().createEntityManager();
		EntityTransaction entityTransaction = 
entityManager.getTransaction();
		entityTransaction.begin();
		return entityManager;
		
	}
	

}
```

La classe contiene un semplice metodo che ritorna un gestore di entità. Il 
metodo nasce per rendere più pulito il codice nelle altre classi.

JPAUtil.java

```java
package com.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAutil {
	private static final String PERSISTENCE_UNIT_NAME = "jersey";
	private static EntityManagerFactory factory;

	public static EntityManagerFactory getEntityManagerFactory() {
		if (factory == null) {
			factory = 
Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		}
		return factory;
	}

	public static void shutdown() {
		if (factory != null) {
			factory.close();
		}
	}
}
```

La classe fondamentale per avviare la connessione con il db.

RandomUtil.java

```java
package com.util;

import java.util.Random;

import com.model.People;

public class RandomUtil {
	
	public static int randomAge() {
		int max = 99;
		int min = 1;
		int range = (max - min) + 1;
		int rnd = (int) (Math.random() * range) + min;
		return rnd;
	}
	
	public static int randomPeoples() {
		int max = 60;
		int min = 10;
		int range = (max - min) + 1;
		int rnd = (int) (Math.random() * range) + min;
		return rnd;
	}
	
	public static String randomStr() {
		
		 // create a string of uppercase and lowercase characters 
and numbers
	    String upperAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    String lowerAlphabet = "abcdefghijklmnopqrstuvwxyz";
	    String numbers = "0123456789";

	    String alphaNumeric = upperAlphabet + lowerAlphabet + numbers;
	    StringBuilder sb = new StringBuilder();
	    Random random = new Random();
	    for(int i = 0; i <11; i++) {
	      int index = random.nextInt(alphaNumeric.length());
	      char randomChar = alphaNumeric.charAt(index);
	      sb.append(randomChar);
	    }

	    String randomString = sb.toString();
	    return randomString;

	}
	
	public static People peopleRandom(int age) {
		People p = new People(RandomUtil.randomStr() + 
"gdu",RandomUtil.randomStr(),age);
		return p;
	}

}
```

La classe contiene metodi per creare in numeri, stringe singole persone 
random.

```java
public static int randomAge() {
		int max = 99;
		int min = 1;
		int range = (max - min) + 1;
		int rnd = (int) (Math.random() * range) + min;
		return rnd;
	}
	
	public static int randomPeoples() {
		int max = 60;
		int min = 10;
		int range = (max - min) + 1;
		int rnd = (int) (Math.random() * range) + min;
		return rnd;
	}
```

Numeri random.

```java
public static String randomStr() {
		
		 // create a string of uppercase and lowercase characters 
and numbers
	    String upperAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    String lowerAlphabet = "abcdefghijklmnopqrstuvwxyz";
	    String numbers = "0123456789";

	    String alphaNumeric = upperAlphabet + lowerAlphabet + numbers;
	    StringBuilder sb = new StringBuilder();
	    Random random = new Random();
	    for(int i = 0; i <11; i++) {
	      int index = random.nextInt(alphaNumeric.length());
	      char randomChar = alphaNumeric.charAt(index);
	      sb.append(randomChar);
	    }

	    String randomString = sb.toString();
	    return randomString;

	}
```

Stringhe random.

```java
public static People peopleRandom(int age) {
		People p = new People(RandomUtil.randomStr() + 
"gdu",RandomUtil.randomStr(),age);
		return p;
	}
```

Singola persona random.
