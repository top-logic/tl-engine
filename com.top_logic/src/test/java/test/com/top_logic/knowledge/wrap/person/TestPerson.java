/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.person;

import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Country;
import com.top_logic.util.TLContextManager;

/**
 * Test for {@link com.top_logic.knowledge.wrap.person.Person}.
 *
 * @author  <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class TestPerson extends BasicTestCase {

    public TestPerson (String s) {
        super(s);
    }

	public void testDeletePerson() {
		String personName = "newTestPerson";
		Person formerPerson = Person.byName(personName);
		assertNull("Person '" + personName + "' is created in this test.", formerPerson);
		Person newPerson = createPerson(personName);
		assertEquals(newPerson, Person.byName(personName));

		// delete the person without using PersonManager
		KnowledgeBase kb = newPerson.getKnowledgeBase();
		Transaction deleteTx = kb.beginTransaction();
		newPerson.tDelete();
		deleteTx.commit();

		Person stillKnownPerson = Person.byName(personName);
		assertNull("Ticket #13195: Deleting person does not update person manager.", stillKnownPerson);
		assertFalse("Ticket #13195: ", Person.all().stream().anyMatch(p -> personName.equals(p.getName())));
	}

	/**
	 * Tests that creating a person with a name of an existing person does not work.
	 */
	public void testDuplicatePersonName() {
		Person root = PersonManager.getManager().getRoot();
		assertNotNull(root);

		try {
			createPerson(root.getName());
			fail("Creating a person with the name of a different person should not work.");
		} catch (KnowledgeBaseException ex) {
			// expected
		}
	}

	public void testSetLocaleWithoutCountry() throws Throwable {
		Person person = createPerson("person");
		try {
			Locale noCountry = Locale.ITALIAN;
			assertEquals("Test needs locale without country.", StringServices.EMPTY_STRING, noCountry.getCountry());
			person.setCountry(null);
			person.setLanguage(noCountry);
			assertEquals("Country should not be added", StringServices.EMPTY_STRING, person.getLocale().getCountry());

		} finally {
			deletePersonAndUser(person);
		}
	}

	public void testSetLocaleWithoutLanguage() throws Throwable {
		Person person = createPerson("person");
		try {
			person.setLanguage(null);
			person.setCountry(new Country(Locale.GERMANY.getCountry()));
			assertEquals("Language should not be added", StringServices.EMPTY_STRING, person.getLocale().getLanguage());
		} finally {
			deletePersonAndUser(person);
		}
	}

	/**
	 * Tests that Locale Germany is default when nothing different is set.
	 */
	public void testGermanyDefaultLocale() throws Throwable {
		TLSubSessionContext session = TLContextManager.getSubSession();
		Person creator = createPerson("creator");
		try {
			session.setPerson(creator);
			setLanguageAndCountry(creator, null);
			Person newPerson = createPerson("newPerson");
			try {
				Locale defaultLocale = Locale.GERMANY;
				// Person added variant to locale.
				Locale expected =
					new Locale(defaultLocale.getLanguage(), defaultLocale.getCountry());
				assertEquals("Created person does not have locale of creator.", expected, newPerson.getLocale());
			} finally {
				deletePersonAndUser(newPerson);
			}
		} finally {
			// set root to session as otherwise deleting fails
			session.setPerson(PersonManager.getManager().getRoot());
			deletePersonAndUser(creator);
		}
	}

	private void setLanguageAndCountry(Person p, Locale locale) {
		if (locale == null) {
			p.setLanguage(null);
			p.setCountry(null);
		} else {
			p.setLanguage(locale);
			p.setCountry(new Country(locale.getCountry()));
		}
	}

	public void testCreatePersonWithGroupName() {
		String groupName = "someGroupName";
		Transaction groupCreation = PersistencyLayer.getKnowledgeBase().beginTransaction();
		Group group = Group.createGroup(groupName);
		assertNotNull("Creating of group with name " + groupName + " should work.", group);
		groupCreation.commit();

		try {
			createPerson(groupName);
			fail("Ticket #9261: Group with name '" + groupName + "' already exists");
		} catch (KnowledgeBaseException ex) {
			// expected
		}
	}

	public void testSavePerson() {
		
    	Person root = PersonManager.getManager().getRoot();
		KnowledgeBase kb = root.getKnowledgeBase();
    	
		Locale previousLocale = root.getLocale();
    	// Modify back and forth.
		Transaction modifyTx = kb.beginTransaction();
		setLanguageAndCountry(root, Locale.TRADITIONAL_CHINESE);
		modifyTx.commit();
		assertNotEquals(previousLocale, root.getLocale());
    	
		Transaction modifyBackTx = kb.beginTransaction();
		setLanguageAndCountry(root, previousLocale);
		modifyBackTx.commit();
		assertEquals(previousLocale, root.getLocale());
    }
    
    public void testGetPerson() throws Exception {
    
        KnowledgeBase kBase = KBSetup.getKnowledgeBase ();
        PersonManager thePM = PersonManager.getManager();
        
        ThreadContext.pushSuperUser();
        try
        {
			KnowledgeObject thePersonKO =
				kBase.getKnowledgeObject("Person", KBUtils.getWrappedObjectName(TestPersonSetup.getTestPerson()));
            Person thePerson  = thePersonKO.getWrapper();

            assertNotNull(thePerson.toString());
            assertNotNull(thePerson.getLocale());  // should be Locale.EN ..    

            Person thePerson2 = thePersonKO.getWrapper();

            // the wrappers must be the same
            assertEquals("The wrappers are not the same.", thePerson, thePerson2);
            // Person is god
			assertEquals(thePerson.getName(), TestPersonSetup.USER_ID);
        }
        finally {
            ThreadContext.popSuperUser();   
        }        
    }
    
	/**
	 * This method tests the problem described in Ticket #667.
	 */
	public void testToStringForDeletedKO() throws KnowledgeBaseException {
		String personName = "testToStringForDeletedKO";

		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		Transaction createTx = kb.beginTransaction();
		Person p = Person.create(kb, personName, null);
		createTx.commit();

		Transaction delTx = kb.beginTransaction();
		p.tDelete();
		delTx.commit();
		try {
			p.toString();
		}
		catch(Exception e) {
			fail("can not call toString() for deleted person " + personName + ": " + e.getLocalizedMessage());
		}
	}

	/**
	 * Tests that refetch of person works also if person is <b>not</b> fetched by
	 * {@link PersonManager} but by the general {@link WrapperFactory} mechanism.
	 * 
	 * <p>
	 * See Ticket #6373
	 * </p>
	 */
	public void testRefetchPerson() throws DataObjectException {
		String testPersonName = "testPerson";
		Person existingTestPerson = Person.byName(testPersonName);
		if (existingTestPerson != null) {
			fail("Expected no person with name " + testPersonName);
		}

		/* Creating may not be done using personManager, e.g. one cluster node creates it (using
		 * person manager), and the creating is propagated to another node. */
		KnowledgeObject testPerson = createTestPersonKO(testPersonName);
		try {
			/* Wrapper may be created by WrapperFactory directly when person is target of some
			 * association. In that case no PersonManager is used. */
			Wrapper personWrapper = WrapperFactory.getWrapper(testPerson);
			try {
				/* May be called by KnowledgeBaseRefetch, when person is changed in different cluster node */
				personWrapper.refetchWrapper();
			} catch (NullPointerException ex) {
				fail("Ticket #6373", ex);
			}
		} finally {
			deleteTestPersonKO(testPerson);
		}
	}

	/**
	 * Deletes the given {@link KnowledgeObject}.
	 */
	private void deleteTestPersonKO(KnowledgeObject testPerson) throws DataObjectException {
		KnowledgeBase kb = testPerson.getKnowledgeBase();
		Transaction deleteTX = kb.beginTransaction();
		testPerson.delete();
		deleteTX.commit();
	}

	/**
	 * Creates a KNowledgeObject of type {@link Person#OBJECT_NAME} with the given name as
	 * {@link AbstractWrapper#NAME_ATTRIBUTE name attribute}
	 */
	private KnowledgeObject createTestPersonKO(String testPersonName) throws DataObjectException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction createTX = kb.beginTransaction();
		KnowledgeObject testPerson = kb.createKnowledgeObject(Person.OBJECT_NAME);
		testPerson.setAttributeValue(Person.NAME_ATTRIBUTE, testPersonName);
		createTX.commit();
		return testPerson;
	}

	public void testHistoricPersonFullName_Ticket6255() throws Throwable {
		String personName = "TestPersonFullName";
		Person testPerson = createPerson(personName);

		MOClass table = (MOClass) testPerson.tTable();
		if (table.isVersioned()) {
			// Only test in a persistency layer that supports versions.
			AssertNoErrorLogListener logListener = new AssertNoErrorLogListener();

			Revision createRevision = WrapperHistoryUtils.getCreateRevision(testPerson);
			Person testPersonHistoric = WrapperHistoryUtils.getWrapper(createRevision, testPerson);
			assertNotNull(Person.byName(personName));

			deletePersonNotUser(testPerson);

			assertFalse(testPerson.tValid());
			assertNull(Person.byName(personName));
			assertNotNull(testPersonHistoric.getFullName());

			logListener.assertNoErrorLogged("Assertion: No error is logged during test execution.");
		}
	}

	private void deletePersonNotUser(Person person) throws Throwable {
		Transaction transactionDeletePerson = person.getKnowledgeBase().beginTransaction();
		try {
			// If the user of the person is deleted, the tests cannot reproduce the problem in #6255
			// PersonManager.getManager().deleteUser(person);
			person.tDelete();
			transactionDeletePerson.commit();
		} finally {
			transactionDeletePerson.rollback();
		}
	}

	/**
	 * Creates a new {@link Person} with the given name.
	 * 
	 * @see #deletePersonAndUser(Person)
	 */
	public static Person createPerson(String personName) {
		PersonManager personManager = PersonManager.getManager();
		Person root = personManager.getRoot();
		Transaction transactionCreatePerson = root.getKnowledgeBase().beginTransaction();
		try {
			String authenticationDeviceID = root.getAuthenticationDeviceID();
			Person testPerson =
				Person.create(PersistencyLayer.getKnowledgeBase(), personName, authenticationDeviceID);
			transactionCreatePerson.commit();
			return testPerson;
		} finally {
			transactionCreatePerson.rollback();
		}
	}

	/**
	 * Deletes the given {@link Person} formerly created with {@link #createPerson(String)}.
	 */
	public static void deletePersonAndUser(Person person) {
		Transaction deleteTx = person.getKnowledgeBase().beginTransaction();
		PersonManager r = PersonManager.getManager();
		person.tDelete();
		person.tDelete();
		deleteTx.commit();
	}

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		final Test innerTest = PersonManagerSetup.createPersonManagerSetup(
			TestPersonSetup.wrap(new TestSuite(TestPerson.class)));
		return innerTest;
    }

}
