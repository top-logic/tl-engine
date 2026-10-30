/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.layout.view.command.WithValueArgument;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.base.security.util.Password;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that verifies the one-time code entered in the one-time-code or enrollment
 * dialog and, on success, completes the step the dialog belongs to.
 *
 * <p>
 * Reads the code from its input (the transient {@link LoginAction#OTP_ENTRY_TYPE} form model), the
 * TOTP secret from the dialog's {@link LoginAction#SECRET_CHANNEL} channel and the account from its
 * {@link LoginAction#ACCOUNT_CHANNEL} channel. When {@link Config#getPersist() persist} is set (the
 * enrollment case) the just-confirmed secret is stored on the account, replacing a secret stored
 * before.
 * </p>
 *
 * <p>
 * In the login flow - the window is not logged in yet - a valid code completes the deferred login.
 * In a window that is already logged in - an enrollment the user started from their settings - the
 * window keeps its session: the dialog closes and the user is told that the second factor is set
 * up.
 * </p>
 *
 * @implNote Verification is delegated to {@link MfaSupport#isValidCode(Password, String)}; the
 *           secret is persisted via {@link com.top_logic.model.TLObject#tUpdateByName(String, Object)}
 *           on {@link Person#MFA_SECRET_ATTR} (avoiding the broken {@link Person#setMFASecret(Password)}).
 *           The codes checked against one secret are counted by the {@link LoginFailuresModule}
 *           (key {@link LoginFailuresModule#secretKey(Password)}): after repeated wrong codes,
 *           further codes are refused for a growing time without being checked. Whether the window
 *           is in the login flow is {@link TLContext#isAnonymous()}, the check that offers the login
 *           command to a window ({@link AnonymousOnly}).
 */
@InApp
public class VerifyOtpAction implements ViewAction {

	/**
	 * Configuration for {@link VerifyOtpAction}.
	 */
	@TagName("verify-otp")
	public interface Config extends PolymorphicConfiguration<VerifyOtpAction>, WithValueArgument {

		/** Configuration name for {@link #getPersist()}. */
		String PERSIST = "persist";

		@Override
		@ClassDefault(VerifyOtpAction.class)
		Class<? extends VerifyOtpAction> getImplementationClass();

		/**
		 * Whether the verified secret should be stored on the account (the MFA-enrollment case).
		 *
		 * <p>
		 * When {@code false} (plain OTP login) the account already carries the secret, so nothing is
		 * persisted.
		 * </p>
		 */
		@Name(PERSIST)
		@BooleanDefault(false)
		boolean getPersist();
	}

	private final boolean _persist;

	/**
	 * Creates a new {@link VerifyOtpAction} from configuration.
	 */
	@CalledByReflection
	public VerifyOtpAction(InstantiationContext context, Config config) {
		_persist = config.getPersist();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		Person account = resolveAccount(context);
		Password secret = resolveSecret(context);
		if (account == null || secret == null) {
			throw new TopLogicException(I18NConstants.LOGIN_FAILED);
		}

		String code = asString(((TLObject) input).tValueByName("code"));
		// Refuses with the too-many-attempts message while the secret is backing off.
		boolean valid = LoginFailuresModule.getInstance().attempt(LoginFailuresModule.secretKey(secret),
			com.top_logic.layout.component.configuration.I18NConstants.CHECK_OTP_TOO_MANY_ATTEMPTS__TIMEOUT,
			() -> MfaSupport.isValidCode(secret, code));
		if (!valid) {
			throw new TopLogicException(I18NConstants.MFA_INVALID_CODE);
		}

		if (_persist) {
			try (Transaction tx = account.tKnowledgeBase()
				.beginTransaction(I18NConstants.MFA_ENABLED__USER.fill(account.getName()))) {
				account.tUpdateByName(Person.MFA_SECRET_ATTR, secret);
				tx.commit();
			}
		}

		if (TLContext.isAnonymous()) {
			LoginAction.completeLogin(context, account.getName());
		} else {
			DialogManager dialogManager = context.getDialogManager();
			if (dialogManager != null) {
				dialogManager.closeTopDialog(DialogResult.ok(null));
			}
			ViewMessages.info(context, I18NConstants.MFA_ENABLED__USER.fill(account.getName()));
		}
		return input;
	}

	private static Person resolveAccount(ReactContext context) {
		Object value = resolveChannel(context, LoginAction.ACCOUNT_CHANNEL);
		return value instanceof Person ? (Person) value : null;
	}

	private static Password resolveSecret(ReactContext context) {
		Object value = resolveChannel(context, LoginAction.SECRET_CHANNEL);
		return value instanceof Password ? (Password) value : null;
	}

	private static Object resolveChannel(ReactContext context, String name) {
		if (!(context instanceof ViewContext)) {
			return null;
		}
		ViewContext viewContext = (ViewContext) context;
		if (!viewContext.hasChannel(name)) {
			return null;
		}
		return viewContext.resolveChannel(new ChannelRef(name)).get();
	}

	private static String asString(Object value) {
		return value == null ? null : value.toString();
	}

	/**
	 * Completes the login, switching the window to the authenticated user, or closes the
	 * enrollment dialog of a logged-in window.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
