/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import junit.framework.Test;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.SecurityDevice;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Tests for the TL-Script functions about accounts.
 *
 * @see com.top_logic.model.search.expr.config.operations.AccountFunctions
 */
@SuppressWarnings("javadoc")
public class TestAccountFunctions extends AbstractSearchExpressionTest {

	public void testPasswordChangeAllowed() throws Exception {
		assertEquals(Boolean.TRUE,
			eval("account -> $account.accountPasswordChangeAllowed()", account(new Device(true))));
	}

	public void testPasswordChangeRefusedByDevice() throws Exception {
		assertEquals("A device keeping the password elsewhere allows no change.", Boolean.FALSE,
			eval("account -> $account.accountPasswordChangeAllowed()", account(new Device(false))));
	}

	public void testPasswordChangeWithoutDevice() throws Exception {
		assertEquals("An account without a device has no password to change.", Boolean.FALSE,
			eval("account -> $account.accountPasswordChangeAllowed()", account(null)));
	}

	public void testCallFormWithArgument() throws Exception {
		assertEquals("The function is reachable as a call as well as chained to its account.", Boolean.TRUE,
			eval("account -> accountPasswordChangeAllowed($account)", account(new Device(true))));
	}

	/** An account authenticated by the given device, or by none. */
	private static Person account(AuthenticationDevice device) {
		return new Account(device);
	}

	/**
	 * An account that answers with the device it was created with, so that what the function reports
	 * depends on nothing but the device.
	 */
	private static class Account extends Person {

		private final AuthenticationDevice _device;

		Account(AuthenticationDevice device) {
			super(null);
			_device = device;
		}

		@Override
		public AuthenticationDevice getAuthenticationDevice() {
			return _device;
		}

		@Override
		public String getName() {
			return "test";
		}
	}

	/**
	 * A device that only states whether it allows a password change; everything else is beyond what
	 * is asked of it here.
	 */
	private static class Device implements AuthenticationDevice {

		private final boolean _allowPwdChange;

		Device(boolean allowPwdChange) {
			_allowPwdChange = allowPwdChange;
		}

		@Override
		public boolean allowPwdChange() {
			return _allowPwdChange;
		}

		@Override
		public SecurityDevice.Config<?> getConfig() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean authentify(LoginCredentials login) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setPassword(Person account, char[] password) {
			throw new UnsupportedOperationException();
		}

		@Override
		public PasswordValidator getPasswordValidator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void expirePassword(Person account) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isPasswordChangeRequested(Person account, char[] password) {
			throw new UnsupportedOperationException();
		}

		@Override
		public MfaRequirement getMFARequirement() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "device(allowPwdChange: " + _allowPwdChange + ")";
		}
	}

	public static Test suite() {
		return suite(TestAccountFunctions.class);
	}

}
