/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Sets a second factor up for the current user there and then, by opening the enrollment dialog on
 * a freshly generated secret.
 *
 * <p>
 * This is how a user takes a second factor into use themselves. The secret is stored only once a
 * code generated from it has been verified, which the dialog does, and the account's
 * {@link MfaRequirement} is left as it is: a second factor the user chose is one they may also
 * drop again, which is what {@link MfaRequirement#OPTIONAL} means. Only an administrator imposes
 * {@link MfaRequirement#REQUIRED}, and an account carrying that keeps it.
 * </p>
 *
 * <p>
 * The counterpart in the login flow is {@link LoginAction}, which opens the same dialog for an
 * account that is required to use a second factor and has none yet. The difference is only who
 * asked: there the application insists before letting the user in, here the user asked for it while
 * already logged in.
 * </p>
 *
 * @see DisableMfaAction
 */
@InApp
public class EnrollMfaAction implements ViewAction {

	/**
	 * Configuration for {@link EnrollMfaAction}.
	 */
	@TagName("enroll-mfa")
	public interface Config extends PolymorphicConfiguration<EnrollMfaAction> {

		@Override
		@ClassDefault(EnrollMfaAction.class)
		Class<? extends EnrollMfaAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link EnrollMfaAction} from configuration.
	 */
	@CalledByReflection
	public EnrollMfaAction(InstantiationContext context, Config config) {
		// No configuration needed: the session's own account is the one being set up.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		Person account = TLContext.currentUser();
		if (account == null) {
			throw new TopLogicException(I18NConstants.MFA_NO_ACCOUNT);
		}
		if (account.getMFARequirement() == MfaRequirement.DISABLED) {
			throw new TopLogicException(I18NConstants.ERROR_MFA_NOT_ALLOWED__USER.fill(account.getName()));
		}

		Password secret = MfaSupport.generateSecret();
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(LoginAction.MODEL_CHANNEL, LoginAction.newOtpEntry());
		channels.put(LoginAction.ACCOUNT_CHANNEL, account);
		channels.put(LoginAction.SECRET_CHANNEL, secret);
		channels.put(LoginAction.QR_CHANNEL, MfaSupport.createQrCode(account, secret));

		// Not closing on a backdrop click: an accidental click outside must not abort a setup
		// halfway, where a secret is on screen but not yet stored.
		OpenDialogAction.openDialog(context, ViewLoader.VIEW_BASE_PATH + LoginAction.MFA_ENROLL_VIEW,
			false, channels, Collections.emptyList());
		return input;
	}

}
