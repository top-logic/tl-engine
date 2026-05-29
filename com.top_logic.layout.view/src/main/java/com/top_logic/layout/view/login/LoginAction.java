/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
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
 *
 * <p>
 * If the authenticated account's password has expired, the session swap is held back: a forced
 * {@link #CHANGE_PASSWORD_VIEW change-password dialog} is opened instead (the account is transferred
 * to it on its {@code "account"} channel), and the login completes only after
 * {@link ChangePasswordApplyAction} has applied a new password.
 * </p>
 */
public class LoginAction implements ViewAction {

	/** Name of the transient model type holding the change-password form input. */
	public static final String PASSWORD_CHANGE_TYPE = "tl.login:PasswordChange";

	/** Dialog view (relative to {@link ViewLoader#VIEW_BASE_PATH}) for the forced password change. */
	public static final String CHANGE_PASSWORD_VIEW = "change-password.view.xml";

	/** Dialog channel carrying the account whose (expired) password is being changed. */
	public static final String ACCOUNT_CHANNEL = "account";

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
		Person account;
		boolean passwordChangeRequired;
		try {
			boolean success;
			try {
				success = Login.getInstance()
					.checkUserPassword(userName, decryptedPassword, displayContext.asRequest(),
						displayContext.asResponse());
			} catch (Exception ex) {
				Logger.info("Login attempt for '" + userName + "' rejected: " + ex.getMessage(), LoginAction.class);
				throw new TopLogicException(I18NConstants.LOGIN_FAILED, ex);
			}

			if (!success) {
				throw new TopLogicException(I18NConstants.LOGIN_FAILED);
			}

			account = Person.byName(userName);
			passwordChangeRequired = !Login.isPasswordValidAndNotExpired(decryptedPassword, account);
		} finally {
			Arrays.fill(decryptedPassword, (char) 0);
		}

		if (passwordChangeRequired) {
			// Hold back the session swap and force a password change first.
			openChangePasswordDialog(context, account);
			return input;
		}

		completeLogin(context, userName);
		return input;
	}

	/**
	 * Replaces the current (login) dialog with the forced change-password dialog, transferring the
	 * authenticated account on its {@link #ACCOUNT_CHANNEL} channel and a fresh transient
	 * {@link #PASSWORD_CHANGE_TYPE} model on its {@code "model"} channel.
	 */
	private static void openChangePasswordDialog(ReactContext context, Person account) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}

		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put("model", newPasswordChange());
		channels.put(ACCOUNT_CHANNEL, account);
		OpenDialogAction.openDialog(context, ViewLoader.VIEW_BASE_PATH + CHANGE_PASSWORD_VIEW, true, channels,
			Collections.emptyList());
	}

	private static TLObject newPasswordChange() {
		TLClass type = (TLClass) TLModelUtil.findType(PASSWORD_CHANGE_TYPE);
		return TransientObjectFactory.INSTANCE.createObject(type);
	}

	/**
	 * Completes a successful authentication: records the deferred login on the HTTP session, closes
	 * the top dialog, and triggers the client reload that lets {@link PendingSessionAction} perform
	 * the actual session swap.
	 */
	static void completeLogin(ReactContext context, String userName) {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		PendingSessionAction.requestLogin(displayContext.asRequest().getSession(), userName);

		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		context.getSSEQueue().enqueue(JSSnipplet.create().setCode("window.location.reload();"));
	}

	private static String asString(Object value) {
		return value == null ? null : value.toString();
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
