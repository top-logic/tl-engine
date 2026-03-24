/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.util.ResKey;
import com.top_logic.contact.business.Account;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.gui.layout.person.ChangePasswordComponent;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
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
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.model.ModelService;

/**
 * Dialog to create a new user together with setting the passwords.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateAccountDialog extends AbstractTemplateDialog {

	private static final String USER_NAME_FIELD = "userName";

	private static final String SUR_NAME_FIELD = "surName";

	private static final String GIVEN_NAME_FIELD = "givenName";

	private Invitation _invitation;

	private Command _continuation;

	private AuthenticationDevice _authenticationDevice;

	/**
	 * Creates a new {@link CreateAccountDialog}.
	 */
	public CreateAccountDialog(Invitation invitation, Command continuation) {
		this(invitation, continuation, I18NConstants.CREATE_LOGIN_DIALOG_TITLE, 
			DisplayDimension.dim(600, DisplayUnit.PIXEL),
			DisplayDimension.dim(400, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link CreateAccountDialog}.
	 */
	public CreateAccountDialog(Invitation invitation, Command continuation, ResKey dialogTitle, DisplayDimension width,
			DisplayDimension height) {
		super(dialogTitle, width, height);
		_invitation = invitation;
		_continuation = continuation;
		_authenticationDevice = TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity");
	}

	/**
	 * Creates a new {@link CreateAccountDialog}.
	 */
	public CreateAccountDialog(Invitation invitation, Command continuation, DialogModel dialogModel) {
		super(dialogModel);
		_invitation = invitation;
		_continuation = continuation;
		_authenticationDevice = TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity");
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
		String userName = ((StringField) formContext.getField(USER_NAME_FIELD)).getAsString();
		String surName = ((StringField) formContext.getField(SUR_NAME_FIELD)).getAsString();
		String givenName = ((StringField) formContext.getField(GIVEN_NAME_FIELD)).getAsString();
		Account person;
		KnowledgeBase kb = _invitation.tKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.UPDATED_INVITATION_CODE)) {
			person = (Account) Person.create(kb, userName, _authenticationDevice.getDeviceID());
			person.setMFARequirement(_invitation.getMfaRequirement());
			
			UserInterface user = person.getUser();
			user.setName(surName);
			if (!givenName.isEmpty()) {
				user.setFirstName(givenName);
			}
			user.setEMail(_invitation.getEmail());

			if (_authenticationDevice.allowPwdChange()) {
				HandlerResult result = ChangePasswordComponent.ApplyPasswordCommand.applyPasswordChange(formContext, person);
				if (!result.isSuccess()) {
					tx.rollback();
					return result;
				}
			}

			SearchExpression createCallback = _invitation.getCreateCallback();
			if (createCallback != null) {
				QueryExecutor callback = QueryExecutor.executor(kb, ModelService.getApplicationModel(), createCallback);
				Object result = callback.execute(person);
				if (result != null) {
					ResKey deniedKey = SearchExpression.asResKey(result);
					if (deniedKey != null) {
						HandlerResult error = HandlerResult.error(deniedKey);
						error.setErrorTitle(I18NConstants.CREATE_ACCOUNT_NOT_POSSIBLE);
						return error;
					} else {
						return HandlerResult.error(I18NConstants.CREATE_ACCOUNT_NOT_POSSIBLE);
					}
				}
			}
			tx.commit();
		}

		MfaRequirement mfaRequirement = person.getMFARequirement();
		if (mfaRequirement != null) {
			switch (mfaRequirement) {
				case DISABLED:
					break;
				case OPTIONAL: {
					Command login = ctx -> loginNewPerson(ctx, person);
					Command continuation = login.andThen(closeAndContinue());
					return EnableMultiFactorAuthenticationDialog.informMFAOptional(context, person, continuation);
				}
				case REQUIRED: {
					/* Ensure that the user is just logged in when he has finished the multi-factor
					 * authentication. */
					Command login = ctx -> loginNewPerson(ctx, person);
					Command continuation = closeAndContinue();
					return EnableMultiFactorAuthenticationDialog.informMFARequired(context, person, continuation,
						login);
				}
				default:
					break;
			}
		}
			
		loginNewPerson(context, person);
		return closeAndContinue().executeCommand(context);
	}

	private Command closeAndContinue() {
		return getDiscardClosure().andThen(_continuation);
	}

	private HandlerResult loginNewPerson(DisplayContext context, Account person) {
		LoginViewDialog.loginUserAndReload(context, person);
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.OK, getApplyClosure()));
	}

}

