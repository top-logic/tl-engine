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
import com.top_logic.basic.config.ConfigurationItem;
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
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.login.ChangePasswordApplyAction;
import com.top_logic.layout.view.login.CheckPasswordChangeAction;
import com.top_logic.layout.view.login.I18NConstants;
import com.top_logic.layout.view.login.LoginAction;
import com.top_logic.model.TLObject;
import com.top_logic.model.TransientObject;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests {@link ChangePasswordApplyAction}, which applies a newly chosen password to the account it
 * belongs to, and {@link CheckPasswordChangeAction}, which checks the password in front of the
 * identity confirmation.
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
	 * Tests that the apply action rejects a password the device's policy rejects before it begins
	 * to store anything.
	 */
	public void testApplyRefusesPolicyViolation() {
		TopLogicException problem = refused(new Device(true, minLength(8)), form("short", "short"));

		assertEquals(PasswordValidator.ValidationResult.TO_SHORT.messageKey(), problem.getErrorKey());
	}

	/**
	 * Tests that the check refuses a confirmation that does not match the password.
	 */
	public void testCheckRefusesMismatch() {
		TopLogicException problem = checkRefused(new Device(true, minLength(4)), form("secret", "typo"));

		assertEquals(I18NConstants.PASSWORD_MISMATCH, problem.getErrorKey());
	}

	/**
	 * Tests that the check refuses an empty password.
	 */
	public void testCheckRefusesEmpty() {
		TopLogicException problem = checkRefused(new Device(true, minLength(0)), form("", ""));

		assertEquals(I18NConstants.PASSWORD_EMPTY, problem.getErrorKey());
	}

	/**
	 * Tests that the check refuses an absent password the same way as an empty one.
	 */
	public void testCheckRefusesMissing() {
		TopLogicException problem = checkRefused(new Device(true, minLength(0)), form(null, null));

		assertEquals(I18NConstants.PASSWORD_EMPTY, problem.getErrorKey());
	}

	/**
	 * Tests that the check refuses a password the device's policy rejects, with the policy's reason.
	 */
	public void testCheckRefusesPolicyViolation() {
		TopLogicException problem = checkRefused(new Device(true, minLength(8)), form("short", "short"));

		assertEquals(PasswordValidator.ValidationResult.TO_SHORT.messageKey(), problem.getErrorKey());
	}

	/**
	 * Tests that the check refuses an account whose device does not allow a password change, without
	 * asking the device for a policy it may not have.
	 */
	public void testCheckRefusesDeviceKeepingThePassword() {
		TopLogicException problem = checkRefused(new Device(false), form("secret", "typo"));

		assertEquals(I18NConstants.ERROR_PASSWORD_CHANGE_NOT_ALLOWED, problem.getErrorKey());
	}

	/**
	 * Tests that the check refuses an account without a device.
	 */
	public void testCheckRefusesAccountWithoutDevice() {
		TopLogicException problem = checkRefused(null, form("secret", "secret"));

		assertEquals(I18NConstants.ERROR_PASSWORD_CHANGE_NOT_ALLOWED, problem.getErrorKey());
	}

	/**
	 * Tests that the check passes an acceptable password on unchanged, without storing anything: the
	 * device's {@link AuthenticationDevice#setPassword(Person, char[])} and the account's knowledge
	 * base both refuse to be used here.
	 */
	public void testCheckPassesValidPassword() {
		TLObject form = form("long enough", "long enough");

		Object result = newCheck().execute(context(new Account(new Device(true, minLength(8)))), form);

		assertSame(form, result);
	}

	/**
	 * Runs the check action for an account authenticated by the given device and returns what it
	 * refused with.
	 */
	private TopLogicException checkRefused(AuthenticationDevice device, TLObject form) {
		return refused(newCheck(), device, form);
	}

	/**
	 * Runs the apply action for an account authenticated by the given device and returns what it
	 * refused with.
	 */
	private TopLogicException refused(AuthenticationDevice device, TLObject form) {
		return refused(newAction(), device, form);
	}

	/**
	 * Runs the given action for an account authenticated by the given device and returns what it
	 * refused with.
	 */
	private TopLogicException refused(ViewAction action, AuthenticationDevice device, TLObject form) {
		try {
			action.execute(context(new Account(device)), form);
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

	/** The production check action, with nothing replaced. */
	private static CheckPasswordChangeAction newCheck() {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestChangePasswordApplyAction.class);
		return new CheckPasswordChangeAction(context,
			TypedConfiguration.newConfigItem(CheckPasswordChangeAction.Config.class));
	}

	/**
	 * The production password policy, enabled and requiring nothing but the given minimum length.
	 */
	private static PasswordValidator minLength(int minLength) {
		PasswordValidator.Config config = TypedConfiguration.newConfigItem(PasswordValidator.Config.class);
		set(config, "enabled", true);
		set(config, "min-password-length", minLength);
		set(config, "criteria-count", 0);
		return new PasswordValidator(new DefaultInstantiationContext(TestChangePasswordApplyAction.class), config);
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
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
		values.put(LoginAction.NEW_PASSWORD_ATTR, newPassword);
		values.put(LoginAction.NEW_PASSWORD_CONFIRM_ATTR, confirmation);
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
	 * A device that states whether it allows a password change and answers with the password policy
	 * it was created with; it is never asked to store anything here.
	 */
	private static class Device implements AuthenticationDevice {

		private final boolean _allowPwdChange;

		private final PasswordValidator _validator;

		Device(boolean allowPwdChange) {
			this(allowPwdChange, null);
		}

		Device(boolean allowPwdChange, PasswordValidator validator) {
			_allowPwdChange = allowPwdChange;
			_validator = validator;
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
			if (_validator == null) {
				// Like a device that keeps the password elsewhere.
				throw new UnsupportedOperationException();
			}
			return _validator;
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
