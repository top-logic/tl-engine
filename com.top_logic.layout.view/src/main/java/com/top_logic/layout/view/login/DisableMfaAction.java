/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.WithValueArgument;
import com.top_logic.util.error.TopLogicException;

/**
 * Takes the one-time codes of an authenticator app ({@link TotpSecondFactor}) off the account
 * passed as its input.
 *
 * <p>
 * An account that may log in without a second factor logs in with the password alone afterwards:
 * the stored secret and the requirement are cleared together, since a requirement kept without a
 * secret is the state that sends the next login into enrollment, which is the opposite of switching
 * the second factor off.
 * </p>
 *
 * <p>
 * An account whose second factor is demanded of it - {@link MfaRequirement#REQUIRED} imposed rather
 * than chosen - keeps that demand. It drops the authenticator app only while it keeps another
 * {@link SecondFactorStep#configured() configured} method of the login it has set up; the secret is
 * then cleared and the requirement stays. Without another method the action refuses: the app is
 * the only second factor the account has. Whether that is the case is a question about the
 * account, so it is answered here rather than left to the view offering the action.
 * </p>
 *
 * @see EnableMfaAction
 * @see ResetMfaAction
 */
@InApp
public class DisableMfaAction implements ViewAction {

	/**
	 * Configuration for {@link DisableMfaAction}.
	 */
	@TagName("disable-mfa")
	public interface Config extends PolymorphicConfiguration<DisableMfaAction>, WithValueArgument {

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

		ResKey message = disable(account, SecondFactorStep.configured());

		ViewMessages.info(context, message);
		return input;
	}

	/**
	 * Takes the authenticator app off the given account, see the class description.
	 *
	 * @param account
	 *        The account to take the authenticator app off.
	 * @param methods
	 *        The second factor methods of the login.
	 * @return The message telling the user what was done.
	 * @throws TopLogicException
	 *         If the account is {@link MfaRequirement#REQUIRED required} to use a second factor and
	 *         has no other method set up, see {@link #refusal(Person, List)}.
	 */
	public static ResKey disable(Person account, List<? extends SecondFactor> methods) {
		ResKey refusal = refusal(account, methods);
		if (refusal != null) {
			throw new TopLogicException(refusal);
		}

		boolean required = account.getMFARequirement() == MfaRequirement.REQUIRED;
		ResKey message = required
			? I18NConstants.MFA_TOTP_REMOVED__USER.fill(account.getName())
			: I18NConstants.MFA_DISABLED__USER.fill(account.getName());
		try (Transaction tx = account.tKnowledgeBase().beginTransaction(message)) {
			account.tUpdateByName(Person.MFA_SECRET_ATTR, null);
			if (!required) {
				account.setMFARequirement(MfaRequirement.OPTIONAL);
			}
			tx.commit();
		}
		return message;
	}

	/**
	 * Why the authenticator app must not be taken off the given account, <code>null</code> if it
	 * may.
	 *
	 * @param account
	 *        The account to take the authenticator app off.
	 * @param methods
	 *        The second factor methods of the login. Methods for the authenticator app are not
	 *        asked: it is the factor being taken off.
	 * @return The reason shown to the user if the account is {@link MfaRequirement#REQUIRED
	 *         required} to use a second factor and has no other method set up, <code>null</code>
	 *         otherwise.
	 */
	public static ResKey refusal(Person account, List<? extends SecondFactor> methods) {
		if (account.getMFARequirement() != MfaRequirement.REQUIRED) {
			return null;
		}
		for (SecondFactor method : methods) {
			if (!(method instanceof TotpSecondFactor) && method.isEnrolled(account)) {
				return null;
			}
		}
		return I18NConstants.ERROR_MFA_REQUIRED__USER.fill(account.getName());
	}

	/**
	 * Tells the user that the authenticator app was taken off.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
