/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.Version;
import com.top_logic.contact.business.Account;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.component.configuration.ChangePasswordDialog;
import com.top_logic.layout.component.configuration.LoginViewDialog;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Dialog to display when a user has forgot its password.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ForgotPasswordDialog extends AbstractVerificationCodeDialog {

	private static final String USERNAME_FIELD = "username";

	/** The account to which the code was sent. */
	private Person _account;

	/**
	 * Creates a new {@link ForgotPasswordDialog} with default values.
	 */
	public ForgotPasswordDialog() {
		this(I18NConstants.FORGOT_PASSWORD_DIALOG_TITLE,
			DisplayDimension.dim(500, DisplayUnit.PIXEL),
			DisplayDimension.dim(350, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link ForgotPasswordDialog}.
	 */
	public ForgotPasswordDialog(ResKey dialogTitle, DisplayDimension width, DisplayDimension height) {
		super(dialogTitle, width, height);
	}

	/**
	 * Creates a new {@link ForgotPasswordDialog}.
	 */
	public ForgotPasswordDialog(DialogModel dialogModel) {
		super(dialogModel);
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(
			div(resource(I18NConstants.FORGOT_PASSWORD_USERNAME_MESSAGE)),
			fieldBox(USERNAME_FIELD),
			fieldBox(CODE_FIELD));

	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField userField = FormFactory.newStringField(USERNAME_FIELD, FormFactory.MANDATORY,
			!FormFactory.IMMUTABLE, FormFactory.NO_CONSTRAINT);
		userField.setLabel(I18NConstants.FORGOT_PASSWORD_USERNAME_FIELD);
		userField.setTransient(true);
		context.addMember(userField);

		StringField codeField = createCodeField();
		codeField.setMandatory(false);
		context.addMember(codeField);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.OK,
			checkContextCommand()
				.andThen(ctx -> checkCode(this::failureKey))
				.andThen(this::openChangePasswordDialog)));

		buttons.add(MessageBox.button(I18NConstants.REQUEST_CODE,
			checkContextCommand()
				.andThen(ctx -> updateCode(this::handleNewCode, this::failureKey))));
	}

	private HandlerResult openChangePasswordDialog(DisplayContext context) {
		Person account = _account;

		Command continuation = getDiscardClosure().andThen(ctx -> {
			LoginViewDialog.loginUserAndReload(ctx, account);
			return HandlerResult.DEFAULT_RESULT;
		});
		ChangePasswordDialog changePassword = new ChangePasswordDialog(account, continuation);
		return changePassword.open(context);
	}

	private void handleNewCode(String code) {
		Person person = Person.byName(userName());
		if (person == null) {
			// No message output to avoid user fishing
			displayMailSent();
			return;
		}

		if (!person.getAuthenticationDevice().allowPwdChange()) {
			// No message output to avoid user fishing
			displayMailSent();
			return;
		}

		String eMail = ((Account) person).getUser().getEMail();
		if (eMail != null) {
			InvitationModule module = InvitationModule.getInstance();
			_account = person;
			module.getResetPasswordMail().execute(eMail, Version.getApplicationName(), code);
		}

		displayMailSent();
		return;

	}

	private String failureKey() {
		return userName() + "@" + "passwordReset";
	}

	private String userName() {
		return (String) getFormContext().getField(USERNAME_FIELD).getValue();
	}

	private void displayMailSent() {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		MessageBox.confirm(context,
			MessageType.CONFIRM,
			I18NConstants.PASSWORD_RESET_MAIL_SENT_MESSAGE,
			MessageBox.button(ButtonType.OK, Command.DO_NOTHING));
	}

	/**
	 * Default command model to open a {@link ForgotPasswordDialog}.
	 */
	public static class Opener extends AbstractCommandModel {

		/**
		 * Creates a new {@link Opener}.
		 */
		public Opener() {
			setLabel(Resources.getInstance().getString(I18NConstants.RESET_PASSWORD_COMMAND));
			setImage(Icons.RESET_PASSWORD);
			if (!MailSenderService.isConfigured()) {
				setVisible(false);
			}
		}

		@Override
		protected HandlerResult internalExecuteCommand(DisplayContext context) {
			return new ForgotPasswordDialog().open(context);
		}

	}

}

