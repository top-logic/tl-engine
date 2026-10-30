/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.login.DisableMfaAction;
import com.top_logic.layout.view.login.I18NConstants;
import com.top_logic.layout.view.login.SecondFactor;
import com.top_logic.layout.view.login.TotpSecondFactor;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests {@link DisableMfaAction}: the authenticator app is taken off an account, and an account
 * required to use a second factor drops it only while it keeps another one.
 */
public class TestDisableMfaAction extends BasicTestCase {

	/** The TOTP secret of an account that has the app set up; never decrypted, only its presence counts. */
	private static final Password SECRET = new Password("not-decrypted");

	private final List<Person> _accounts = new ArrayList<>();

	private TotpSecondFactor _totp;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TotpSecondFactor.Config<?> config = TypedConfiguration.newConfigItem(TotpSecondFactor.Config.class);
		config.setName("totp");
		config.update(config.descriptor().getProperty(TotpSecondFactor.Config.LABEL), ResKey.text("App"));
		_totp = (TotpSecondFactor) new DefaultInstantiationContext(TestDisableMfaAction.class).getInstance(config);
	}

	@Override
	protected void tearDown() throws Exception {
		for (Person account : _accounts) {
			if (account.isAlive()) {
				TestPerson.deletePersonAndUser(account);
			}
		}
		_accounts.clear();
		super.tearDown();
	}

	/** An account that may log in without a second factor logs in with the password alone. */
	public void testOptionalAccountSwitchedOff() {
		Person account = account("disableOptional", MfaRequirement.OPTIONAL);
		assertTrue(_totp.isEnrolled(account));

		ResKey message = DisableMfaAction.disable(account, List.of(_totp, factor(false)));

		assertEquals(I18NConstants.MFA_DISABLED__USER.fill(account.getName()), message);
		assertNull(account.getMFASecret());
		assertEquals(MfaRequirement.OPTIONAL, account.getMFARequirement());
	}

	/** A required account keeping another second factor drops the app and stays required. */
	public void testRequiredAccountWithOtherFactor() {
		Person account = account("disableRequiredOther", MfaRequirement.REQUIRED);

		assertNull(DisableMfaAction.refusal(account, List.of(_totp, factor(true))));
		ResKey message = DisableMfaAction.disable(account, List.of(_totp, factor(true)));

		assertEquals(I18NConstants.MFA_TOTP_REMOVED__USER.fill(account.getName()), message);
		assertNull(account.getMFASecret());
		assertEquals(MfaRequirement.REQUIRED, account.getMFARequirement());
	}

	/** A required account whose only second factor is the app keeps it. */
	public void testRequiredAccountAloneRefused() {
		Person account = account("disableRequiredAlone", MfaRequirement.REQUIRED);
		ResKey expected = I18NConstants.ERROR_MFA_REQUIRED__USER.fill(account.getName());

		assertEquals("The app itself does not count as another second factor.",
			expected, DisableMfaAction.refusal(account, List.of(_totp)));
		assertEquals("A method the account has not set up does not count.",
			expected, DisableMfaAction.refusal(account, List.of(_totp, factor(false))));

		try {
			DisableMfaAction.disable(account, List.of(_totp, factor(false)));
			fail("The only second factor of a required account must not be switched off.");
		} catch (TopLogicException ex) {
			assertEquals(expected, ex.getErrorKey());
		}
		assertNotNull(account.getMFASecret());
		assertEquals(MfaRequirement.REQUIRED, account.getMFARequirement());
	}

	private Person account(String name, MfaRequirement requirement) {
		Person account = TestPerson.createPerson(name);
		_accounts.add(account);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			account.setMFARequirement(requirement);
			account.setMFASecret(SECRET);
			tx.commit();
		}
		return account;
	}

	/**
	 * A second factor method that every account has or has not set up.
	 */
	private static SecondFactor factor(boolean enrolled) {
		return new SecondFactor() {
			@Override
			public ResKey getLabel() {
				return ResKey.text("Other factor");
			}

			@Override
			public ThemeImage getIcon() {
				return null;
			}

			@Override
			public boolean isEnrolled(Person account) {
				return enrolled;
			}

			@Override
			public boolean canEnroll(Person account) {
				return true;
			}

			@Override
			public void verify(ReactContext context, Person account) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void enroll(ReactContext context, Person account) {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * The suite, with a knowledge base holding the accounts.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestDisableMfaAction.class), PersonManager.Module.INSTANCE));
	}

}
