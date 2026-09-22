/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.login;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.device.interfaces.SecurityDevice;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.login.ChangePasswordApplyAction;
import com.top_logic.layout.view.login.I18NConstants;
import com.top_logic.layout.view.login.LoginAction;
import com.top_logic.model.TLObject;
import com.top_logic.model.TransientObject;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests {@link ChangePasswordApplyAction}, which applies a newly chosen password to the account it
 * belongs to.
 *
 * <p>
 * What is exercised here is the refusal: the device owns the password, and an account whose device
 * keeps it elsewhere is told so instead of running into the device's own failure. The successful
 * change belongs to a knowledge base and is left to the application, so what stands for "not
 * refused" here is the action reading the form it was refusing to look at.
 * </p>
 */
public class TestChangePasswordApplyAction extends TestCase {

	/**
	 * Tests that an account whose device does not allow a password change is refused with the
	 * reason, whatever the form holds.
	 */
	public void testRefusesDeviceKeepingThePassword() {
		TopLogicException problem = refused(new Device(false), form("secret", "secret"));

		assertEquals(I18NConstants.ERROR_PASSWORD_CHANGE_NOT_ALLOWED, problem.getErrorKey());
	}

	/**
	 * Tests that an account with no device at all - nothing that could store a password - is refused
	 * the same way.
	 */
	public void testRefusesAccountWithoutDevice() {
		TopLogicException problem = refused(null, form("secret", "secret"));

		assertEquals(I18NConstants.ERROR_PASSWORD_CHANGE_NOT_ALLOWED, problem.getErrorKey());
	}

	/**
	 * Tests that the refusal is what the user reads even when the form is faulty as well: what the
	 * entered passwords are does not matter where none of them can be applied.
	 */
	public void testRefusalPrecedesTheForm() {
		TopLogicException problem = refused(new Device(false), form("secret", "typo"));

		assertEquals("A refused account is told the reason, not to retype its password.",
			I18NConstants.ERROR_PASSWORD_CHANGE_NOT_ALLOWED, problem.getErrorKey());
	}

	/**
	 * Tests that an account whose device allows the change is not refused: the action reads the form
	 * and judges what was entered.
	 */
	public void testAllowedDeviceReachesTheForm() {
		TopLogicException problem = refused(new Device(true), form("secret", "typo"));

		assertEquals(I18NConstants.PASSWORD_MISMATCH, problem.getErrorKey());
	}

	/**
	 * Runs the action for an account authenticated by the given device and returns what it refused
	 * with.
	 */
	private TopLogicException refused(AuthenticationDevice device, TLObject form) {
		try {
			newAction().execute(context(new Account(device)), form);
		} catch (TopLogicException ex) {
			return ex;
		}
		throw new AssertionError("The action did not refuse.");
	}

	/** The production action, with nothing replaced. */
	private static ChangePasswordApplyAction newAction() {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestChangePasswordApplyAction.class);
		return new ChangePasswordApplyAction(context,
			TypedConfiguration.newConfigItem(ChangePasswordApplyAction.Config.class));
	}

	/**
	 * A context naming the account the way a login waiting for the password change does, so that the
	 * action has an account without a session to take one from.
	 */
	private static ViewContext context(Person account) {
		ViewContext result =
			new DefaultViewContext(new DefaultReactContext("", "test", null, new ReactWindowRegistry("test")));
		ViewChannel channel = new DefaultViewChannel(LoginAction.ACCOUNT_CHANNEL);
		channel.set(account);
		result.registerChannel(LoginAction.ACCOUNT_CHANNEL, channel);
		return result;
	}

	/** The model the dialog's form stores what was entered in. */
	private static TLObject form(String newPassword, String confirmation) {
		Map<String, Object> values = new HashMap<>();
		values.put("newPassword", newPassword);
		values.put("newPasswordConfirm", confirmation);
		return new Form(values);
	}

	/**
	 * An account that answers with the device it was created with and refuses access to a knowledge
	 * base: a change that gets as far as storing something is beyond what a test without an
	 * application can follow.
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
		public KnowledgeBase tKnowledgeBase() {
			throw new UnsupportedOperationException("The change is not carried through in this test.");
		}

		@Override
		public String getName() {
			return "test";
		}
	}

	/** A transient model answering with the values it was created with. */
	private static class Form extends TransientObject {

		private final Map<String, Object> _values;

		Form(Map<String, Object> values) {
			_values = values;
		}

		@Override
		public Object tGetData(String property) {
			return _values.get(property);
		}
	}

	/**
	 * A device that only states whether it allows a password change; a device that allows one is
	 * never asked to store anything here.
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
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module and the resources the reported messages are
	 * resolved from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestChangePasswordApplyAction.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
