/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation.script;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.security.selfservice.invitation.SelfRegistration;
import com.top_logic.security.selfservice.model.Registration;

/**
 * TL-Script functions for accounts registered by visitors without an invitation.
 *
 * <p>
 * Functions changing data open a transaction of their own, so they are called from a command
 * without transaction.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #approve(Registration)} is called <code>registrationApprove</code>.
 *
 * @see SelfRegistration
 */
@ScriptPrefix(RegistrationFunctions.PREFIX)
public class RegistrationFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "registration";

	/**
	 * Whether visitors can register an account of their own in this application.
	 *
	 * @return <code>true</code> if registration is switched on and the application can send mails.
	 *
	 * @implNote Calls {@link SelfRegistration#isEnabled()}.
	 */
	@Label("Registration enabled")
	@SideEffectFree
	public static boolean enabled() {
		return SelfRegistration.getInstance().isEnabled();
	}

	/**
	 * Whether a visitor registering an account must solve a captcha before a verification code is
	 * sent.
	 *
	 * @return <code>true</code> if the self-registration requires a captcha.
	 *
	 * @implNote Calls {@link SelfRegistration#isCaptchaRequired()}.
	 */
	@Label("Registration requires captcha")
	@SideEffectFree
	public static boolean captcha() {
		return SelfRegistration.getInstance().isCaptchaRequired();
	}

	/**
	 * Approves a pending registration: unlocks the registered account and sends the approval mail.
	 *
	 * <p>
	 * An approved registration is refused.
	 * </p>
	 *
	 * @param registration
	 *        The registration to approve.
	 * @return The result of the approval mail script, by default whether the mail was sent;
	 *         <code>null</code> if no approval mail is configured.
	 *
	 * @implNote Calls {@link SelfRegistration#approve(Registration)}.
	 */
	@Label("Approve registration")
	public static Object approve(@Mandatory Registration registration) {
		return SelfRegistration.getInstance().approve(registration);
	}

	/**
	 * The registration of an account that waits for its approval.
	 *
	 * @param account
	 *        The account.
	 * @return The pending registration of the account; <code>null</code> if the account was not
	 *         registered by its owner, its registration is approved or the account is not locked.
	 *
	 * @implNote Calls {@link SelfRegistration#pendingRegistration(Person)}.
	 */
	@Label("Pending registration")
	@SideEffectFree
	public static Registration pending(Person account) {
		return SelfRegistration.pendingRegistration(account);
	}
}
