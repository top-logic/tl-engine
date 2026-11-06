/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation.script;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.model.Invitation;

/**
 * TL-Script functions managing {@link Invitation}s.
 *
 * <p>
 * The functions open a transaction of their own, so they are called from a command without
 * transaction.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #resend(Invitation)} is called <code>invitationResend</code>.
 *
 * @see SendInvitation
 */
@ScriptPrefix(InvitationFunctions.PREFIX)
public class InvitationFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "invitation";

	/**
	 * Gives an open or expired invitation a new link and a new expiry date, without sending it.
	 *
	 * <p>
	 * The previous link of the invitation is no longer valid. An accepted invitation is refused.
	 * </p>
	 *
	 * @param invitation
	 *        The invitation to renew.
	 *
	 * @implNote Calls {@link InvitationModule#renewInvitation(Invitation)}.
	 */
	@Label("Renew invitation")
	public static void renew(@Mandatory Invitation invitation) {
		InvitationModule.getInstance().renewInvitation(invitation);
	}

	/**
	 * Sends an open or expired invitation again.
	 *
	 * <p>
	 * An open invitation is sent with its link, an expired invitation is renewed first and sent
	 * with a new link and a new expiry date. An accepted invitation is refused.
	 * </p>
	 *
	 * @param invitation
	 *        The invitation to send again.
	 * @return The result of the invitation mail script, by default whether the mail was sent.
	 *
	 * @implNote Calls {@link InvitationModule#resendInvitation(Invitation)}.
	 */
	@Label("Send invitation again")
	public static Object resend(@Mandatory Invitation invitation) {
		return InvitationModule.getInstance().resendInvitation(invitation);
	}

	/**
	 * The reason why the invitation of a link cannot be accepted.
	 *
	 * <p>
	 * The page an invitation link leads to shows the reason to the visitor, who is not logged in.
	 * The lookup does not depend on access rights: the token of the link is the proof.
	 * </p>
	 *
	 * @param token
	 *        The token from the invitation link.
	 * @return The message telling that there is no invitation with the token, or that it has
	 *         expired or has been accepted; <code>null</code> if the invitation is open.
	 *
	 * @implNote Calls {@link InvitationModule#refusal(String)}.
	 */
	@Label("Refusal of an invitation")
	@SideEffectFree
	public static ResKey refusal(String token) {
		return InvitationModule.getInstance().refusal(token);
	}
}
