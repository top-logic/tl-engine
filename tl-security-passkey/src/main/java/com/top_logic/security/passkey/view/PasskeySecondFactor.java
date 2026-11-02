/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.login.AbstractSecondFactor;
import com.top_logic.layout.view.login.LoginAction;
import com.top_logic.layout.view.login.SecondFactor;
import com.top_logic.security.passkey.PasskeyService;

/**
 * {@link SecondFactor} confirming a login with a passkey of the account.
 *
 * <p>
 * An account has it set up when it has a passkey, and may set it up when it
 * {@link PasskeyService#mayUsePasskeys(Person) may use passkeys}. The verification is the
 * {@link #VERIFY_VIEW passkey dialog} with a {@link PasskeyLoginElement} for the account, the set-up
 * the {@link #ENROLL_VIEW set-up dialog} with a {@link PasskeyRegistrationElement} completing the
 * login; both receive the account on the dialog channel {@link LoginAction#ACCOUNT_CHANNEL}.
 * </p>
 */
public class PasskeySecondFactor extends AbstractSecondFactor<PasskeySecondFactor.Config<?>> {

	/**
	 * Dialog view (relative to {@link com.top_logic.layout.view.ViewLoader#VIEW_BASE_PATH}) in which
	 * an account confirms its login with a passkey.
	 */
	public static final String VERIFY_VIEW = "passkey-verify.view.xml";

	/**
	 * Dialog view (relative to {@link com.top_logic.layout.view.ViewLoader#VIEW_BASE_PATH}) in which
	 * an account creates a passkey during its login.
	 */
	public static final String ENROLL_VIEW = "passkey-enroll.view.xml";

	/**
	 * Configuration of a {@link PasskeySecondFactor}.
	 */
	public interface Config<I extends PasskeySecondFactor> extends SecondFactor.Config<I> {

		@Override
		@ClassDefault(PasskeySecondFactor.class)
		Class<? extends I> getImplementationClass();
	}

	/**
	 * Creates a {@link PasskeySecondFactor} from configuration.
	 */
	@CalledByReflection
	public PasskeySecondFactor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public boolean isEnrolled(Person account) {
		return PasskeyService.getInstance().isEnrolled(account);
	}

	@Override
	public boolean canEnroll(Person account) {
		return PasskeyService.getInstance().mayUsePasskeys(account);
	}

	@Override
	public void verify(ReactContext context, Person account) {
		LoginAction.replaceDialog(context, VERIFY_VIEW, Map.of(LoginAction.ACCOUNT_CHANNEL, account));
	}

	@Override
	public void enroll(ReactContext context, Person account) {
		LoginAction.replaceDialog(context, ENROLL_VIEW, Map.of(LoginAction.ACCOUNT_CHANNEL, account));
	}

}
