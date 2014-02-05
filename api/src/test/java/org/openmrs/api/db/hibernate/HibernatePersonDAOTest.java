/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.List;

public class HibernatePersonDAOTest extends BaseContextSensitiveTest {
	
	private final static Log log = LogFactory.getLog(HibernatePersonDAOTest.class);
	
	private final static String PEOPLE_FROM_THE_SHIRE_XML = "org/openmrs/api/db/hibernate/include/HibernatePersonDAOTest-people.xml";
	
	private SessionFactory sessionFactory;
	
	private HibernatePersonDAO hibernatePersonDAO;
	
	private PersonAttributeHelper personAttributeHelper;
	
	@Before
	public void getPersonDAO() throws Exception {
		executeDataSet(PEOPLE_FROM_THE_SHIRE_XML);
		
		hibernatePersonDAO = (HibernatePersonDAO) applicationContext.getBean("personDAO");
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		
		personAttributeHelper = new PersonAttributeHelper(sessionFactory);
	}
	
	private void logPeople(List<Person> people) {
		for (Person person : people) {
			logPerson(person);
		}
	}
	
	private void logPerson(Person person) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("class=").append(person.getClass().getCanonicalName()).append(", person=").append(person.toString())
		        .append(", person.names=").append(person.getNames().toString()).append(", person.attributes=").append(
		            person.getAttributes().toString());
		
		log.debug(builder.toString());
	}
	
	/**
	 * @verifies get no one by null
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNull() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople(null, false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies get every one by empty string
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetEveryOneByEmptyString() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("", false);
		logPeople(people);
		
		// PEOPLE_FROM_THE_SHIRE_XML contains 7 people but more people are defined in the standard test data set
		Assert.assertTrue(people.size() >= 7);
		
		// assert that all 7 people from PEOPLE_FROM_THE_SHIRE_XML (who are neither dead nor voided) are retrieved
		assertPeopleContainPersonID(people, 42);
		assertPeopleContainPersonID(people, 43);
		assertPeopleContainPersonID(people, 44);
		assertPeopleContainPersonID(people, 45);
		assertPeopleContainPersonID(people, 46);
		assertPeopleContainPersonID(people, 47);
		assertPeopleContainPersonID(people, 48);
	}
	
	private void assertPeopleContainPersonID(List<Person> people, Integer personID) {
		for (Person person : people) {
			if (person.getId() == personID) {
				return;
			}
		}
		Assert.fail("list of people does not contain person with ID = " + personID);
	}
	
	/**
	 * @verifies get no one by non-existing attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingAttribute() throws Exception {
		Assert.assertFalse(personAttributeHelper.personAttributeExists("Wizard"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Wizard", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies get no one by non-searchable attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonsearchableAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.nonSearchablePersonAttributeExists("Porridge with honey"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Porridge honey", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies get no one by voided attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByVoidedAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master thief"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Master thief", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies get one person by attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Story Teller", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get one person by random case attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByRandomCaseAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		
		List<Person> people = hibernatePersonDAO.getPeople("sToRy TeLlEr", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get one person by searching for a mix of attribute and voided attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonBySearchingForAMixOfAttributeAndVoidedAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		Assert.assertFalse(personAttributeHelper.voidedPersonAttributeExists("Story teller"));
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master thief"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Story Thief", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get multiple people by single attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleBySingleAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Senior ring bearer"));
		List<Person> people = hibernatePersonDAO.getPeople("Senior ring bearer", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}
	
	/**
	 * @verifies get multiple people by multiple attributes
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByMultipleAttributes() throws Exception {
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Senior ring bearer"));
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		List<Person> people = hibernatePersonDAO.getPeople("Story Bearer", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}
	
	/**
	 * @verifies get no one by non-existing name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("Gandalf", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies get one person by name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("Bilbo", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get one person by random case name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByRandomCaseName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("fRoDo", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Frodo Ansilon", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get multiple people by single name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleBySingleName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("Baggins", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}
	
	/**
	 * @verifies get multiple people by multiple names
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByMultipleNames() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("Bilbo Frodo", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}
	
	/**
	 * @verifies get no one by non-existing name and non-existing attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingNameAndNonexistingAttribute() throws Exception {
		Assert.assertFalse(personAttributeHelper.personAttributeExists("Wizard"));
		
		List<Person> people = hibernatePersonDAO.getPeople("Gandalf Wizard", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies get no one by non-existing name and non-searchable attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingNameAndNonsearchableAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.nonSearchablePersonAttributeExists("Mushroom pie"));
		List<Person> people = hibernatePersonDAO.getPeople("Gandalf Mushroom pie", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies get no one by non-existing name and voided attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByNonexistingNameAndVoidedAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master Thief"));
		List<Person> people = hibernatePersonDAO.getPeople("Gandalf Master Thief", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies get one person by name and attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByNameAndAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.personAttributeExists("Story teller"));
		List<Person> people = hibernatePersonDAO.getPeople("Bilbo Story Teller", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Bilbo Odilon", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get one person by name and voided attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByNameAndVoidedAttribute() throws Exception {
		Assert.assertTrue(personAttributeHelper.voidedPersonAttributeExists("Master Thief"));
		List<Person> people = hibernatePersonDAO.getPeople("Frodo Master Thief", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("Frodo Ansilon", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get multiple people by name and attribute
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByNameAndAttribute() throws Exception {
		List<Person> people = hibernatePersonDAO
		        .getPeople(
		            "Bilbo Baggins Story Teller Master Thief Porridge Honey Frodo Baggins Ring Bearer Mushroom Pie Gandalf Wizard Beer",
		            false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("Baggins", people.get(0).getFamilyName());
		Assert.assertEquals("Baggins", people.get(1).getFamilyName());
		Assert.assertFalse(people.get(0).getGivenName().equalsIgnoreCase(people.get(1).getGivenName()));
	}
	
	/**
	 * @verifies get one person by given name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByGivenName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("bravo", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("bravo", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get multiple people by given name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByGivenName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("alpha", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("alpha", people.get(0).getGivenName());
		Assert.assertEquals("alpha", people.get(1).getGivenName());
		Assert.assertTrue(people.get(0).getMiddleName() != people.get(1).getMiddleName());
	}
	
	/**
	 * @verifies get one person by middle name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByMiddleName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("echo", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("echo", people.get(0).getMiddleName());
	}
	
	/**
	 * @verifies get multiple people by middle name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByMiddleName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("foxtrot", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("foxtrot", people.get(0).getMiddleName());
		Assert.assertEquals("foxtrot", people.get(1).getMiddleName());
		Assert.assertTrue(people.get(0).getFamilyName() != people.get(1).getFamilyName());
	}
	
	/**
	 * @verifies get one person by family name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByFamilyName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("lima", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("lima", people.get(0).getFamilyName());
	}
	
	/**
	 * @verifies get multiple people by family name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByFamilyName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("kilo", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("kilo", people.get(0).getFamilyName());
		Assert.assertEquals("kilo", people.get(1).getFamilyName());
		Assert.assertTrue(people.get(0).getGivenName() != people.get(1).getGivenName());
	}
	
	/**
	 * @verifies get one person by family name2
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByFamilyName2() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("mike", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("alpha", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get multiple people by family name2
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByFamilyName2() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("papa", false);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("papa", people.get(0).getPersonName().getFamilyName2());
		Assert.assertEquals("papa", people.get(1).getPersonName().getFamilyName2());
		Assert.assertTrue(people.get(0).getFamilyName() != people.get(1).getFamilyName());
	}
	
	/**
	 * @verifies get one person by multiple name parts
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetOnePersonByMultipleNameParts() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("echo india mike", false);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("alpha", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get multiple people by multiple name parts
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultiplePeopleByMultipleNameParts() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("bravo delta golf juliet mike ", false);
		logPeople(people);
		
		Assert.assertEquals(5, people.size());
	}
	
	/**
	 * @verifies get no one by voided name
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetNoOneByVoidedName() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("voided-delta", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies not get voided person
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldNotGetVoidedPerson() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("voided-bravo", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies not get dead person
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldNotGetDeadPerson() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("dead-charlie", false);
		logPeople(people);
		
		Assert.assertEquals(0, people.size());
	}
	
	/**
	 * @verifies get single dead person
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetSingleDeadPerson() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("dead-charlie", true);
		logPeople(people);
		
		Assert.assertEquals(1, people.size());
		Assert.assertEquals("dead-charlie", people.get(0).getGivenName());
	}
	
	/**
	 * @verifies get multiple dead people
	 * @see HibernatePersonDAO#getPeople(String, Boolean)
	 */
	@Test
	public void getPeople_shouldGetMultipleDeadPeople() throws Exception {
		List<Person> people = hibernatePersonDAO.getPeople("dead-papa", true);
		logPeople(people);
		
		Assert.assertEquals(2, people.size());
		Assert.assertEquals("dead-papa", people.get(0).getPersonName().getFamilyName2());
		Assert.assertEquals("dead-papa", people.get(1).getPersonName().getFamilyName2());
		Assert.assertTrue(people.get(0).getFamilyName() != people.get(1).getFamilyName());
	}
	
}
