/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.person;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.util.sched.model.TaskTestUtil;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.col.ConstantProvider;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.exceptions.InvalidWrapperException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.RefreshUsersTask;
import com.top_logic.knowledge.wrap.person.RefreshUsersTask.Config;

/**
 * {@link TestCase} for {@link PersonManager}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestTLPersonManager extends BasicTestCase {

	private static final String SECOND_DEFAULT_KB_ID = "Default2";

	private static final String AUTHENTICATION_DEVICE_ID = "dbSecurity";

	private CollectingLogListener logListener;

	private KnowledgeBase firstNodeKnowledgeBase;

	private PersonManager firstNodePersonManager;

	private RefreshUsersTask firstNodeRefreshUsersTask;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logListener = createLogListener();

		firstNodeKnowledgeBase = createFirstNodeKnowlegdeBase();
		firstNodePersonManager = createPersonManager(firstNodeKnowledgeBase);
		firstNodeRefreshUsersTask = createFirstNodeRefreshUsersTask(firstNodeKnowledgeBase, firstNodePersonManager);
	}

	private KnowledgeBase createFirstNodeKnowlegdeBase() {
		return KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
	}

	private KnowledgeBase createSecondNodeKnowlegdeBase() {
		return KnowledgeBaseFactory.getInstance().getKnowledgeBase(SECOND_DEFAULT_KB_ID);
	}

	private PersonManager createPersonManager(KnowledgeBase knowledgeBase) {
		PersonManager.Config configItem = TypedConfiguration.newConfigItem(PersonManager.Config.class);
		ConfigurationDescriptor descriptor = configItem.descriptor();
		configItem.update(descriptor.getProperty(PersonManager.Config.TL_SECURITY_DEVICE_MANAGER_PROPERTY),
			new ConstantProvider<>(
				TLSecurityDeviceManager.getInstance()));
		return new PersonManager(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, configItem);
	}

	private CollectingLogListener createLogListener() {
		Set<Level> prioritiesToListenTo = new HashSet<>();
		prioritiesToListenTo.add(Level.WARN);
		prioritiesToListenTo.add(Level.ERROR);
		return new CollectingLogListener(prioritiesToListenTo, true);
	}

	@Override
	protected void tearDown() throws Exception {
		logListener.deactivate();
		super.tearDown();
	}

	public void testCreatePerson() throws RefetchTimeout {
		MockPersonDataAccessDevice mockPersonDataAccessDevice = getMockPersonDataAccessDevice();
		UserInterface user = newUser();
		mockPersonDataAccessDevice.addUserData(user);

		Transaction createTx = firstNodeKnowledgeBase.beginTransaction();
		Person fooPerson = firstNodePersonManager.getOrCreatePersonForUser(user.getUserName(),
			Boolean.TRUE, AUTHENTICATION_DEVICE_ID);
		createTx.commit();

		assertTrue("Ticket #13195: ", firstNodePersonManager.getAllPersons().contains(fooPerson));
		assertTrue("Ticket #13195: ", firstNodePersonManager.getAllPersonsSet().contains(fooPerson));
		assertTrue("Ticket #13195: ", firstNodePersonManager.getAllPersonsList().contains(fooPerson));
		assertTrue("Ticket #13195: ", firstNodePersonManager.getAllAlivePersons().contains(fooPerson));
	}

	public void testDeletePerson() throws RefetchTimeout {
		MockPersonDataAccessDevice mockPersonDataAccessDevice = getMockPersonDataAccessDevice();
		UserInterface user = newUser();
		mockPersonDataAccessDevice.addUserData(user);

		Transaction createTx = firstNodeKnowledgeBase.beginTransaction();
		Person fooPerson = firstNodePersonManager.getOrCreatePersonForUser(user.getUserName(),
			Boolean.TRUE, AUTHENTICATION_DEVICE_ID);
		createTx.commit();
		String personName = fooPerson.getName();

		Transaction deleteTx = firstNodeKnowledgeBase.beginTransaction();
		fooPerson.tDelete();
		deleteTx.commit();

		firstNodeKnowledgeBase.refetch();
		assertFalse("Ticket #13195: ", firstNodePersonManager.getAllPersons().contains(fooPerson));
		assertFalse("Ticket #13195: ", firstNodePersonManager.getAllPersonsSet().contains(fooPerson));
		assertFalse("Ticket #13195: ", firstNodePersonManager.getAllPersonsList().contains(fooPerson));
		assertFalse("Ticket #13195: ", firstNodePersonManager.getAllAlivePersons().contains(fooPerson));
	}

	public void testLocalePreservationOnRefetch() {
		MockPersonDataAccessDevice mockPersonDataAccessDevice = getMockPersonDataAccessDevice();
		UserInterface fooUser = newUser();
		mockPersonDataAccessDevice.addUserData(fooUser);

		assertNull(firstNodePersonManager.getPersonByName(fooUser.getUserName(), firstNodeKnowledgeBase));
		Transaction tx = firstNodeKnowledgeBase.beginTransaction();
		Person fooPerson = firstNodePersonManager.getOrCreatePersonForUser(fooUser.getUserName(),
			Boolean.TRUE, AUTHENTICATION_DEVICE_ID);
		fooPerson.setLocale(Locale.CHINA);
		tx.commit();

		Locale dynamicLocale = fooPerson.getLocale();
		// Dynamic locale may differ from set locale because a variant is included in Locale.
		// assertEquals(Locale.CHINA, dynamicLocale);
		Person refetchedPerson = firstNodePersonManager
			.getOrCreatePersonForUser(fooUser.getUserName(), Boolean.TRUE, AUTHENTICATION_DEVICE_ID);
		assertEquals("Ticket #11923: Fetching person for user changes locale.", dynamicLocale,
			refetchedPerson.getLocale());
	}

	public void testRemoveUsersByRefreshUsersTask() {
		MockPersonDataAccessDevice mockPersonDataAccessDevice = getMockPersonDataAccessDevice();

		UserInterface rootUser = mockPersonDataAccessDevice.getRootUser();
		List<UserInterface> users = new LinkedList<>();
		UserInterface fooUser = newUser();
		UserInterface barUser = newUser();
		users.add(fooUser);
		users.add(barUser);
		// root user is needed for refetch task, otherwise root is deleted
		users.add(rootUser);

		mockPersonDataAccessDevice.setAllUserData(users);
		firstNodeRefreshUsersTask.run();
		assertAllRegisteredUsers(rootUser, fooUser, barUser);

		users.remove(0);

		mockPersonDataAccessDevice.setAllUserData(users);
		firstNodeRefreshUsersTask.run();
		assertAllRegisteredUsers(rootUser, barUser);
	}

	public void testClusterRemoveUsersByRefreshUsersTask() throws RefetchTimeout {
		MockPersonDataAccessDevice mockPersonDataAccessDevice = getMockPersonDataAccessDevice();

		UserInterface rootUser = mockPersonDataAccessDevice.getRootUser();
		List<UserInterface> users = new LinkedList<>();
		UserInterface fooUser = newUser();
		UserInterface barUser = newUser();
		users.add(fooUser);
		users.add(barUser);
		// root user is needed for refetch task, otherwise root is deleted
		users.add(rootUser);
		
		mockPersonDataAccessDevice.setAllUserData(users);
		firstNodeRefreshUsersTask.run();
		assertAllRegisteredUsers(firstNodePersonManager, rootUser, fooUser, barUser);

		users.remove(0);
		mockPersonDataAccessDevice.setAllUserData(users);
		
		firstNodeRefreshUsersTask.run();
		assertAllRegisteredUsers(firstNodePersonManager, rootUser, barUser);

		assertNoInvalidWrapperExceptionOccured("user update of PersonManager");
	}

	private void assertNoInvalidWrapperExceptionOccured(String operationDescription) {
		List<LogEntry> logEntries = logListener.getLogEntries();
		for (LogEntry logEntry : logEntries) {
			if (logEntry.getException().hasValue() && logEntry.getException().get() instanceof InvalidWrapperException) {
				fail("During " + operationDescription + " no InvalidWrapperException should occur!");
			}
		}
	}

	private UserInterface newUser() {
		return MockUserInterface.newUserInterface(AUTHENTICATION_DEVICE_ID);
	}

	private RefreshUsersTask<?> createFirstNodeRefreshUsersTask(KnowledgeBase knowledgeBase,
			PersonManager personManager) {
		return createRefreshUsersTask("FirstNodeRefreshUsersTask", knowledgeBase, personManager);
	}

	private RefreshUsersTask<?> createSecondNodeRefreshUsersTask(KnowledgeBase knowledgeBase,
			PersonManager personManager) {
		return createRefreshUsersTask("SecondNodeRefreshUsersTask", knowledgeBase, personManager);
	}

	private RefreshUsersTask createRefreshUsersTask(String taskName, final KnowledgeBase knowledgeBase, final PersonManager personManager) {
		Config<?> config = TypedConfiguration.newConfigItem(RefreshUsersTask.Config.class);
		config.setName(taskName);
		config.setPersonManagerProvider(new ConstantProvider<>(personManager));
		config.setKnowledgeBaseProvider(new ConstantProvider<>(knowledgeBase));
		RefreshUsersTask<?> task = TypedConfigUtil.createInstance(config);
		return TaskTestUtil.initTaskLog(task);
	}

	private MockPersonDataAccessDevice getMockPersonDataAccessDevice() {
		PersonDataAccessDevice dataAccessDevice =
			TLSecurityDeviceManager.getInstance().getDataAccessDevice(AUTHENTICATION_DEVICE_ID);
		assertInstanceof(dataAccessDevice, MockPersonDataAccessDevice.class);
		return (MockPersonDataAccessDevice) dataAccessDevice;
	}

	private void assertAllRegisteredUsers(UserInterface... users) {
		assertAllRegisteredUsers(firstNodePersonManager, users);
	}

	private void assertAllRegisteredUsers(PersonManager personManager, UserInterface... users) {
		Collection<Person> registeredPersons = personManager.getAllPersonsSet();

		for (UserInterface user : users) {
			Person personForUser = null;
			for (Person person : registeredPersons) {
				assertTrue("PersonManager should not contain invalid persons!", person.tValid());
				if (person.getName().equals(user.getUserName())) {
					personForUser = person;
				}
			}
			assertNotNull("User '" + user.getUserName()
				+ "' is requested to be registered in PersonManager, but was not found!",
				personForUser);
			registeredPersons.remove(personForUser);
		}

		if (!registeredPersons.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			Iterator<Person> personIterator = registeredPersons.iterator();
			while (personIterator.hasNext()) {
				Person person = personIterator.next();
				builder.append(person.getName());
				if (personIterator.hasNext()) {
					builder.append(", ");
				}
			}
			builder.append("]");
			fail("Following users should not be registered in PersonManager, but where found: " + builder.toString());
		}
	}

	/**
	 * Return the suite of tests to perform.
	 */
	public static Test suite() {
		Test testClassSetup = new TestSuite(TestTLPersonManager.class);
		Test personManagerSetup = PersonManagerSetup.createPersonManagerSetup(testClassSetup);
		CustomPropertiesSetup customPropertiesSetup = new CustomPropertiesSetup(personManagerSetup,
			CustomPropertiesDecorator.createFileName(TestTLPersonManager.class), true);
		return TestUtils.doNotMerge(TLTestSetup.createTLTestSetup(customPropertiesSetup));
	}

}
