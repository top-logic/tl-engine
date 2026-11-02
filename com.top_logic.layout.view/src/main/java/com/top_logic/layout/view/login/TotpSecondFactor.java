/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;

/**
 * {@link SecondFactor} confirming a login with the time-based one-time code of an authenticator app
 * (TOTP).
 *
 * <p>
 * An account has it set up when it carries a {@link Person#getMFASecret() TOTP secret}, and may set
 * it up unless its {@link MfaRequirement} is {@link MfaRequirement#DISABLED}. The verification is
 * the {@link LoginAction#OTP_VIEW code dialog}, the set-up the {@link LoginAction#MFA_ENROLL_VIEW
 * enrollment dialog} on a freshly generated secret; both confirm the code with
 * {@link VerifyOtpAction}.
 * </p>
 */
public class TotpSecondFactor extends AbstractSecondFactor<TotpSecondFactor.Config<?>> {

	/**
	 * Configuration of a {@link TotpSecondFactor}.
	 */
	public interface Config<I extends TotpSecondFactor> extends SecondFactor.Config<I> {

		@Override
		@ClassDefault(TotpSecondFactor.class)
		Class<? extends I> getImplementationClass();
	}

	/**
	 * Creates a {@link TotpSecondFactor} from configuration.
	 */
	@CalledByReflection
	public TotpSecondFactor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public boolean isEnrolled(Person account) {
		return account.getMFASecret() != null;
	}

	@Override
	public boolean canEnroll(Person account) {
		return account.getMFARequirement() != MfaRequirement.DISABLED;
	}

	/**
	 * Replaces the current dialog with the {@link LoginAction#OTP_VIEW code dialog}, transferring
	 * the account and its TOTP secret on the {@link LoginAction#ACCOUNT_CHANNEL} /
	 * {@link LoginAction#SECRET_CHANNEL} channels.
	 */
	@Override
	public void verify(ReactContext context, Person account) {
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(LoginAction.MODEL_CHANNEL, LoginAction.newOtpEntry());
		channels.put(LoginAction.ACCOUNT_CHANNEL, account);
		channels.put(LoginAction.SECRET_CHANNEL, account.getMFASecret());
		LoginAction.replaceDialog(context, LoginAction.OTP_VIEW, channels);
	}

	/**
	 * Replaces the current dialog with the {@link LoginAction#MFA_ENROLL_VIEW enrollment dialog},
	 * see {@link #enrollmentChannels(Person)}.
	 */
	@Override
	public void enroll(ReactContext context, Person account) {
		LoginAction.replaceDialog(context, LoginAction.MFA_ENROLL_VIEW, enrollmentChannels(account));
	}

	/**
	 * The channel values of the {@link LoginAction#MFA_ENROLL_VIEW enrollment dialog} for the given
	 * account: a fresh secret and its QR code on the {@link LoginAction#SECRET_CHANNEL} /
	 * {@link LoginAction#QR_CHANNEL} channels. The secret is stored only once
	 * {@link VerifyOtpAction} confirms a valid code generated from it.
	 */
	static Map<String, Object> enrollmentChannels(Person account) {
		Password secret = MfaSupport.generateSecret();
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(LoginAction.MODEL_CHANNEL, LoginAction.newOtpEntry());
		channels.put(LoginAction.ACCOUNT_CHANNEL, account);
		channels.put(LoginAction.SECRET_CHANNEL, secret);
		channels.put(LoginAction.QR_CHANNEL, MfaSupport.createQrCode(account, secret));
		return channels;
	}

}
