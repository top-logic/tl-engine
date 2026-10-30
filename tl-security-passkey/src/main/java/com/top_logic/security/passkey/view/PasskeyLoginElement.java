/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.login.LoginAction;

/**
 * {@link UIElement} showing a button that logs in with a passkey.
 *
 * <p>
 * Without an {@link Config#getAccount() account channel}, the user picks a passkey of any account,
 * and the passkey alone logs its account in (the authenticator always verifies its user). With an
 * account channel, only the passkeys of the account on the channel are accepted: the passkey as
 * second factor, e.g. in the login dialog that the login opens with the account whose password it
 * checked.
 * </p>
 *
 * <p>
 * The login is completed for the account the passkey belongs to, as the check of the passkey
 * established it.
 * </p>
 *
 * @see LoginAction#completeLogin(ReactContext, String)
 */
public class PasskeyLoginElement extends PasskeyCeremonyElement<PasskeyLoginElement.Config, PasskeyAuthentication> {

	/**
	 * Configuration of a {@link PasskeyLoginElement}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PasskeyCeremonyElement.Config {

		/** The tag of the element in a view. */
		String TAG_NAME = "passkey-login";

		@Override
		@ClassDefault(PasskeyLoginElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a {@link PasskeyLoginElement} from configuration.
	 */
	@CalledByReflection
	public PasskeyLoginElement(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected PasskeyAuthentication createCeremony(ViewContext context) {
		return new PasskeyAuthentication((ctx, account) -> LoginAction.completeLogin(ctx, account.getName()));
	}

	@Override
	protected void offer(PasskeyAuthentication ceremony, Object accountValue) {
		if (getConfig().getAccount() == null) {
			ceremony.offer(null);
		} else if (accountValue instanceof Person account) {
			ceremony.offer(account);
		} else {
			ceremony.withdraw(null);
		}
	}

}
