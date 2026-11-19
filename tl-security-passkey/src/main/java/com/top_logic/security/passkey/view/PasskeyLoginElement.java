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
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.login.LoginFlow;
import com.top_logic.layout.view.login.SecondFactorLoginStep;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PasskeyUsage;

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
 * The {@link PasskeyService#getUsage() usage} of the passkeys decides which of both is offered:
 * the element without account channel only for {@link PasskeyUsage#LOGIN}, the element with
 * account channel only for {@link PasskeyUsage#SECOND_FACTOR}. An element whose kind of login the
 * usage does not allow offers no ceremony and is hidden.
 * </p>
 *
 * <p>
 * The login continues for the account the passkey belongs to, as the check of the passkey
 * established it: the passwordless login begins the {@link LoginFlow} of the account, with the
 * {@link SecondFactorLoginStep} marked as {@link SecondFactorLoginStep#PROVEN proven}; the passkey
 * as second factor finishes the {@link SecondFactorLoginStep} of the login in progress. Either way
 * the further login steps run before the session starts.
 * </p>
 *
 * @see LoginFlow
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
		boolean passwordless = getConfig().getAccount() == null;
		return new PasskeyAuthentication(passwordless ? PasskeyLoginElement::logIn : PasskeyLoginElement::confirm);
	}

	/**
	 * Begins the login of the given account, which a passkey has authenticated without a password.
	 *
	 * <p>
	 * The authenticator has verified its user, so the passkey proves more than a password and a
	 * second factor together: the {@link SecondFactorLoginStep} does not apply. The password is not
	 * involved, so neither does the change of an expired one.
	 * </p>
	 *
	 * @param context
	 *        The context of the display the login is in.
	 * @param account
	 *        The account the passkey belongs to.
	 * @throws com.top_logic.util.error.TopLogicException
	 *         If the account {@link LoginFlow#checkMayLogIn(Person) may not log in}, e.g. because
	 *         it is locked.
	 */
	public static void logIn(ReactContext context, Person account) {
		LoginFlow.checkMayLogIn(account);
		LoginFlow flow = new LoginFlow(account);
		flow.set(SecondFactorLoginStep.PROVEN, Boolean.TRUE);
		flow.begin(context);
	}

	private static void confirm(ReactContext context, Person account) {
		LoginFlow.finish(context, SecondFactorLoginStep.class, account);
	}

	@Override
	protected void offer(PasskeyAuthentication ceremony, Object accountValue) {
		boolean passwordless = getConfig().getAccount() == null;
		PasskeyUsage usage = PasskeyService.getInstance().getUsage();
		if (passwordless && usage != PasskeyUsage.LOGIN) {
			hide(ceremony, com.top_logic.security.passkey.I18NConstants.ERROR_NO_PASSWORDLESS_LOGIN);
		} else if (!passwordless && usage != PasskeyUsage.SECOND_FACTOR) {
			hide(ceremony, com.top_logic.security.passkey.I18NConstants.ERROR_NO_SECOND_FACTOR);
		} else if (passwordless) {
			ceremony.offer(null);
		} else if (accountValue instanceof Person account) {
			ceremony.offer(account);
		} else {
			ceremony.withdraw(null);
		}
	}

	/**
	 * Withdraws the ceremony and hides its button, since the {@link PasskeyService#getUsage() usage}
	 * of the passkeys does not allow the kind of login of this element.
	 */
	private static void hide(PasskeyAuthentication ceremony, ResKey reason) {
		ceremony.withdraw(reason);
		ceremony.getControl().setHidden(true);
	}

}
