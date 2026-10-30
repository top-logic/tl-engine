/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.layout.view.command.WithValueArgument;
import java.util.Arrays;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that applies a newly chosen password to an account.
 *
 * <p>
 * Serves both ways a password is changed. A login with an expired password forces the change before
 * the session exists: {@link LoginAction} opens the dialog with the authenticated account on its
 * {@link LoginAction#ACCOUNT_CHANNEL} channel, and the deferred login continues once the new
 * password is stored. A user changing their password voluntarily has a session already; no such
 * channel is in scope, the current account is the one changed, and nothing continues afterwards.
 * </p>
 *
 * <p>
 * Expects its input to be the transient {@link LoginAction#PASSWORD_CHANGE_TYPE} model (carrying
 * {@link LoginAction#NEW_PASSWORD_ATTR} and {@link LoginAction#NEW_PASSWORD_CONFIRM_ATTR}), e.g. as the result of an enclosing
 * {@code <store-form-state>}.
 * Password policy validation and the actual change are delegated to the
 * {@link AuthenticationDevice}, which also clears the expiry flag and maintains the password
 * history. On a policy violation or mismatch a {@link TopLogicException} is raised so the form shows
 * the error. The checks that say nothing about the account are the ones of
 * {@link CheckPasswordChangeAction}, which a view runs in front of an identity confirmation; this
 * action runs them again itself, since the forced change of a login has no such step in front of it.
 * </p>
 *
 * <p>
 * An account whose device keeps the password elsewhere - a directory service, an identity provider -
 * is refused before the device is asked to store anything, whichever way the dialog was reached. A
 * view decides by the same condition whether to offer the change at all, with the TL-Script function
 * <code>accountPasswordChangeAllowed()</code>.
 * </p>
 *
 * @implNote The change is performed via {@link AuthenticationDevice#setPassword(Person, char[])}.
 *           Only a pending login continues through {@link LoginAction#proceedAfterPassword} (which
 *           runs any required MFA step before completing the login); that is decided by where the
 *           account came from, see
 *           {@link CheckPasswordChangeAction#pendingLogin(ReactContext)}.
 */
@InApp
public class ChangePasswordApplyAction implements ViewAction {

	/**
	 * Configuration for {@link ChangePasswordApplyAction}.
	 */
	@TagName("change-password")
	public interface Config extends PolymorphicConfiguration<ChangePasswordApplyAction>, WithValueArgument {

		@Override
		@ClassDefault(ChangePasswordApplyAction.class)
		Class<? extends ChangePasswordApplyAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link ChangePasswordApplyAction} from configuration.
	 */
	@CalledByReflection
	public ChangePasswordApplyAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		// A pending login names the account it authenticated; otherwise the session's own account is
		// the one whose password is changed.
		Person pending = CheckPasswordChangeAction.pendingLogin(context);
		Person account = CheckPasswordChangeAction.account(pending);
		AuthenticationDevice device = CheckPasswordChangeAction.checkedDevice(account);
		String newPassword = CheckPasswordChangeAction.checkedPassword((TLObject) input, account, device);

		char[] password = newPassword.toCharArray();
		try {
			try (Transaction tx = account.tKnowledgeBase()
				.beginTransaction(I18NConstants.CHANGED_PASSWORD__USER.fill(account.getName()))) {
				// Validates against the configured password policy (throws on violation), persists the
				// new password, clears the expiry flag and updates the password history.
				device.setPassword(account, password);
				tx.commit();
			}
		} finally {
			Arrays.fill(password, (char) 0);
		}

		if (pending != null) {
			LoginAction.proceedAfterPassword(context, pending);
		}
		return input;
	}

}
