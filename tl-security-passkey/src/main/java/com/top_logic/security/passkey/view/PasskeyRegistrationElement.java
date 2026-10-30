/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.command.ActionScopes;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActions;
import com.top_logic.layout.view.login.LoginAction;
import com.top_logic.layout.view.login.SecondFactorStep;
import com.top_logic.security.passkey.PasskeyCredentialRepository;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link UIElement} showing a button that creates a passkey for an account.
 *
 * <p>
 * The element serves two situations:
 * </p>
 * <ul>
 * <li>An account that is logged in creates a passkey of its own, e.g. in its settings. The account
 * is the one logged in, or the one on the {@link Config#getAccount() account channel}, which then
 * must be the one logged in. After the passkey is stored, the {@link Config#getOnSuccess() actions}
 * run with it as argument {@link #PASSKEY_ARGUMENT}, e.g. to update a list of the passkeys.</li>
 * <li>The login sets up a passkey as second factor ({@link Config#isCompleteLogin()}): the account
 * on the {@link Config#getAccount() account channel}, put there by the login after it checked the
 * password, is {@link MfaRequirement#REQUIRED required} to use a second factor and has none set up.
 * The registration runs with the verification of the user by the authenticator, so the stored
 * passkey confirms the account and the login is completed for it.</li>
 * </ul>
 *
 * <p>
 * An account that does not fit the situation gets no passkey: the button is disabled and tells
 * why.
 * </p>
 */
@InApp
public class PasskeyRegistrationElement
		extends PasskeyCeremonyElement<PasskeyRegistrationElement.Config, PasskeyRegistration> {

	/**
	 * Name of the argument holding the created passkey for the {@link Config#getOnSuccess() actions
	 * run after a registration}.
	 */
	public static final String PASSKEY_ARGUMENT = "passkey";

	/**
	 * Configuration of a {@link PasskeyRegistrationElement}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PasskeyCeremonyElement.Config, ActionScopeDefinition {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		/** The tag of the element in a view. */
		String TAG_NAME = "passkey-registration";

		/** Configuration name for {@link #isCompleteLogin()}. */
		String COMPLETE_LOGIN = "complete-login";

		/** Configuration name for {@link #getOnSuccess()}. */
		String ON_SUCCESS = "on-success";

		@Override
		@ClassDefault(PasskeyRegistrationElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Whether a stored passkey completes the login of the account on the
		 * {@link #getAccount() account channel}, which sets up its second factor during the login.
		 *
		 * <p>
		 * Requires the {@link #getAccount() account channel}, and excludes the
		 * {@link #getOnSuccess() actions after a registration}.
		 * </p>
		 */
		@Name(COMPLETE_LOGIN)
		boolean isCompleteLogin();

		/**
		 * Actions run after a passkey has been stored, with the passkey as argument
		 * {@link PasskeyRegistrationElement#PASSKEY_ARGUMENT}.
		 */
		@Name(ON_SUCCESS)
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		List<PolymorphicConfiguration<? extends ViewAction>> getOnSuccess();

		/**
		 * The {@link #getOnSuccess() actions after a registration} receive the passkey as argument
		 * {@link PasskeyRegistrationElement#PASSKEY_ARGUMENT}.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			if (ActionScopes.contains(getOnSuccess(), part)) {
				return ActionScope.single(PASSKEY_ARGUMENT);
			}
			return outer;
		}
	}

	private final GenericViewCommand _onSuccess;

	/**
	 * Creates a {@link PasskeyRegistrationElement} from configuration.
	 */
	@CalledByReflection
	public PasskeyRegistrationElement(InstantiationContext context, Config config) {
		super(context, config);
		if (config.isCompleteLogin()) {
			if (config.getAccount() == null) {
				context.error("A <" + Config.TAG_NAME + "> with '" + Config.COMPLETE_LOGIN
					+ "' requires the channel '" + Config.ACCOUNT + "' with the account of the login at "
					+ config.location() + ".");
			}
			if (!config.getOnSuccess().isEmpty()) {
				context.error("A <" + Config.TAG_NAME + "> with '" + Config.COMPLETE_LOGIN
					+ "' completes the login and runs no '" + Config.ON_SUCCESS + "' actions at "
					+ config.location() + ".");
			}
		}
		List<ViewAction> onSuccess =
			ViewActions.instantiate(context, config.getOnSuccess(), ActionScope.single(PASSKEY_ARGUMENT));
		_onSuccess = onSuccess.isEmpty() ? null : new GenericViewCommand(PASSKEY_ARGUMENT, onSuccess);
	}

	@Override
	protected PasskeyRegistration createCeremony(ViewContext context) {
		return new PasskeyRegistration(getConfig().isCompleteLogin() ? this::completeLogin : this::registered);
	}

	@Override
	protected void offer(PasskeyRegistration ceremony, Object accountValue) {
		Person account;
		if (getConfig().getAccount() == null) {
			account = TLContext.currentUser();
		} else {
			account = accountValue instanceof Person person ? person : null;
		}
		if (account == null) {
			ceremony.withdraw(null);
			return;
		}

		ResKey refusal = refusal(account, getConfig().isCompleteLogin(), TLContext.currentUser());
		if (refusal != null) {
			Logger.warn("Passkey registration for '" + account.getName() + "' refused at "
				+ getConfig().location() + ".", PasskeyRegistrationElement.class);
			ceremony.withdraw(refusal);
		} else {
			ceremony.offer(account);
		}
	}

	/**
	 * Why the given account must not create a passkey here, <code>null</code> if it may.
	 *
	 * @param account
	 *        The account to create the passkey for.
	 * @param completeLogin
	 *        Whether the passkey completes the login of the account, see
	 *        {@link Config#isCompleteLogin()}.
	 * @param currentUser
	 *        The account logged in where the passkey is created.
	 * @return The reason shown to the user, or <code>null</code>.
	 */
	public static ResKey refusal(Person account, boolean completeLogin, Person currentUser) {
		if (completeLogin) {
			// Completing the login is only safe for the account the login is setting a second factor
			// up for: an account with a second factor must confirm itself with it.
			SecondFactorStep step;
			try {
				step = SecondFactorStep.decide(account);
			} catch (TopLogicException ex) {
				return ex.getErrorKey();
			}
			if (step.getKind() != SecondFactorStep.Kind.ENROLL) {
				return I18NConstants.ERROR_NOT_ENROLLING__ACCOUNT.fill(account.getName());
			}
			return null;
		}
		if (!account.equals(currentUser)) {
			return I18NConstants.ERROR_NOT_CURRENT_USER;
		}
		return null;
	}

	private void completeLogin(ReactContext context, Passkey passkey) {
		Person account = PasskeyCredentialRepository.getOwner(passkey);
		LoginAction.completeLogin(context, account.getName());
	}

	private void registered(ReactContext context, Passkey passkey) {
		if (_onSuccess != null) {
			_onSuccess.execute(context, passkey);
		}
	}

}
