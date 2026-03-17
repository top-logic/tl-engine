/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.Version;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.DisplayImageControl;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.mig.html.layout.LoginHooks;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;

/**
 * Dialog to enable multi-factor authentication.
 * 
 * <p>
 * This dialogs displays a QR code to initialize an authenticator app.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EnableMultiFactorAuthenticationDialog extends AbstractTemplateDialog {

	private static final String QR_CODE_FIELD = "qrCode";

	private final Person _account;

	private final Password _mfaSecret;

	private Command _continuation;

	/**
	 * Creates a new {@link EnableMultiFactorAuthenticationDialog}.
	 */
	public EnableMultiFactorAuthenticationDialog(Person account, Command continuation) {
		this(account, continuation, I18NConstants.ENABLE_MFA_AUTHENTICATION,
			DisplayDimension.dim(500, DisplayUnit.PIXEL),
			DisplayDimension.dim(500, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link EnableMultiFactorAuthenticationDialog}.
	 */
	public EnableMultiFactorAuthenticationDialog(Person account, Command continuation, ResKey dialogTitle,
			DisplayDimension width, DisplayDimension height) {
		super(dialogTitle, width, height);
		_account = account;
		_continuation = continuation;
		SecretGenerator secretGenerator = new DefaultSecretGenerator();
		_mfaSecret = Password.fromPlainText(secretGenerator.generate());
	}

	/**
	 * Creates a new {@link EnableMultiFactorAuthenticationDialog}.
	 */
	public EnableMultiFactorAuthenticationDialog(Person account, Command continuation, DialogModel dialogModel) {
		super(dialogModel);
		_account = account;
		_continuation = continuation;
		SecretGenerator secretGenerator = new DefaultSecretGenerator();
		_mfaSecret = Password.fromPlainText(secretGenerator.generate());
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(
			div(resource(I18NConstants.INIT_AUTHENTICATOR_MESSAGE)),
			div(style("text-align:center"), member(QR_CODE_FIELD)));
	}

	@Override
	protected void fillFormContext(FormContext context) {
		DataField qrCodeField = FormFactory.newDataField(QR_CODE_FIELD);
		qrCodeField.initializeField(createQRCode());
		qrCodeField.setImmutable(true);
		qrCodeField.setControlProvider(new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				return new DisplayImageControl((BinaryDataSource) ((FormField) model).getValue());
			}
		});
		context.addMember(qrCodeField);
	}

	private BinaryData createQRCode() {
		QrGenerator generator = new ZxingPngQrGenerator();
		QrData qrData = new QrData.Builder()
			.label(_account.getName())
			.secret(_mfaSecret.decrypt())
			.issuer(Version.getApplicationName())
			.algorithm(HashingAlgorithm.SHA1)
			.digits(6)
			.period(30)
			.build();
		byte[] imageData;
		try {
			imageData = generator.generate(qrData);
		} catch (QrGenerationException ex) {
			throw new TopLogicException(I18NConstants.UNABLE_TO_CREATE_QR_CODE, ex);
		}
		return BinaryDataFactory.createBinaryData(imageData, generator.getImageMimeType());
	}

	@Override
	public Command getApplyClosure() {
		return checkContextCommand()
			.andThen(this::updateMFA);
	}

	private HandlerResult updateMFA(DisplayContext context) {
		Command updatePassword = ctx -> {
			try (Transaction tx = _account.tKnowledgeBase().beginTransaction(I18NConstants.MFA_PASSWORD_UPDATED)) {
				_account.tUpdateByName(Person.MFA_SECRET_ATTR, _mfaSecret);
				tx.commit();
			}
			return HandlerResult.DEFAULT_RESULT;

		};
		Command continuation = updatePassword
			.andThen(getDiscardClosure())
			.andThen(_continuation);
		return new CheckOTPDialog(continuation, _mfaSecret).open(context);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.OK, getApplyClosure()));
	}

	/**
	 * Service method to open a dialog to inform the user that multi-factor authentication is
	 * required for his account.
	 * 
	 * <p>
	 * After confirming the setup process starts.
	 * </p>
	 * 
	 * @param user
	 *        The user for which MFA is required.
	 * @param continuation
	 *        Command that is executed when the user does not confirm the message (just closing the
	 *        dialog) and after MFA was set up successful.
	 * @param afterMFAEnabled
	 *        Command that is executed when the user has set up the MFA process successful. After
	 *        this command is executes, the <code>continuation</code> command is executed.
	 */
	public static HandlerResult informMFARequired(DisplayContext context, Person user, Command continuation,
			Command afterMFAEnabled) {
		Command[] closeMessageCommand = new Command[1];
		Command enableMFAAuthentication = ctx -> {
			Command closeMessageAfterMFAEnabled = afterMFAEnabled.andThen(closeMessageCommand[0]);
			return new EnableMultiFactorAuthenticationDialog(user, closeMessageAfterMFAEnabled).open(ctx);
		};
		DialogWindowControl dialog = MessageBox.newBuilder(MessageType.CONFIRM)
			.message(I18NConstants.MFA_REQUIRED_MESSAGE)
			.buttons(MessageBox.button(ButtonType.OK, enableMFAAuthentication))
			.implicitClose(false)
			.toDialog();
		LoginHooks.runOnClose(dialog.getDialogModel(), continuation);

		closeMessageCommand[0] = dialog.getDialogModel().getCloseAction();

		context.getWindowScope().openDialog(dialog);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Service method to open a dialog to inform the user that multi-factor authentication is
	 * optional for his account and allows the user to start the setup process.
	 * 
	 * @param user
	 *        The user for which MFA is required.
	 * @param continuation
	 *        Command that is executed either when the user does not start the MFA set up process or
	 *        when MFA was set up successful.
	 */
	public static HandlerResult informMFAOptional(DisplayContext context, Person user, Command continuation) {
		/* Strange construction: The continuation must be executed in each case, also when the user
		 * closed the message box by clicking in the background. Therefore it must be wrapped as
		 * close listener at the dialog. Moreover the continuation must *not* be executed when the
		 * OK button is pressed, because first the MFA authentication must be set up. */
		Command[] closeMessageCommand = new Command[1];

		Command cancelCommand = ctx -> closeMessageCommand[0].executeCommand(ctx);
		DialogWindowControl dialog = MessageBox.newBuilder(MessageType.CONFIRM)
			.message(I18NConstants.MFA_OPTIONAL_MESSAGE)
			.buttons(
				MessageBox.button(ButtonType.CANCEL, cancelCommand),
				MessageBox.button(ButtonType.OK, ctx -> {
					return new EnableMultiFactorAuthenticationDialog(user, closeMessageCommand[0]).open(ctx);
				}))
			.implicitClose(false)
			.toDialog();
		LoginHooks.runOnClose(dialog.getDialogModel(), continuation);
		closeMessageCommand[0] = dialog.getDialogModel().getCloseAction();

		context.getWindowScope().openDialog(dialog);
		return HandlerResult.DEFAULT_RESULT;
	}

}

