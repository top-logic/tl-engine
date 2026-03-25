/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;
import java.util.function.Function;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.base.security.util.Password;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.Version;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.configuration.LogoutView;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.mig.html.layout.LoginHooks;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Dialog to display when a user has forgot its password.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResetMFADialog extends AbstractVerificationCodeDialog {

	private Person _account;

	/**
	 * Creates a new {@link ResetMFADialog} with default values.
	 */
	public ResetMFADialog(Person account) {
		this(account, I18NConstants.RESET_MFA_DIALOG_TITLE,
			DisplayDimension.dim(500, DisplayUnit.PIXEL),
			DisplayDimension.dim(350, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link ResetMFADialog}.
	 */
	public ResetMFADialog(Person account, ResKey dialogTitle, DisplayDimension width, DisplayDimension height) {
		super(dialogTitle, width, height);
		_account = account;
	}

	/**
	 * Creates a new {@link ResetMFADialog}.
	 */
	public ResetMFADialog(Person account, DialogModel dialogModel) {
		super(dialogModel);
		_account = account;
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(
			div(resource(I18NConstants.RESET_MFA_MESSAGE)),
			fieldBox(CODE_FIELD));

	}

	@Override
	protected void fillFormContext(FormContext context) {
		context.addMember(createCodeField());
	}


	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.OK,
			checkContextCommand()
				.andThen(ctx -> checkCode(this::failureKey))
				.andThen(this::resetMFA)));

		buttons.add(MessageBox.button(I18NConstants.REQUEST_CODE,
			ctx -> updateCode(this::handleNewCode, this::failureKey)));
	}

	private String failureKey() {
		return _account.getUser() + "@" + "resetMFA";
	}

	private void handleNewCode(String code) {
		sendCodeMail(code);
		InfoService.showInfo(I18NConstants.MESSAGE_VERIFICATION_CODE_SENT);
	}

	private void sendCodeMail(String code) {
		String email = email(_account);
		String applicationName = Version.getApplicationName();
		InvitationModule.getInstance().getResetMFAMail().execute(email, applicationName, code);
	}

	static String email(Person p) {
		return p.getUser().getEMail();
	}

	private HandlerResult resetMFA(DisplayContext context) {
		try (Transaction tx = _account.tKnowledgeBase().beginTransaction(I18NConstants.RESET_MFA_CODE)) {
			_account.setMFASecret(null);
			tx.commit();
		}
		DialogWindowControl dialog =
			MessageBox.newBuilder(MessageType.INFO)
			.message(I18NConstants.RESET_MFA_SUCCESS_MESSAGE)
			.buttons(MessageBox.button(ButtonType.OK)).toDialog();
		LoginHooks.runOnClose(dialog.getDialogModel(), ctx -> {
			LogoutView.logout(ctx.getWindowScope());
			return HandlerResult.DEFAULT_RESULT;
		});
		context.getWindowScope().openDialog(dialog);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Default command model to open a {@link ResetMFADialog}.
	 */
	public static class Opener implements Function<Person, CommandModel> {

		@Override
		public CommandModel apply(Person p) {
			CommandModel command = new AbstractCommandModel() {

				@Override
				protected HandlerResult internalExecuteCommand(DisplayContext context) {
					return new ResetMFADialog(p).open(context);
				}

			};
			command.setLabel(Resources.getInstance().getString(I18NConstants.RESET_MFA_COMMAND));
			command.setImage(Icons.RESET_MFA);
			setExecutability(command, p);
			return command;
		}

		private void setExecutability(CommandModel command, Person p) {
			if (!MailSenderService.isConfigured()) {
				// No mails can be send
				command.setVisible(false);
				return;
			}
			Password mfaSecret = p.getMFASecret();
			if (mfaSecret == null) {
				// happens, for example, during setup process for multi-factor-authentication.
				command.setVisible(false);
				return;
			}
			if (email(p) == null) {
				// Can not send email.
				command.setVisible(false);
				return;
			}
		}

	}

}

