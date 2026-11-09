/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import java.util.Arrays;
import java.util.Objects;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.login.LoginFlow;
import com.top_logic.layout.view.login.LoginStep;
import com.top_logic.model.TLObject;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} of the account dialog of an invitation: it creates the account and logs the
 * new user in.
 *
 * <p>
 * The input is the {@link CheckInvitationCodeAction#ACCOUNT_CREATION_TYPE} the dialog edits, the
 * invitation comes from the {@link RequestInvitationCodeAction#INVITATION_CHANNEL} channel. A
 * refusal - missing surname, passwords that differ or violate the password rules, an invitation
 * that is no longer open, the {@link Invitation#getCreateCallback() callback} of the invitation -
 * is reported and leaves the dialog open.
 * </p>
 *
 * <p>
 * The new account then runs the {@link LoginStep}s of the React login in a {@link LoginFlow},
 * exactly as after a password login, e.g. sets a second factor up if the invitation requires one.
 * After the last step the window loads the root of the view application
 * ({@link ViewServlet#ROOT_PATH}), not the invitation page again.
 * </p>
 *
 * @implNote The account is created by
 *           {@link InvitationModule#acceptInvitation(Invitation, String, String, char[])}.
 */
@InApp
public class AcceptInvitationAction implements ViewAction {

	/** Attribute of {@link CheckInvitationCodeAction#ACCOUNT_CREATION_TYPE} holding the surname. */
	public static final String SURNAME_ATTR = "surname";

	/** Attribute of {@link CheckInvitationCodeAction#ACCOUNT_CREATION_TYPE} holding the given name. */
	public static final String GIVEN_NAME_ATTR = "givenName";

	/** Attribute of {@link CheckInvitationCodeAction#ACCOUNT_CREATION_TYPE} holding the password. */
	public static final String NEW_PASSWORD_ATTR = "newPassword";

	/**
	 * Attribute of {@link CheckInvitationCodeAction#ACCOUNT_CREATION_TYPE} holding the repetition of
	 * the password.
	 */
	public static final String NEW_PASSWORD_CONFIRM_ATTR = "newPasswordConfirm";

	/**
	 * Configuration for {@link AcceptInvitationAction}.
	 */
	@TagName("accept-invitation")
	public interface Config extends PolymorphicConfiguration<AcceptInvitationAction> {

		@Override
		@ClassDefault(AcceptInvitationAction.class)
		Class<? extends AcceptInvitationAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link AcceptInvitationAction} from configuration.
	 */
	@CalledByReflection
	public AcceptInvitationAction(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(input instanceof TLObject)) {
			return input;
		}
		Object invitationValue = SelfServiceSupport.channel(context, RequestInvitationCodeAction.INVITATION_CHANNEL);
		if (!(invitationValue instanceof Invitation)) {
			throw new TopLogicException(com.top_logic.security.selfservice.I18NConstants.ERROR_UNKNOWN_INVITATION);
		}
		Invitation invitation = (Invitation) invitationValue;
		TLObject form = (TLObject) input;

		String surname = SelfServiceSupport.stringValue(form, SURNAME_ATTR);
		if (StringServices.isEmpty(surname)) {
			throw new TopLogicException(I18NConstants.SURNAME_EMPTY);
		}
		String givenName = SelfServiceSupport.stringValue(form, GIVEN_NAME_ATTR);

		InvitationModule invitations = InvitationModule.getInstance();
		AuthenticationDevice device = invitations.authenticationDevice();
		char[] password = null;
		if (device.allowPwdChange()) {
			String newPassword = SelfServiceSupport.stringValue(form, NEW_PASSWORD_ATTR);
			String newPasswordConfirm = SelfServiceSupport.stringValue(form, NEW_PASSWORD_CONFIRM_ATTR);
			if (!Objects.equals(newPassword, newPasswordConfirm)) {
				throw new TopLogicException(I18NConstants.PASSWORD_MISMATCH);
			}
			password = StringServices.nonNull(newPassword).toCharArray();
		}

		Person account;
		try {
			account = invitations.acceptInvitation(invitation, surname.trim(), trimmed(givenName), password);
		} finally {
			if (password != null) {
				Arrays.fill(password, (char) 0);
			}
		}

		LoginFlow flow = new LoginFlow(account);
		flow.set(LoginFlow.LANDING_PAGE, context.getContextPath() + ViewServlet.ROOT_PATH);
		flow.begin(context);
		return input;
	}

	private static String trimmed(String value) {
		return value == null ? null : value.trim();
	}

	/**
	 * Logs the new user in or opens the dialog of a login step.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
