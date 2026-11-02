/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.Arrays;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.security.password.PasswordValidator;
import com.top_logic.base.security.password.PasswordValidator.ValidationResult;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that checks a newly chosen password before anything else is asked of the user.
 *
 * <p>
 * Placed in the change-password chain in front of {@code <verify-identity/>}, so that a mistyped
 * confirmation or a password the policy rejects is reported while the user is still looking at the
 * form, not after they have typed their current password into the identity prompt. Expects the same
 * input as {@link ChangePasswordApplyAction} - the transient {@link LoginAction#PASSWORD_CHANGE_TYPE}
 * model - and passes it on unchanged when the password is acceptable. A rejected password raises the
 * same {@link TopLogicException} the apply action would raise for it.
 * </p>
 *
 * <p>
 * The checks are the ones that say nothing about the account: whether its
 * {@link AuthenticationDevice} allows a password change at all, whether the confirmation matches,
 * whether a password was entered, and whether it satisfies the device's {@link PasswordValidator
 * password policy}.
 * </p>
 *
 * <p>
 * Whether the account used the password before is deliberately not checked here. That answer
 * describes the account's past passwords, and a check in front of the identity confirmation would
 * let anyone at an unattended session probe them. It is left to the device storing the password,
 * which {@link ChangePasswordApplyAction} asks only after the identity has been confirmed.
 * </p>
 *
 * @implNote The reuse check is part of {@link AuthenticationDevice#setPassword(Person, char[])}.
 *           {@link ChangePasswordApplyAction} runs the same checks through
 *           {@link #checkedDevice(Person)} and {@link #checkedPassword(TLObject, Person, AuthenticationDevice)},
 *           since it also serves the forced change of a login with an expired password, where no
 *           check runs in front of it.
 */
@InApp
public class CheckPasswordChangeAction implements ViewAction {

	/**
	 * Configuration for {@link CheckPasswordChangeAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<CheckPasswordChangeAction> {

		/** Tag name of the action in a view's command chain. */
		String TAG_NAME = "check-password-change";

		@Override
		@ClassDefault(CheckPasswordChangeAction.class)
		Class<? extends CheckPasswordChangeAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link CheckPasswordChangeAction} from configuration.
	 */
	@CalledByReflection
	public CheckPasswordChangeAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		Person account = account(pendingLogin(context));
		AuthenticationDevice device = checkedDevice(account);
		checkedPassword((TLObject) input, account, device);
		return input;
	}

	/**
	 * The account whose password is changed.
	 *
	 * @param pending
	 *        The account of a login waiting for the change, see {@link #pendingLogin(ReactContext)}.
	 * @return The pending account, or the session's own account when no login is pending.
	 * @throws TopLogicException
	 *         When there is neither.
	 */
	static Person account(Person pending) {
		Person account = pending != null ? pending : TLContext.currentUser();
		if (account == null) {
			throw new TopLogicException(I18NConstants.LOGIN_FAILED);
		}
		return account;
	}

	/**
	 * The account of a login waiting for this password change, or {@code null} when the password is
	 * being changed from an established session.
	 *
	 * <p>
	 * The distinguishing mark is {@link LoginAction#ACCOUNT_CHANNEL}, which only
	 * {@link LoginAction} puts in scope.
	 * </p>
	 */
	static Person pendingLogin(ReactContext context) {
		if (!(context instanceof ViewContext)) {
			return null;
		}
		ViewContext viewContext = (ViewContext) context;
		if (!viewContext.hasChannel(LoginAction.ACCOUNT_CHANNEL)) {
			return null;
		}
		Object value = viewContext.resolveChannel(new ChannelRef(LoginAction.ACCOUNT_CHANNEL)).get();
		return value instanceof Person ? (Person) value : null;
	}

	/**
	 * The device that stores the password of the given account.
	 *
	 * <p>
	 * The device owns the password, so an account whose device keeps it elsewhere - a directory
	 * service, an identity provider - is refused before anything else is asked of the device.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         When the account has no device or its device does not allow a password change.
	 */
	static AuthenticationDevice checkedDevice(Person account) {
		AuthenticationDevice device = account.getAuthenticationDevice();
		if (device == null || !device.allowPwdChange()) {
			throw new TopLogicException(I18NConstants.ERROR_PASSWORD_CHANGE_NOT_ALLOWED);
		}
		return device;
	}

	/**
	 * The new password entered in the given form, after the checks that say nothing about the
	 * account's password history.
	 *
	 * @param form
	 *        The transient {@link LoginAction#PASSWORD_CHANGE_TYPE} model.
	 * @param account
	 *        The account whose password is changed.
	 * @param device
	 *        The device of the account, as returned by {@link #checkedDevice(Person)}.
	 * @return The new password, never empty.
	 * @throws TopLogicException
	 *         When the confirmation does not match, no password was entered, or the device's
	 *         password policy rejects the password.
	 */
	static String checkedPassword(TLObject form, Person account, AuthenticationDevice device) {
		String newPassword = asString(form.tValueByName(LoginAction.NEW_PASSWORD_ATTR));
		String newPasswordConfirm = asString(form.tValueByName(LoginAction.NEW_PASSWORD_CONFIRM_ATTR));

		if (!equalPasswords(newPassword, newPasswordConfirm)) {
			throw new TopLogicException(I18NConstants.PASSWORD_MISMATCH);
		}
		if (isEmpty(newPassword)) {
			throw new TopLogicException(I18NConstants.PASSWORD_EMPTY);
		}

		PasswordValidator validator = device.getPasswordValidator();
		if (validator != null) {
			char[] password = newPassword.toCharArray();
			try {
				ValidationResult result = validator.validatePassword(account, password);
				if (result != ValidationResult.OK) {
					throw new TopLogicException(result.messageKey());
				}
			} finally {
				Arrays.fill(password, (char) 0);
			}
		}
		return newPassword;
	}

	private static boolean equalPasswords(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}

	private static String asString(Object value) {
		return value == null ? null : value.toString();
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
