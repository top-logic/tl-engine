/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.Arrays;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that authenticates the credentials held by its input object and switches the
 * session to the resulting user.
 *
 * <p>
 * Expects its input to be a (transient) object carrying {@code name} and {@code password}
 * attributes, e.g. the model of the login {@code <form>} after {@code <store-form-state/>}. On
 * success the actual session swap is deferred to the reload via {@link PendingSessionAction}; on
 * failure a {@link TopLogicException} is raised so the form shows the error.
 * </p>
 */
public class LoginAction implements ViewAction {

	/**
	 * Configuration for {@link LoginAction}.
	 */
	@TagName("login")
	public interface Config extends PolymorphicConfiguration<LoginAction> {

		@Override
		@ClassDefault(LoginAction.class)
		Class<? extends LoginAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link LoginAction} from configuration.
	 */
	@CalledByReflection
	public LoginAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		TLObject credentials = (TLObject) input;
		String userName = asString(credentials.tValueByName("name"));
		String password = asString(credentials.tValueByName("password"));
		if (isEmpty(userName) || isEmpty(password)) {
			throw new TopLogicException(I18NConstants.LOGIN_MISSING_CREDENTIALS);
		}

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		char[] decryptedPassword = password.toCharArray();
		boolean success;
		try {
			success = Login.getInstance()
				.checkUserPassword(userName, decryptedPassword, displayContext.asRequest(),
					displayContext.asResponse());
		} catch (Exception ex) {
			Logger.info("Login attempt for '" + userName + "' rejected: " + ex.getMessage(), LoginAction.class);
			throw new TopLogicException(I18NConstants.LOGIN_FAILED, ex);
		} finally {
			Arrays.fill(decryptedPassword, (char) 0);
		}

		if (!success) {
			throw new TopLogicException(I18NConstants.LOGIN_FAILED);
		}

		// Defer the session swap to the reload handled by ViewServlet.
		PendingSessionAction.requestLogin(displayContext.asRequest().getSession(), userName);

		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		context.getSSEQueue().enqueue(JSSnipplet.create().setCode("window.location.reload();"));
		return input;
	}

	private static String asString(Object value) {
		return value == null ? null : value.toString();
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
