/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.util.Arrays;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that sets (resets) the password of an existing {@link Person account}.
 *
 * <p>
 * Expects its input to be a (transient) object carrying an {@code account} reference (the
 * {@link Person} to update) and a {@code password} attribute (e.g. the model of the "reset password"
 * {@code <form>} after {@code <store-form-state/>}). The password is set through the default
 * {@link AuthenticationDevice}. Must run inside a transaction (wrap the action in
 * {@code <with-transaction>}); the updated {@link Person} is returned as the action result.
 * </p>
 */
public class SetPasswordAction implements ViewAction {

	/**
	 * Configuration for {@link SetPasswordAction}.
	 */
	@TagName("set-password")
	public interface Config extends PolymorphicConfiguration<SetPasswordAction> {

		@Override
		@ClassDefault(SetPasswordAction.class)
		Class<? extends SetPasswordAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link SetPasswordAction} from configuration.
	 */
	@CalledByReflection
	public SetPasswordAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		TLObject reset = (TLObject) input;

		Object accountValue = reset.tValueByName("account");
		if (!(accountValue instanceof Person)) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_ACCOUNT);
		}
		Person account = (Person) accountValue;

		Object passwordValue = reset.tValueByName("password");
		String password = passwordValue == null ? null : passwordValue.toString();
		if (password == null || password.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_PASSWORD);
		}

		AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice();
		if (device == null || !device.allowPwdChange()) {
			throw new TopLogicException(I18NConstants.ERROR_PASSWORD_CHANGE_UNSUPPORTED);
		}

		char[] passwordChars = password.toCharArray();
		try {
			// Throws a TopLogicException when the password violates the configured policy.
			device.setPassword(account, passwordChars);
		} finally {
			Arrays.fill(passwordChars, (char) 0);
		}
		return account;
	}
}
