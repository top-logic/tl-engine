/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.login.LoginAction;
import com.top_logic.model.TLObject;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.invitation.VerificationCode;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} of the code dialog of an invitation: it checks the entered code against the
 * {@link VerificationCode} that {@link RequestInvitationCodeAction} mailed and replaces the dialog
 * with the {@link #CREATE_ACCOUNT_VIEW dialog creating the account}.
 *
 * <p>
 * The account dialog edits a transient {@link #ACCOUNT_CREATION_TYPE} whose user name is the
 * e-mail address of the invitation. It receives the invitation on the
 * {@link RequestInvitationCodeAction#INVITATION_CHANNEL} channel and, on the
 * {@link #PASSWORDS_CHANNEL} channel, whether the person chooses a password.
 * </p>
 */
@InApp
public class CheckInvitationCodeAction implements ViewAction {

	/** Dialog view (relative to {@link ViewLoader#VIEW_BASE_PATH}) creating the account. */
	public static final String CREATE_ACCOUNT_VIEW = "create-account.view.xml";

	/** Name of the transient model type holding the input of the account dialog. */
	public static final String ACCOUNT_CREATION_TYPE = "tl.login:AccountCreation";

	/** Attribute of {@link #ACCOUNT_CREATION_TYPE} holding the user name of the new account. */
	public static final String USER_NAME_ATTR = "userName";

	/**
	 * Dialog channel telling whether the account dialog asks for a password: whether the
	 * {@link InvitationModule#authenticationDevice() authentication device} of the new account
	 * manages passwords.
	 */
	public static final String PASSWORDS_CHANNEL = "passwords";

	/**
	 * Configuration for {@link CheckInvitationCodeAction}.
	 */
	@TagName("check-invitation-code")
	public interface Config extends PolymorphicConfiguration<CheckInvitationCodeAction> {

		@Override
		@ClassDefault(CheckInvitationCodeAction.class)
		Class<? extends CheckInvitationCodeAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link CheckInvitationCodeAction} from configuration.
	 */
	@CalledByReflection
	public CheckInvitationCodeAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		Object invitationValue = SelfServiceSupport.channel(context, RequestInvitationCodeAction.INVITATION_CHANNEL);
		Object codeValue = SelfServiceSupport.channel(context, RequestInvitationCodeAction.VERIFICATION_CHANNEL);
		if (!(invitationValue instanceof Invitation) || !(codeValue instanceof VerificationCode)) {
			throw new TopLogicException(
				com.top_logic.security.selfservice.I18NConstants.ERROR_VERIFICATION_CODE_EXPIRED);
		}
		Invitation invitation = (Invitation) invitationValue;
		((VerificationCode) codeValue)
			.check(SelfServiceSupport.stringValue((TLObject) input, RequestInvitationCodeAction.CODE_ATTR));
		InvitationModule.checkOpen(invitation);

		openAccountDialog(context, invitation);
		return input;
	}

	private static void openAccountDialog(ReactContext context, Invitation invitation) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager != null) {
			dialogManager.closeTopDialog(DialogResult.cancelled());
		}
		TLObject account = SelfServiceSupport.newTransient(ACCOUNT_CREATION_TYPE);
		account.tUpdateByName(USER_NAME_ATTR, invitation.getEmail());

		Map<String, Object> channels = new LinkedHashMap<>();
		channels.put(LoginAction.MODEL_CHANNEL, account);
		channels.put(RequestInvitationCodeAction.INVITATION_CHANNEL, invitation);
		channels.put(PASSWORDS_CHANNEL,
			Boolean.valueOf(InvitationModule.getInstance().authenticationDevice().allowPwdChange()));
		// Do not close on a backdrop click: an accidental click outside must not abort the acceptance.
		OpenDialogAction.openDialog(context, ViewLoader.VIEW_BASE_PATH + CREATE_ACCOUNT_VIEW, false, channels,
			Collections.emptyList());
	}

	/**
	 * Replaces the code dialog with the account dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
