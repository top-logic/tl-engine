/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

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
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that verifies the one-time code entered in the MFA step of the login flow and,
 * on success, completes the deferred login.
 *
 * <p>
 * Reads the code from its input (the transient {@link LoginAction#OTP_ENTRY_TYPE} form model), the
 * TOTP secret from the dialog's {@link LoginAction#SECRET_CHANNEL} channel and the account from its
 * {@link LoginAction#ACCOUNT_CHANNEL} channel. When {@link Config#getPersist() persist} is set (the
 * enrollment case) the just-confirmed secret is stored on the account before the login proceeds.
 * </p>
 *
 * @implNote Verification is delegated to {@link MfaSupport#isValidCode(Password, String)}; the
 *           secret is persisted via {@link com.top_logic.model.TLObject#tUpdateByName(String, Object)}
 *           on {@link Person#MFA_SECRET_ATTR} (avoiding the broken {@link Person#setMFASecret(Password)}).
 */
public class VerifyOtpAction implements ViewAction {

	/**
	 * Configuration for {@link VerifyOtpAction}.
	 */
	@TagName("verify-otp")
	public interface Config extends PolymorphicConfiguration<VerifyOtpAction> {

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
		if (!MfaSupport.isValidCode(secret, code)) {
			throw new TopLogicException(I18NConstants.MFA_INVALID_CODE);
		}

		if (_persist) {
			try (Transaction tx = account.tKnowledgeBase()
				.beginTransaction(I18NConstants.MFA_ENABLED__USER.fill(account.getName()))) {
				account.tUpdateByName(Person.MFA_SECRET_ATTR, secret);
				tx.commit();
			}
		}

		LoginAction.completeLogin(context, account.getName());
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

}
