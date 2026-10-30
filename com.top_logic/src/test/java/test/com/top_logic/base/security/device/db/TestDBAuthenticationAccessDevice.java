/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security.device.db;

import static com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.db.DBAuthenticationAccessDevice;
import com.top_logic.base.security.password.I18NConstants;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of the password reuse check of {@link DBAuthenticationAccessDevice#setPassword(Person, char[])}.
 */
public class TestDBAuthenticationAccessDevice extends BasicTestCase {

	private static final String DEVICE_ID = "dbSecurity";

	private static final String FIRST_PASSWORD = "First-pwd-1";

	private static final String SECOND_PASSWORD = "Second-pwd-2";

	private static final String THIRD_PASSWORD = "Third-pwd-3";

	private DBAuthenticationAccessDevice _device;

	private Person _account;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_device = (DBAuthenticationAccessDevice) TLSecurityDeviceManager.getInstance().getAuthenticationDevice(DEVICE_ID);
		assertTrue("Test requires a repeat cycle.", _device.getPasswordValidator().getPwdRepeatCycle() > 0);

		KnowledgeBase kb = kb();
		try (Transaction tx = kb.beginTransaction(NO_COMMIT_MESSAGE)) {
			_account = Person.create(kb, "pwdTestUser", _device);
			tx.commit();
		}
		setPassword(_device, FIRST_PASSWORD);
	}

	@Override
	protected void tearDown() throws Exception {
		KnowledgeBase kb = kb();
		try (Transaction tx = kb.beginTransaction(NO_COMMIT_MESSAGE)) {
			_account.tDelete();
			tx.commit();
		}
		_account = null;
		_device = null;
		super.tearDown();
	}

	/**
	 * Setting the password that is currently set is refused.
	 */
	public void testCurrentPasswordRefused() {
		assertUsedBefore(_device, FIRST_PASSWORD);
		assertAuthenticates(FIRST_PASSWORD);
	}

	/**
	 * Setting a password that was never used before is accepted.
	 */
	public void testFreshPasswordAccepted() {
		setPassword(_device, SECOND_PASSWORD);
		assertAuthenticates(SECOND_PASSWORD);
	}

	/**
	 * Setting a password from the history within the repeat cycle is refused.
	 */
	public void testPreviousPasswordRefused() {
		setPassword(_device, SECOND_PASSWORD);
		setPassword(_device, THIRD_PASSWORD);

		assertUsedBefore(_device, FIRST_PASSWORD);
		assertUsedBefore(_device, SECOND_PASSWORD);
		assertUsedBefore(_device, THIRD_PASSWORD);
		assertAuthenticates(THIRD_PASSWORD);
	}

	/**
	 * Without a repeat cycle, setting the current password again is accepted.
	 */
	public void testNoRepeatCycle() {
		DBAuthenticationAccessDevice.Config config = TypedConfiguration.copy(_device.getConfig());
		PasswordValidator.Config validatorConfig = (PasswordValidator.Config) config.getPasswordValidator();
		validatorConfig.update(validatorConfig.descriptor().getProperty(PasswordValidator.Config.REPEAT_CYCLE), 0);
		DBAuthenticationAccessDevice device =
			(DBAuthenticationAccessDevice) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		assertEquals(0, device.getPasswordValidator().getPwdRepeatCycle());

		setPassword(device, FIRST_PASSWORD);
		assertAuthenticates(FIRST_PASSWORD);
	}

	private void assertUsedBefore(DBAuthenticationAccessDevice device, String password) {
		try {
			setPassword(device, password);
			fail("Setting a password used before must be refused: " + password);
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.PWD_USED_BEFORE, ex.getErrorKey());
		}
	}

	private void setPassword(DBAuthenticationAccessDevice device, String password) {
		KnowledgeBase kb = kb();
		try (Transaction tx = kb.beginTransaction(NO_COMMIT_MESSAGE)) {
			device.setPassword(_account, password.toCharArray());
			tx.commit();
		}
	}

	private void assertAuthenticates(String password) {
		assertTrue("Password must be set: " + password,
			_device.authentify(LoginCredentials.fromUserAndPassword(_account,
				password.toCharArray())));
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestDBAuthenticationAccessDevice.class));
	}

}
