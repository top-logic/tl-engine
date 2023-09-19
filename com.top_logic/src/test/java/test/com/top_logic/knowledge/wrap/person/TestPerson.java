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
import com.top_logic.base.user.UserInterface;
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
		Person formerPerson = PersonManager.getManager().getPersonByName(personName);
		assertNull("Person '" + personName + "' is created in this test.", formerPerson);
		Person newPerson = createPerson(personName);
		assertEquals(newPerson, PersonManager.getManager().getPersonByName(personName));

		// delete the person without using PersonManager
		KnowledgeBase kb = newPerson.getKnowledgeBase();
		Transaction deleteTx = kb.beginTransaction();
		newPerson.tDelete();
		deleteTx.commit();

		Person stillKnownPerson = PersonManager.getManager().getPersonByName(personName);
		assertNull("Ticket #13195: Deleting person does not update person manager.", stillKnownPerson);
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
		} catch (IllegalStateException ex) {
			// expected
		}
	}

	public void testSetLocaleWithoutCountry() throws Throwable {
		Person person = createPerson("person");
		try {
			Locale noCountry = Locale.ITALIAN;
			assertEquals("Test needs locale without country.", StringServices.EMPTY_STRING, noCountry.getCountry());
			person.setLocale(noCountry);
			assertEquals("Country should not be added", StringServices.EMPTY_STRING, person.getLocale().getCountry());

		} finally {
			deletePersonAndUser(person);
		}
	}

	public void testSetLocaleWithoutLanguage() throws Throwable {
		Person person = createPerson("person");
		try {
			Locale noLanguage = new Locale("", Locale.GERMANY.getCountry());
			assertEquals("Test needs locale without language.", StringServices.EMPTY_STRING, noLanguage.getLanguage());
			person.setLocale(noLanguage);
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
			creator.setLocale(null);
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

	/**
	 * Tests that the initial locale of a new locale is that of the creator.
	 */
	public void testSameLocaleAsCreator() throws Throwable {
		TLSubSessionContext session = TLContextManager.getSubSession();
		Person creator = createPerson("creator");
		try {
			session.setPerson(creator);
			testLocalEquality(creator, Locale.CANADA);
			testLocalEquality(creator, Locale.ITALY);
		} finally {
			// set root to session as otherwise deleting fails
			session.setPerson(PersonManager.getManager().getRoot());
			deletePersonAndUser(creator);
		}
	}

	private void testLocalEquality(final Person creator, Locale locale) {
		creator.setLocale(locale);
		Person newPerson = createPerson("newPerson");
		try {
			assertEquals("Created person does not have locale of creator.", creator.getLocale(),
				newPerson.getLocale());
		} finally {
			deletePersonAndUser(newPerson);
		}
	}

	public void testCreatePersonWithGroupName() {
		String groupName = "someGroupName";
		Transaction groupCreation = PersistencyLayer.getKnowledgeBase().beginTransaction();
		Group group = Group.createGroup(groupName);
		assertNotNull("Creating of group with name " + groupName + " should work.", group);
		groupCreation.commit();

		Person createdPerson = createPerson(groupName);
		assertNull("Ticket #9261: Group with name '" + groupName + "' already exists", createdPerson);
	}

	public void testSavePerson() {
		
    	Person root = PersonManager.getManager().getRoot();
		KnowledgeBase kb = root.getKnowledgeBase();
    	
    	assertNotNull(root.getProperties());
    	
		Locale previousLocale = root.getLocale();
    	// Modify back and forth.
		Transaction modifyTx = kb.beginTransaction();
		root.setLocale(Locale.TRADITIONAL_CHINESE);
		modifyTx.commit();
		assertNotEquals(previousLocale, root.getLocale());
    	
		Transaction modifyBackTx = kb.beginTransaction();
		root.setLocale(previousLocale);
		modifyBackTx.commit();
		assertEquals(previousLocale, root.getLocale());
    	
    	assertNotNull(root.getProperties());
    }
    
    public void testGetPerson() throws Exception {
    
        KnowledgeBase kBase = KBSetup.getKnowledgeBase ();
        PersonManager thePM = PersonManager.getManager();
        
        ThreadContext.pushSuperUser();
        try
        {
			KnowledgeObject thePersonKO =
				kBase.getKnowledgeObject("Person", KBUtils.getWrappedObjectName(TestPersonSetup.getTestPerson()));
            Person thePerson  = thePM.getPersonByKO(thePersonKO);

            assertNotNull(thePerson.toString());
            assertNotNull(thePerson.getLocale());  // should be Locale.EN ..    

            Person thePerson2 = thePM.getPersonByKO(thePersonKO);

            // the wrappers must be the same
            assertEquals("The wrappers are not the same.", thePerson, thePerson2);
            // Person is god
			assertEquals(thePerson.getName(), TestPersonSetup.USER_ID);
            // get DOUser from Person
            UserInterface theUser = Person.getUser(thePerson);
			assertNotNull("No user for Person.", theUser);
			assertEquals(TestPersonSetup.USER_ID, theUser.getUserName());
        }
        finally {
            ThreadContext.popSuperUser();   
        }        
    }
    
	/**
	 * This method tests the problem described in Ticket #667.
	 */
	public void testToStringForDeletedKO() throws KnowledgeBaseException {
    	String personName = "dau";
		Person p = PersonManager.getManager().getPersonByName(personName);
    	assertNotNull("There is no person with name " + personName, p);
		Transaction delTx = KBSetup.getKnowledgeBase().beginTransaction();
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
		Person existingTestPerson = PersonManager.getManager().getPersonByName(testPersonName);
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
			assertNotNull(PersonManager.getManager().getPersonByName(personName));

			deletePersonNotUser(testPerson);

			assertFalse(testPerson.tValid());
			assertNull(PersonManager.getManager().getPersonByName(personName));
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
			String dataAccessDeviceID = root.getDataAccessDeviceID();
			String authenticationDeviceID = root.getAuthenticationDeviceID();
			Person testPerson =
				personManager.createPerson(personName, dataAccessDeviceID, authenticationDeviceID, Boolean.FALSE);
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
		PersonManager.getManager().deleteUser(person);
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
