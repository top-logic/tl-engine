/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.login.LoginAction;
import com.top_logic.security.selfservice.invitation.InvitationLink;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.invitation.VerificationCode;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that starts the acceptance of an {@link Invitation}: it mails a
 * {@link VerificationCode} to the address of the invitation and opens the
 * {@link #INVITATION_CODE_VIEW dialog} in which the invited person enters it.
 *
 * <p>
 * The action processes the token of the invitation link (see
 * {@link InvitationLink#INVITATION_PARAMETER}) or the invitation itself; without either, it takes
 * the invitation from the {@link #INVITATION_CHANNEL} channel of its view, i.e. it sends a new code
 * from the code dialog. It refuses an invitation that is not open with the reason, and a request
 * that follows the previous one for the same invitation too soon. Without a configured mail
 * server, the request is refused and no dialog opens: the code is only ever sent by mail.
 * </p>
 *
 * <p>
 * The dialog receives the invitation on the {@link #INVITATION_CHANNEL} channel and the code on
 * the {@link #VERIFICATION_CHANNEL} channel; both stay on the server. The top dialog open already,
 * e.g. the code dialog asking for a new code, is replaced.
 * </p>
 *
 * @implNote The invitation is looked up with {@link InvitationModule#findOpenInvitation(String)},
 *           the mail is sent through
 *           {@link InvitationModule#sendVerificationMail(Invitation, String)}, requests are
 *           throttled by {@link VerificationCode#throttleRequest(String)}.
 */
@InApp
public class RequestInvitationCodeAction implements ViewAction {

	/** Dialog view (relative to {@link ViewLoader#VIEW_BASE_PATH}) for entering the code. */
	public static final String INVITATION_CODE_VIEW = "invitation-code.view.xml";

	/** Name of the transient model type holding the entered code. */
	public static final String CODE_ENTRY_TYPE = "tl.login:OtpEntry";

	/** Attribute of {@link #CODE_ENTRY_TYPE} holding the entered code. */
	public static final String CODE_ATTR = "code";

	/** Dialog channel carrying the {@link Invitation} being accepted. */
	public static final String INVITATION_CHANNEL = "invitation";

	/** Dialog channel carrying the {@link VerificationCode} that was mailed. */
	public static final String VERIFICATION_CHANNEL = "verification";

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
		Invitation invitation = openInvitation(context, input);
		if (!MailSenderService.isConfigured()) {
			throw new TopLogicException(
				com.top_logic.security.selfservice.I18NConstants.ERROR_MAIL_NOT_CONFIGURED);
		}
		VerificationCode.throttleRequest(THROTTLE_KEY_PREFIX + invitation.getId());

		VerificationCode code = new VerificationCode();
		deliverCode(invitation, code);

		openCodeDialog(context, invitation, code);
		InfoService.showInfo(I18NConstants.INVITATION_CODE_SENT);
		return input;
	}

	private static Invitation openInvitation(ReactContext context, Object input) {
		Object value = input instanceof Invitation || input instanceof CharSequence ? input
			: SelfServiceSupport.channel(context, INVITATION_CHANNEL);
		if (value instanceof Invitation) {
			Invitation invitation = (Invitation) value;
			InvitationModule.checkOpen(invitation);
			return invitation;
		}
		return InvitationModule.getInstance().findOpenInvitation(value == null ? null : value.toString());
	}

	private static void deliverCode(Invitation invitation, VerificationCode code) {
		if (!InvitationModule.getInstance().sendVerificationMail(invitation, code.getCode())) {
			throw new TopLogicException(
				com.top_logic.security.selfservice.I18NConstants.ERROR_VERIFICATION_CODE_NOT_SENT);
		}
	}

	private static void openCodeDialog(ReactContext context, Invitation invitation, VerificationCode code) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(LoginAction.MODEL_CHANNEL, SelfServiceSupport.newTransient(CODE_ENTRY_TYPE));
		channels.put(INVITATION_CHANNEL, invitation);
		channels.put(VERIFICATION_CHANNEL, code);
		// Do not close on a backdrop click: an accidental click outside must not abort the acceptance.
		OpenDialogAction.openDialog(context, ViewLoader.VIEW_BASE_PATH + INVITATION_CODE_VIEW, false, channels,
			Collections.emptyList());
	}

	/**
	 * Opens the code dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
