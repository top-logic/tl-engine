/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.WithValueArgument;
import com.top_logic.util.error.TopLogicException;

/**
 * Administrative {@link ViewAction} that removes the stored second-factor secret of the account
 * passed as its input, for a user who has lost the device their authenticator app ran on.
 *
 * <p>
 * The account's {@link MfaRequirement} is kept. An account that is
 * {@link MfaRequirement#REQUIRED required} to use a second factor is therefore sent into setting up
 * a new one at its next login, before it is let in; an account that had chosen its second factor
 * itself logs in with the password alone and may set up a new one from its settings.
 * </p>
 *
 * @implNote The secret is cleared via
 *           {@link com.top_logic.model.TLObject#tUpdateByName(String, Object)} on
 *           {@link Person#MFA_SECRET_ATTR} (avoiding the broken
 *           {@link Person#setMFASecret(Password)}).
 *
 * @see EnableMfaAction
 * @see DisableMfaAction
 */
@InApp
public class ResetMfaAction implements ViewAction {

	/**
	 * Configuration for {@link ResetMfaAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ResetMfaAction>, WithValueArgument {

		/** Configuration tag of a {@link ResetMfaAction}. */
		String TAG_NAME = "reset-mfa";

		@Override
		@ClassDefault(ResetMfaAction.class)
		Class<? extends ResetMfaAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link ResetMfaAction} from configuration.
	 */
	@CalledByReflection
	public ResetMfaAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof Person)) {
			throw new TopLogicException(I18NConstants.MFA_NO_ACCOUNT);
		}
		Person account = (Person) input;

		try (Transaction tx = account.tKnowledgeBase()
			.beginTransaction(I18NConstants.MFA_RESET__USER.fill(account.getName()))) {
			account.tUpdateByName(Person.MFA_SECRET_ATTR, null);
			tx.commit();
		}

		ViewMessages.info(context, I18NConstants.MFA_RESET__USER.fill(account.getName()));
		return input;
	}

	/**
	 * Tells the user that the second factor was removed.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
