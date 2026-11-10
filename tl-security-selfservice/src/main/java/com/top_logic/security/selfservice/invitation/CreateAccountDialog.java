/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.gui.layout.person.ChangePasswordComponent;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.configuration.EnableMultiFactorAuthenticationDialog;
import com.top_logic.layout.component.configuration.LoginViewDialog;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * Dialog in which an invited person creates the account of an open {@link Invitation}.
 *
 * <p>
 * The user name is the e-mail address of the invitation. The person enters surname, given name
 * and, if the {@link InvitationModule#authenticationDevice() authentication device} manages
 * passwords, the password. The account is created by
 * {@link InvitationModule#acceptInvitation(Invitation, String, String, char[])}; afterwards the
 * new person sets up a second factor, if the invitation asks for one, and is logged in.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateAccountDialog extends AbstractTemplateDialog {

	private static final String USER_NAME_FIELD = "userName";

	private static final String SUR_NAME_FIELD = "surName";

	private static final String GIVEN_NAME_FIELD = "givenName";

	private final Invitation _invitation;

	private final AuthenticationDevice _authenticationDevice;

	/**
	 * Creates a new {@link CreateAccountDialog}.
	 */
	public CreateAccountDialog(Invitation invitation) {
		this(invitation, I18NConstants.CREATE_LOGIN_DIALOG_TITLE,
			DisplayDimension.dim(600, DisplayUnit.PIXEL),
			DisplayDimension.dim(400, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link CreateAccountDialog}.
	 */
	public CreateAccountDialog(Invitation invitation, ResKey dialogTitle, DisplayDimension width,
			DisplayDimension height) {
		super(dialogTitle, width, height);
		_invitation = invitation;
		_authenticationDevice = InvitationModule.getInstance().authenticationDevice();
	}

	/**
	 * Creates a new {@link CreateAccountDialog}.
	 */
	public CreateAccountDialog(Invitation invitation, DialogModel dialogModel) {
		super(dialogModel);
		_invitation = invitation;
		_authenticationDevice = InvitationModule.getInstance().authenticationDevice();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected ResPrefix getResourcePrefix() {
		return I18NConstants.CREATE_ACCOUNT_DIALOG;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField userName = FormFactory.newStringField(USER_NAME_FIELD);
		userName.setDisabled(true);
		userName.initializeField(_invitation.getEmail());
		context.addMember(userName);

		StringField surName = FormFactory.newStringField(SUR_NAME_FIELD);
		surName.setMandatory(true);
		context.addMember(surName);

		StringField givenName = FormFactory.newStringField(GIVEN_NAME_FIELD);
		context.addMember(givenName);

		if (_authenticationDevice.allowPwdChange()) {
			ChangePasswordComponent.addPasswordFields(context, _authenticationDevice.getPasswordValidator());
		}
	}

	@Override
	protected TagTemplate getTemplate() {
		List<HTMLTemplateFragment> fragments = new ArrayList<>();
		fragments.add(fieldBox(USER_NAME_FIELD));
		fragments.add(fieldBox(GIVEN_NAME_FIELD));
		fragments.add(fieldBox(SUR_NAME_FIELD));
		if (_authenticationDevice.allowPwdChange()) {
			fragments.add(fieldBox(ChangePasswordComponent.NEW_PASSWORD_1));
			fragments.add(fieldBox(ChangePasswordComponent.NEW_PASSWORD_2));
		}
		return div(fragments.toArray(HTMLTemplateFragment[]::new));
	}

	@Override
	public Command getApplyClosure() {
		return checkContextCommand()
			.andThen(this::createAccount);
	}

	private HandlerResult createAccount(DisplayContext context) {
		FormContext formContext = getFormContext();
		String surName = ((StringField) formContext.getField(SUR_NAME_FIELD)).getAsString();
		String givenName = ((StringField) formContext.getField(GIVEN_NAME_FIELD)).getAsString();

		char[] password = null;
		if (_authenticationDevice.allowPwdChange()) {
			String password1 = (String) formContext.getField(ChangePasswordComponent.NEW_PASSWORD_1).getValue();
			String password2 = (String) formContext.getField(ChangePasswordComponent.NEW_PASSWORD_2).getValue();
			if (!Utils.equals(password1, password2)) {
				return HandlerResult.error(com.top_logic.knowledge.gui.layout.person.I18NConstants.PASSWORD_MISSMATCH);
			}
			password = StringServices.nonNull(password1).toCharArray();
		}

		Person person;
		try {
			person = InvitationModule.getInstance().acceptInvitation(_invitation, surName, givenName, password);
		} catch (TopLogicException ex) {
			HandlerResult error = new HandlerResult();
			error.setException(ex);
			error.setErrorTitle(I18NConstants.CREATE_ACCOUNT_NOT_POSSIBLE);
			return error;
		} finally {
			if (password != null) {
				Arrays.fill(password, '\0');
			}
		}

		MfaRequirement mfaRequirement = person.getMFARequirement();
		if (mfaRequirement != null) {
			switch (mfaRequirement) {
				case DISABLED:
					break;
				case OPTIONAL: {
					Command login = ctx -> loginNewPerson(ctx, person);
					Command continuation = login.andThen(getDiscardClosure());
					return EnableMultiFactorAuthenticationDialog.informMFAOptional(context, person, continuation);
				}
				case REQUIRED: {
					/* Ensure that the user is just logged in when he has finished the multi-factor
					 * authentication. */
					Command login = ctx -> loginNewPerson(ctx, person);
					Command continuation = getDiscardClosure();
					return EnableMultiFactorAuthenticationDialog.informMFARequired(context, person, continuation,
						login);
				}
				default:
					break;
			}
		}

		HandlerResult login = loginNewPerson(context, person);
		if (!login.isSuccess()) {
			return login;
		}
		return getDiscardClosure().executeCommand(context);
	}

	private HandlerResult loginNewPerson(DisplayContext context, Person person) {
		return LoginViewDialog.loginUserAndReload(context, person);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.OK, getApplyClosure()));
	}

}
