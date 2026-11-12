/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.security.selfservice.invitation.InvitationLink;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.invitation.VerificationCode;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that starts the acceptance of an {@link Invitation}: it mails a
 * {@link VerificationCode} to the address of the invitation and opens the code dialog in which the
 * invited person enters it (see {@link CheckVerificationCodeAction}).
 *
 * <p>
 * The action processes the token of the invitation link (see
 * {@link InvitationLink#INVITATION_PARAMETER}) or the invitation itself. It refuses an invitation
 * that is not open with the reason, and a request that follows the previous one for the same
 * invitation too soon. Without a configured mail server, the request is refused and no dialog
 * opens: the code is only ever sent by mail.
 * </p>
 *
 * <p>
 * The dialog carries the invitation as {@link InvitationRequest} and the code on its channels; both
 * stay on the server. The top dialog open already, e.g. the code dialog asking for a new code, is
 * replaced.
 * </p>
 *
 * @implNote The invitation is looked up with {@link InvitationModule#findOpenInvitation(String)},
 *           the mail is sent through
 *           {@link InvitationModule#sendVerificationMail(Invitation, String)}, requests are
 *           throttled by {@link VerificationCode#throttleRequest(String)}.
 */
@InApp
public class RequestInvitationCodeAction implements ViewAction {

	/** Prefix of the key under which code requests are throttled, followed by the token. */
	private static final String THROTTLE_KEY_PREFIX = "invitation-code:";

	/**
	 * Configuration for {@link RequestInvitationCodeAction}.
	 */
	@TagName("request-invitation-code")
	public interface Config extends PolymorphicConfiguration<RequestInvitationCodeAction> {

		@Override
		@ClassDefault(RequestInvitationCodeAction.class)
		Class<? extends RequestInvitationCodeAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link RequestInvitationCodeAction} from configuration.
	 */
	@CalledByReflection
	public RequestInvitationCodeAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		sendCode(context, openInvitation(input));
		return input;
	}

	/**
	 * Mails a new code to the address of the given invitation and opens the code dialog in place of
	 * the top dialog.
	 *
	 * @param context
	 *        The context of the action.
	 * @param invitation
	 *        The invitation to accept.
	 * @throws TopLogicException
	 *         If the invitation is not open, the request must wait, or the mail cannot be sent.
	 */
	public static void sendCode(ReactContext context, Invitation invitation) {
		InvitationModule.checkOpen(invitation);
		if (!MailSenderService.isConfigured()) {
			throw new TopLogicException(
				com.top_logic.security.selfservice.I18NConstants.ERROR_MAIL_NOT_CONFIGURED);
		}
		VerificationCode.throttleRequest(THROTTLE_KEY_PREFIX + invitation.getId());

		VerificationCode code = new VerificationCode();
		if (!InvitationModule.getInstance().sendVerificationMail(invitation, code.getCode())) {
			throw new TopLogicException(
				com.top_logic.security.selfservice.I18NConstants.ERROR_VERIFICATION_CODE_NOT_SENT);
		}

		CheckVerificationCodeAction.openCodeDialog(context, new InvitationRequest(invitation), code);
		InfoService.showInfo(I18NConstants.INVITATION_CODE_SENT);
	}

	private static Invitation openInvitation(Object input) {
		if (input instanceof Invitation) {
			return (Invitation) input;
		}
		return InvitationModule.getInstance().findOpenInvitation(input == null ? null : input.toString());
	}

	/**
	 * Opens the code dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
