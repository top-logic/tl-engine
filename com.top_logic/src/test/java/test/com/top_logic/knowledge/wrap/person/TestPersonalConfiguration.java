/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.person;

import jakarta.servlet.http.HttpSessionBindingListener;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.knowledge.wrap.person.TransientPersonalConfiguration;
import com.top_logic.util.TLContext;

/**
 * The class {@link TestPersonalConfiguration} tests the {@link PersonalConfiguration}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestPersonalConfiguration extends BasicTestCase {

	private Person _oldPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Person root = PersonManager.getManager().getRoot();
		TLContext context = TLContext.getContext();
		_oldPerson = context.getCurrentPersonWrapper();
		context.setCurrentPerson(root);
		boolean hasDeletedObject = deletePersonalConfigurationWrapper();
		assertFalse("Some test before has not removed the personal configuration.", hasDeletedObject);
	}

	private boolean deletePersonalConfigurationWrapper() {
		TLContext context = TLContext.getContext();
		Person currentPerson = context.getCurrentPersonWrapper();
		assertNotNull("Expected a person is available in TLContext.", currentPerson);
		PersonalConfigurationWrapper persistentPC =
			PersonalConfigurationWrapper.getPersonalConfiguration(currentPerson);
		if (persistentPC != null) {
			Transaction tx = persistentPC.getKnowledgeBase().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
			persistentPC.tDelete();
			tx.commit();
			return true;
		}
		return false;
	}

	@Override
	protected void tearDown() throws Exception {
		deletePersonalConfigurationWrapper();
		TLContext.getContext().setCurrentPerson(_oldPerson);
		super.tearDown();
	}

	/**
	 * Test setting and storing of simple values in personal configuration.
	 */
	public void testInstallPersonalConfiguration() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		pc.setValue("key", "MyFunnyValue");
		assertEquals("MyFunnyValue", pc.getValue("key"));
		
		makeConfigurationPersistent(pc);

		PersonalConfiguration refetchedPC = PersonalConfiguration.getPersonalConfiguration();
		assertEquals("Value was not made persistent.", "MyFunnyValue", refetchedPC.getValue("key"));
	}

	/**
	 * Test setting and storing of simple values in personal configuration.
	 */
	public void testChangePersonalConfiguration() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		pc.setValue("key", "MyFunnyValue");
		assertEquals("MyFunnyValue", pc.getValue("key"));

		// Creates the persistent Wrapper
		makeConfigurationPersistent(pc);

		// Copies values from persistent Wrapper into PC
		PersonalConfiguration refetchedPC = PersonalConfiguration.getPersonalConfiguration();
		refetchedPC.setValue("key", "new value");
		assertEquals("new value", refetchedPC.getValue("key"));

		// Creates the persistent Wrapper with updated values.
		makeConfigurationPersistent(refetchedPC);

		PersonalConfiguration refetchedPC2 = PersonalConfiguration.getPersonalConfiguration();
		refetchedPC2.getValue("key");
		assertEquals("Value was not made persistent.", "new value", refetchedPC2.getValue("key"));
	}

	/**
	 * Tests that the personal configuration of a regular account reaches the database when it is
	 * stored.
	 */
	public void testRegularAccountIsPersisted() {
		Person root = TLContext.getContext().getCurrentPersonWrapper();
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		pc.setValue("key", "regularValue");

		PersonalConfiguration.storePersonalConfiguration();

		PersonalConfigurationWrapper persistent = PersonalConfigurationWrapper.getPersonalConfiguration(root);
		assertNotNull("Personal configuration of a regular account was not stored.", persistent);
		assertEquals("regularValue", persistent.getValue("key"));

		TLContext.getContext().resetPersonalConfiguration();
		assertEquals("Stored value not loaded again.", "regularValue",
			PersonalConfiguration.getPersonalConfiguration().getValue("key"));
	}

	/**
	 * Tests that the personal configuration of the anonymous account stays usable in the session
	 * but is never written to the database, neither by an explicit store nor at session end.
	 */
	public void testAnonymousAccountIsNotPersisted() {
		TLContext context = TLContext.getContext();
		Person anonymous = PersonManager.getManager().getAnonymous();
		assertNotNull("No anonymous account.", anonymous);
		assertTrue(PersonManager.getManager().isAnonymous(anonymous));

		Person root = context.getCurrentPersonWrapper();
		context.setCurrentPerson(anonymous);
		try {
			assertFalse("Some test before has stored a personal configuration for the anonymous account.",
				deletePersonalConfigurationWrapper());
			assertTrue(TLContext.isAnonymous());

			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			pc.setValue("key", "anonymousValue");

			PersonalConfiguration.storePersonalConfiguration();

			assertEquals("Value of the anonymous session lost on store.", "anonymousValue",
				PersonalConfiguration.getPersonalConfiguration().getValue("key"));
			assertNull("Personal configuration of the anonymous account was stored.",
				PersonalConfigurationWrapper.getPersonalConfiguration(anonymous));

			// Session end.
			context.informUnboundListeners();
			assertNull("Personal configuration of the anonymous account was stored at session end.",
				PersonalConfigurationWrapper.getPersonalConfiguration(anonymous));

			// A fresh load does not see the value of the former anonymous session.
			context.resetPersonalConfiguration();
			assertNull("Value of an anonymous session reached the shared anonymous account.",
				PersonalConfiguration.getPersonalConfiguration().getValue("key"));
		} finally {
			deletePersonalConfigurationWrapper();
			context.setCurrentPerson(root);
		}
	}

	/**
	 * Currently the personal configuration is made persistent, when session goes invalid. As this
	 * can not be provoked in JUnit tests, the
	 * {@link HttpSessionBindingListener#valueUnbound(jakarta.servlet.http.HttpSessionBindingEvent)}
	 * is called which do the work.
	 */
	private void makeConfigurationPersistent(PersonalConfiguration pc) {
		assertInstanceof(pc, TransientPersonalConfiguration.class);
		TLContext.getContext().storePersonalConfiguration();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPersonalConfiguration}.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestPersonalConfiguration.class));
}

}

