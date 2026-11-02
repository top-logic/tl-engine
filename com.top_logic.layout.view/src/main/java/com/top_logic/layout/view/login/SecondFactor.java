/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;

/**
 * A method of the React login to confirm an account with a second factor, after its password has
 * been checked.
 *
 * <p>
 * The methods an application offers are listed in {@link SecondFactorsConfig}. After the password
 * {@link LoginAction} asks every method whether the account has it {@link #isEnrolled(Person) set
 * up} and lets the account confirm itself with one of those, see {@link SecondFactorStep}. An
 * account whose {@link MfaRequirement} is {@link MfaRequirement#REQUIRED required} and that has no
 * method set up sets one up before the session starts.
 * </p>
 *
 * <p>
 * Both steps are dialogs of the method's own: they replace the current login dialog (see
 * {@link LoginAction#replaceDialog(ReactContext, String, java.util.Map)}), carry the account on the
 * dialog channel {@link LoginAction#ACCOUNT_CHANNEL}, and end the login with
 * {@link LoginAction#completeLogin(ReactContext, String)} once the account has proven itself.
 * Cancelling the dialog cancels the login.
 * </p>
 *
 * @see AbstractSecondFactor
 */
public interface SecondFactor {

	/**
	 * Configuration of a {@link SecondFactor}.
	 *
	 * <p>
	 * The {@link #getName() name} identifies the method in the {@link SecondFactorsConfig}, so that
	 * an application configuration can address it.
	 * </p>
	 */
	interface Config<I extends SecondFactor> extends PolymorphicConfiguration<I>, NamedConfigMandatory {

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/**
		 * The name of the method shown to a user who chooses between several methods.
		 */
		@Name(LABEL)
		@Mandatory
		ResKey getLabel();

		/**
		 * The icon shown next to the {@link #getLabel() label}.
		 */
		@Name(IMAGE)
		@Nullable
		ThemeImage getImage();
	}

	/**
	 * The name of this method shown to a user who chooses between several methods.
	 */
	ResKey getLabel();

	/**
	 * The icon of this method next to its {@link #getLabel() label}, <code>null</code> for none.
	 */
	ThemeImage getIcon();

	/**
	 * Whether the given account has this method set up, so that it can confirm a login with it.
	 */
	boolean isEnrolled(Person account);

	/**
	 * Whether the given account may set this method up.
	 */
	boolean canEnroll(Person account);

	/**
	 * Opens the step in which the given account confirms its login with this method.
	 *
	 * <p>
	 * Called only for an account that {@link #isEnrolled(Person) has this method set up}. The step
	 * ends with {@link LoginAction#completeLogin(ReactContext, String)} once the account has proven
	 * itself.
	 * </p>
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param account
	 *        The account whose password has been checked.
	 */
	void verify(ReactContext context, Person account);

	/**
	 * Opens the step in which the given account sets this method up during its login.
	 *
	 * <p>
	 * Called only for an account that is {@link MfaRequirement#REQUIRED required} to use a second
	 * factor, has none {@link #isEnrolled(Person) set up} and {@link #canEnroll(Person) may set this
	 * one up}. The step ends with {@link LoginAction#completeLogin(ReactContext, String)} once the
	 * method is set up and has confirmed the account.
	 * </p>
	 *
	 * @param context
	 *        The context of the dialog the login is in.
	 * @param account
	 *        The account whose password has been checked.
	 */
	void enroll(ReactContext context, Person account);

}
