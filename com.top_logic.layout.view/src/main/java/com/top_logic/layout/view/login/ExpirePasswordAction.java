/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that expires the password of the account passed as its input, forcing a
 * password change on the account's next login.
 *
 * <p>
 * Expiry lives in the authentication device's internal credential store (not as a model attribute),
 * so it cannot be set from TL-Script; this action delegates to the {@link AuthenticationDevice}. It
 * is primarily an administrative / demo affordance to exercise the forced change-password login
 * flow.
 * </p>
 *
 * @implNote Delegates to {@link AuthenticationDevice#expirePassword(Person)}.
 */
public class ExpirePasswordAction implements ViewAction {

	/**
	 * Configuration for {@link ExpirePasswordAction}.
	 */
	@TagName("expire-password")
	public interface Config extends PolymorphicConfiguration<ExpirePasswordAction> {

		@Override
		@ClassDefault(ExpirePasswordAction.class)
		Class<? extends ExpirePasswordAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link ExpirePasswordAction} from configuration.
	 */
	@CalledByReflection
	public ExpirePasswordAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof Person)) {
			throw new TopLogicException(I18NConstants.EXPIRE_PASSWORD_NO_ACCOUNT);
		}
		Person account = (Person) input;

		AuthenticationDevice device = account.getAuthenticationDevice();
		if (device == null || !device.allowPwdChange()) {
			throw new TopLogicException(I18NConstants.EXPIRE_PASSWORD_NOT_SUPPORTED);
		}

		try (Transaction tx =
			account.tKnowledgeBase().beginTransaction(I18NConstants.EXPIRE_PASSWORD_DONE__USER.fill(account.getName()))) {
			device.expirePassword(account);
			tx.commit();
		}

		InfoService.showInfo(I18NConstants.EXPIRE_PASSWORD_DONE__USER.fill(account.getName()));
		return input;
	}

}
