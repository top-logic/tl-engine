/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.accesscontrol;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.LoginFailure;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.base.accesscontrol.RetryDeniedException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.component.configuration.I18NConstants;

/**
 * Tests {@link LoginFailuresModule#attempt(String, ResKey1, LoginFailuresModule.Attempt)}, the
 * back-off guarding a check of a credential.
 */
public class TestLoginFailuresModule extends TestCase {

	private static final String KEY = "test-login-failures@127.0.0.1";

	private static final ResKey1 TOO_MANY = I18NConstants.ERROR_TOO_MANY_LOGIN_ATTEMPS__TIMEOUT;

	private int _checks;

	@Override
	protected void tearDown() throws Exception {
		module().notifyLoginSuccessed(KEY);
		super.tearDown();
	}

	/**
	 * Tests that a rejected credential is recorded as failure and an accepted one clears the
	 * failures.
	 */
	public void testFailureRecordedSuccessClears() {
		assertFalse(attempt(false));
		LoginFailure failure = module().getFailureFor(KEY);
		assertNotNull("The rejection is recorded.", failure);
		assertEquals(1, failure.getFailureCnt());

		assertFalse(attempt(false));
		assertEquals(2, module().getFailureFor(KEY).getFailureCnt());

		assertTrue(attempt(true));
		assertNull("The accepted credential clears the failures.", module().getFailureFor(KEY));
		assertEquals(3, _checks);
	}

	/**
	 * Tests that repeated failures make the key back off, and that while backing off the check is
	 * not run and the attempt is refused with the seconds to wait.
	 */
	public void testRefusedWhileBackingOff() {
		while (module().getFailureFor(KEY) == null || module().getFailureFor(KEY).allowRetry()) {
			assertFalse(attempt(false));
			assertTrue("The key backs off after a few failures.", _checks < 10);
		}
		int checksBeforeBackOff = _checks;

		try {
			attempt(true);
			fail("An attempt while backing off is refused.");
		} catch (RetryDeniedException ex) {
			ResKey message = ex.getErrorKey();
			assertEquals(TOO_MANY.getKey(), message.getKey());
			long seconds = ((Number) message.arguments()[0]).longValue();
			assertTrue("The message names the seconds to wait: " + seconds, seconds > 0);
			assertTrue(ex.getRetryTimeout() > 0);
		}
		assertEquals("The check is not run while backing off.", checksBeforeBackOff, _checks);
		assertEquals("A refused attempt is not counted.", checksBeforeBackOff,
			module().getFailureFor(KEY).getFailureCnt());
	}

	/**
	 * Tests that a throwing check records nothing.
	 */
	public void testThrowingCheckRecordsNothing() {
		try {
			module().attempt(KEY, TOO_MANY, () -> {
				throw new IllegalStateException("check failed");
			});
			fail("The exception of the check is passed on.");
		} catch (IllegalStateException ex) {
			// Expected.
		}
		assertNull(module().getFailureFor(KEY));
	}

	/**
	 * Tests the key of an account checked without a request.
	 */
	public void testAccountKey() {
		assertEquals("Without request, the key is the account name.", "root",
			LoginFailuresModule.accountKey("Root", null));
	}

	private boolean attempt(boolean accept) {
		return module().attempt(KEY, TOO_MANY, () -> {
			_checks++;
			return accept;
		});
	}

	private static LoginFailuresModule<?> module() {
		return LoginFailuresModule.getInstance();
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestLoginFailuresModule.class, LoginFailuresModule.Module.INSTANCE));
	}

}
