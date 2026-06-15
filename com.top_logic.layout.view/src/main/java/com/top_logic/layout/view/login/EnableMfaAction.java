/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.util.error.TopLogicException;

/**
 * Administrative / demo {@link ViewAction} that configures multi-factor authentication on the
 * account passed as its input, to exercise the OTP / enrollment login steps.
 *
 * <p>
 * With a configured {@link Config#getSecret() secret} it stores that TOTP secret on the account, so
 * the next login goes through OTP verification against a known secret. Without one it clears any
 * stored secret and marks MFA {@link MfaRequirement#REQUIRED required}, so the next login goes
 * through enrollment.
 * </p>
 *
 * @implNote The secret is persisted via
 *           {@link com.top_logic.model.TLObject#tUpdateByName(String, Object)} on
 *           {@link Person#MFA_SECRET_ATTR} (avoiding the broken
 *           {@link Person#setMFASecret(Password)}).
 */
public class EnableMfaAction implements ViewAction {

	/**
	 * Configuration for {@link EnableMfaAction}.
	 */
	@TagName("enable-mfa")
	public interface Config extends PolymorphicConfiguration<EnableMfaAction> {

		/** Configuration name for {@link #getSecret()}. */
		String SECRET = "secret";

		@Override
		@ClassDefault(EnableMfaAction.class)
		Class<? extends EnableMfaAction> getImplementationClass();

		/**
		 * A fixed Base32 TOTP secret to store on the account (for testing the OTP-login path).
		 *
		 * <p>
		 * When empty, the account is instead marked as MFA-required with no secret, so the next
		 * login triggers enrollment.
		 * </p>
		 */
		@Name(SECRET)
		@Nullable
		String getSecret();
	}

	private final String _secret;

	/**
	 * Creates a new {@link EnableMfaAction} from configuration.
	 */
	@CalledByReflection
	public EnableMfaAction(InstantiationContext context, Config config) {
		_secret = config.getSecret();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof Person)) {
			throw new TopLogicException(I18NConstants.MFA_NO_ACCOUNT);
		}
		Person account = (Person) input;

		try (Transaction tx = account.tKnowledgeBase()
			.beginTransaction(I18NConstants.MFA_REQUIRED_SET__USER.fill(account.getName()))) {
			if (StringServices.isEmpty(_secret)) {
				account.tUpdateByName(Person.MFA_SECRET_ATTR, null);
				account.setMFARequirement(MfaRequirement.REQUIRED);
			} else {
				account.tUpdateByName(Person.MFA_SECRET_ATTR, Password.fromPlainText(_secret));
			}
			tx.commit();
		}

		InfoService.showInfo(I18NConstants.MFA_REQUIRED_SET__USER.fill(account.getName()));
		return input;
	}

}
