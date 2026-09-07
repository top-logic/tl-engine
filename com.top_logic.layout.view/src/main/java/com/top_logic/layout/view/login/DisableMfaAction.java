/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.util.error.TopLogicException;

/**
 * Takes multi-factor authentication off the account passed as its input, so its next login needs
 * the password alone.
 *
 * <p>
 * Clears the stored secret and the requirement together: a requirement kept without a secret is the
 * state that sends the next login into enrollment, which is the opposite of switching the second
 * factor off.
 * </p>
 *
 * <p>
 * An account whose second factor is demanded of it - {@link MfaRequirement#REQUIRED} imposed rather
 * than chosen - cannot switch it off. Whether that is the case is a question about the account, so
 * it is answered here rather than left to the view offering the action.
 * </p>
 *
 * @see EnableMfaAction
 */
@InApp
public class DisableMfaAction implements ViewAction {

	/**
	 * Configuration for {@link DisableMfaAction}.
	 */
	@TagName("disable-mfa")
	public interface Config extends PolymorphicConfiguration<DisableMfaAction> {

		@Override
		@ClassDefault(DisableMfaAction.class)
		Class<? extends DisableMfaAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link DisableMfaAction} from configuration.
	 */
	@CalledByReflection
	public DisableMfaAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof Person)) {
			throw new TopLogicException(I18NConstants.MFA_NO_ACCOUNT);
		}
		Person account = (Person) input;

		if (account.getMFARequirement() == MfaRequirement.REQUIRED) {
			throw new TopLogicException(I18NConstants.ERROR_MFA_REQUIRED__USER.fill(account.getName()));
		}

		try (Transaction tx = account.tKnowledgeBase()
			.beginTransaction(I18NConstants.MFA_DISABLED__USER.fill(account.getName()))) {
			account.tUpdateByName(Person.MFA_SECRET_ATTR, null);
			account.setMFARequirement(MfaRequirement.OPTIONAL);
			tx.commit();
		}

		InfoService.showInfo(I18NConstants.MFA_DISABLED__USER.fill(account.getName()));
		return input;
	}

}
