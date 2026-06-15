/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.Arrays;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that applies the new password chosen in the forced change-password step that
 * follows a login with an expired password, then completes the deferred login.
 *
 * <p>
 * Expects its input to be the transient {@link LoginAction#PASSWORD_CHANGE_TYPE} model (carrying
 * {@code newPassword} and {@code newPasswordConfirm}), e.g. after {@code <store-form-state/>}. The
 * account to change is read from the dialog's {@link LoginAction#ACCOUNT_CHANNEL} channel (populated
 * by {@link LoginAction} when it opened the dialog).
 * </p>
 *
 * <p>
 * Password policy validation and the actual change are delegated to the
 * {@link AuthenticationDevice} (which also clears the expiry flag and maintains the password
 * history), reusing the same headless logic as the legacy change-password component. On success the
 * deferred session swap is triggered; on a policy violation or mismatch a {@link TopLogicException}
 * is raised so the form shows the error.
 * </p>
 *
 * @implNote The change is performed via {@link AuthenticationDevice#setPassword(Person, char[])} and,
 *           on success, the login is finished through {@link LoginAction#completeLogin}.
 */
public class ChangePasswordApplyAction implements ViewAction {

	/**
	 * Configuration for {@link ChangePasswordApplyAction}.
	 */
	@TagName("change-password")
	public interface Config extends PolymorphicConfiguration<ChangePasswordApplyAction> {

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
		Person account = resolveAccount(context);
		if (account == null) {
			throw new TopLogicException(I18NConstants.LOGIN_FAILED);
		}

		TLObject form = (TLObject) input;
		String newPassword = asString(form.tValueByName("newPassword"));
		String newPasswordConfirm = asString(form.tValueByName("newPasswordConfirm"));

		if (!equalPasswords(newPassword, newPasswordConfirm)) {
			throw new TopLogicException(I18NConstants.PASSWORD_MISMATCH);
		}
		if (isEmpty(newPassword)) {
			throw new TopLogicException(I18NConstants.PASSWORD_EMPTY);
		}

		char[] password = newPassword.toCharArray();
		try {
			AuthenticationDevice device = account.getAuthenticationDevice();
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

		LoginAction.completeLogin(context, account.getName());
		return input;
	}

	private static Person resolveAccount(ReactContext context) {
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
