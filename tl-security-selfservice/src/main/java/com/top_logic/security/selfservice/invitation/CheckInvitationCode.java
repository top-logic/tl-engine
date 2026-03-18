/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.base.accesscontrol.LoginFailure;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.Version;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Dialog to check the invitation code.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CheckInvitationCode extends AbstractTemplateDialog {

	private static final String CODE_FIELD = "code";

	private final Invitation _invitation;

	private Command _continuation;

	private InvitationModule _serviceModule = InvitationModule.getInstance();

	private LoginFailuresModule<?> _loginFailures = LoginFailuresModule.Module.INSTANCE.getImplementationInstance();

	/**
	 * Creates a new {@link CheckInvitationCode} with default title and dimensions.
	 * 
	 * @param continuation
	 *        Command to execute when the code check was successful.
	 */
	public CheckInvitationCode(Invitation invitation, Command continuation) {
		this(invitation, continuation, I18NConstants.CHECK_INVITATION_CODE_TITLE,
			DisplayDimension.dim(450, DisplayUnit.PIXEL),
			DisplayDimension.dim(300, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link CheckInvitationCode}.
	 * 
	 * @param continuation
	 *        Command to execute when the code check was successful.
	 */
	public CheckInvitationCode(Invitation invitation, Command continuation, ResKey dialogTitle, DisplayDimension width,
			DisplayDimension height) {
		super(dialogTitle, width, height);
		_invitation = invitation;
		_continuation = continuation;
	}

	/**
	 * Creates a new {@link CheckInvitationCode}.
	 * 
	 * @param continuation
	 *        Command to execute when the code check was successful.
	 */
	public CheckInvitationCode(Invitation invitation, Command continuation, DialogModel dialogModel) {
		super(dialogModel);
		_invitation = invitation;
		_continuation = continuation;
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(
			tag(HTMLConstants.PARAGRAPH,
				resource(I18NConstants.MESSAGE_WELCOME_TO_APPLICATION__APPLICATION.fill(Version.getApplicationName()))),
			tag(HTMLConstants.PARAGRAPH,
				resource(I18NConstants.MESSAGE_CODE_SENT__MAIL.fill(_invitation.getEmail()))),
			fieldBox(CODE_FIELD));
	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField code = FormFactory.newStringField(CODE_FIELD);
		code.setLabel(I18NConstants.CODE_FIELD_LABEL);
		code.setMandatory(true);

		context.addMember(code);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.OK,
			checkContextCommand()
			.andThen(this::checkCode)
			.andThen(getDiscardClosure())
			.andThen(_continuation)));

		buttons.add(MessageBox.button(I18NConstants.REQUEST_CODE,
			this::updateCodeCommand));
	}

	private HandlerResult checkCode(@SuppressWarnings("unused") DisplayContext ctx) {
		String failureKey = failureKey();
		LoginFailure failure = _loginFailures.getFailureFor(failureKey);
		if (failure != null && !failure.allowRetry()) {
			return HandlerResult.error(I18NConstants.ERROR_CODE_MISMATCH_TOO_MANY_TIMES);
		}
		if (isCodeExpired()) {
			return HandlerResult.error(I18NConstants.ERROR_CODE_EXPIRED);
		}
		if (!String.valueOf(_invitation.getCode()).equals(getFormContext().getField(CODE_FIELD).getValue())) {
			_loginFailures.notifyLoginFailed(failureKey);
			return HandlerResult.error(I18NConstants.ERROR_CODE_MISMATCH);
		} else {
			_loginFailures.notifyLoginSuccessed(failureKey);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private KnowledgeBase kb() {
		return _invitation.tKnowledgeBase();
	}

	@Override
	public HandlerResult open(WindowScope windowScope) {
		if (isCodeExpired()) {
			updateCode();
		}

		return super.open(windowScope);
	}

	private boolean isCodeExpired() {
		return now() > _invitation.getCodeCreatedAt() + _serviceModule.getConfig().getCodeValidity();
	}

	private static long now() {
		return System.currentTimeMillis();
	}

	private long codeRequestAllowedAfter() {
		long createdAt = _invitation.getCodeCreatedAt();
		int updateCount = _invitation.getCodeUpdateCount();
		return createdAt + updateCount * _serviceModule.getConfig().getCodeResendDelay();
	}

	private HandlerResult updateCodeCommand(@SuppressWarnings("unused") DisplayContext ctx) {
		long waitTime = (codeRequestAllowedAfter() - now()) / 1000;
		if (waitTime > 0) {
			HandlerResult warn = HandlerResult.error(I18NConstants.REQUEST_CODE_NOT_ALLOWED__TIMEOUT.fill(waitTime));
			warn.setErrorSeverity(ErrorSeverity.WARNING);
			return warn;
		}
		updateCode();
		return HandlerResult.DEFAULT_RESULT;
	}

	private void updateCode() {
		int newCode = newCode();
		try (Transaction tx = kb().beginTransaction(I18NConstants.UPDATED_INVITATION_CODE)) {
			_invitation.setCode(newCode);
			_invitation.setCodeCreatedAt(now());
			_invitation.setCodeUpdateCount(_invitation.getCodeUpdateCount() + 1);
			tx.commit();
		}
		/* New code was created, reset failures. */
		_loginFailures.notifyLoginSuccessed(failureKey());

		sendCodeMail(String.valueOf(newCode));
	}

	private void sendCodeMail(String newCode) {
		String applicationName = Version.getApplicationName();
		_serviceModule.getVerificationMail().execute(_invitation, applicationName, newCode);
	}

	private int newCode() {
		return SecureRandomService.getInstance().getRandom().nextInt(100_000, 1_000_000);
	}

	private String failureKey() {
		return _invitation.getId() + "@" + "code";
	}
}

