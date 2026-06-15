/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import java.util.Arrays;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that creates a new persistent {@link Person account} from a login name.
 *
 * <p>
 * Expects its input to be a (transient) object carrying a {@code login} attribute and an optional
 * {@code password} attribute (e.g. the model of the "new account" {@code <form>} after
 * {@code <store-form-state/>}). The account is created with the default {@link AuthenticationDevice};
 * when a password is given (and the device supports password change) it is set, so the account is
 * immediately usable for login. Must run inside a transaction (wrap the action in
 * {@code <with-transaction>}); the created {@link Person} is returned as the action result.
 * </p>
 */
public class CreateAccountAction implements ViewAction {

	/**
	 * Configuration for {@link CreateAccountAction}.
	 */
	@TagName("create-account")
	public interface Config extends PolymorphicConfiguration<CreateAccountAction> {

		@Override
		@ClassDefault(CreateAccountAction.class)
		Class<? extends CreateAccountAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link CreateAccountAction} from configuration.
	 */
	@CalledByReflection
	public CreateAccountAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		TLObject newAccount = (TLObject) input;
		Object loginValue = newAccount.tValueByName("login");
		String login = loginValue == null ? null : loginValue.toString().trim();
		if (login == null || login.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_LOGIN);
		}
		if (Person.byName(login) != null) {
			throw new TopLogicException(I18NConstants.ERROR_ACCOUNT_EXISTS__LOGIN.fill(login));
		}

		AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice();
		Person account = Person.create(PersistencyLayer.getKnowledgeBase(), login, device);

		Object passwordValue = newAccount.tValueByName("password");
		String password = passwordValue == null ? null : passwordValue.toString();
		if (password != null && !password.isEmpty() && device != null && device.allowPwdChange()) {
			char[] passwordChars = password.toCharArray();
			try {
				// Throws a TopLogicException when the password violates the configured policy.
				device.setPassword(account, passwordChars);
			} finally {
				Arrays.fill(passwordChars, (char) 0);
			}
		}
		return account;
	}
}
