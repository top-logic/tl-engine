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
import com.top_logic.base.security.util.Password;
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
 * Dialog to check the invitation token.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CheckInvitationToken extends AbstractTemplateDialog {

	private static final String TOKEN_FIELD = "token";

	private final Invitation _invitation;

	private Command _continuation;

	private InvitationModule _serviceModule = InvitationModule.getInstance();

	private LoginFailuresModule<?> _loginFailures = LoginFailuresModule.Module.INSTANCE.getImplementationInstance();

	/**
	 * Creates a new {@link CheckInvitationToken} with default title and dimensions.
	 * 
	 * @param continuation
	 *        Command to execute when the token check was successful.
	 */
	public CheckInvitationToken(Invitation invitation, Command continuation) {
		this(invitation, continuation, I18NConstants.CHECK_INVITATION_TOKEN_TITLE,
			DisplayDimension.dim(450, DisplayUnit.PIXEL),
			DisplayDimension.dim(300, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link CheckInvitationToken}.
	 * 
	 * @param continuation
	 *        Command to execute when the token check was successful.
	 */
	public CheckInvitationToken(Invitation invitation, Command continuation, ResKey dialogTitle, DisplayDimension width,
			DisplayDimension height) {
		super(dialogTitle, width, height);
		_invitation = invitation;
		_continuation = continuation;
	}

	/**
	 * Creates a new {@link CheckInvitationToken}.
	 * 
	 * @param continuation
	 *        Command to execute when the token check was successful.
	 */
	public CheckInvitationToken(Invitation invitation, Command continuation, DialogModel dialogModel) {
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
				resource(I18NConstants.MESSAGE_TOKEN_SENT__MAIL.fill(_invitation.getEmail()))),
			fieldBox(TOKEN_FIELD));
	}

	@Override
	protected void fillFormContext(FormContext context) {
		StringField token = FormFactory.newStringField(TOKEN_FIELD);
		token.setLabel(I18NConstants.TOKEN_FIELD_LABEL);
		token.setMandatory(true);

		context.addMember(token);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.OK,
			checkContextCommand()
			.andThen(this::checkToken)
			.andThen(getDiscardClosure())
			.andThen(_continuation)));

		buttons.add(MessageBox.button(I18NConstants.REQUEST_TOKEN,
			this::updateTokenCommand));
	}

	private HandlerResult checkToken(@SuppressWarnings("unused") DisplayContext ctx) {
		String failureKey = failureKey();
		LoginFailure failure = _loginFailures.getFailureFor(failureKey);
		if (failure != null && !failure.allowRetry()) {
			return HandlerResult.error(I18NConstants.ERROR_TOKEN_MISMATCH_TOO_MANY_TIMES);
		}
		if (isTokenExpired()) {
			return HandlerResult.error(I18NConstants.ERROR_TOKEN_EXPIRED);
		}
		if (!_invitation.getToken().decrypt().equals(getFormContext().getField(TOKEN_FIELD).getValue())) {
			_loginFailures.notifyLoginFailed(failureKey);
			return HandlerResult.error(I18NConstants.ERROR_TOKEN_MISMATCH);
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
		if (_invitation.getToken() == null) {
			updateToken();
		} else if (isTokenExpired()) {
			updateToken();
		}

		return super.open(windowScope);
	}

	private boolean isTokenExpired() {
		return now() > _invitation.getTokenCreation() + _serviceModule.getConfig().getTokenValidity();
	}

	private static long now() {
		return System.currentTimeMillis();
	}

	private long tokenCreationAllowedAfter() {
		long tokenCreation = _invitation.getTokenCreation();
		int tokenCounter = _invitation.getTokenCounter();
		return tokenCreation + tokenCounter * _serviceModule.getConfig().getTokenResendDelay();
	}

	private HandlerResult updateTokenCommand(@SuppressWarnings("unused") DisplayContext ctx) {
		long waitTime = (tokenCreationAllowedAfter() - now()) / 1000;
		if (waitTime > 0) {
			HandlerResult warn = HandlerResult.error(I18NConstants.REQUEST_TOKEN_NOT_ALLOWED__TIMEOUT.fill(waitTime));
			warn.setErrorSeverity(ErrorSeverity.WARNING);
			return warn;
		}
		updateToken();
		return HandlerResult.DEFAULT_RESULT;
	}

	private void updateToken() {
		String newTokenString = newTokenString();
		try (Transaction tx = kb().beginTransaction(I18NConstants.UPDATED_INVITATION_TOKEN)) {
			_invitation.setToken(Password.fromPlainText(newTokenString));
			_invitation.setTokenCreation(now());
			_invitation.setTokenCounter(_invitation.getTokenCounter() + 1);
			tx.commit();
		}
		/* New token was created, reset failures. */
		_loginFailures.notifyLoginSuccessed(failureKey());

		sendTokenMail(newTokenString);
	}

	private void sendTokenMail(String newTokenString) {
		String applicationName = Version.getApplicationName();
		_serviceModule.getVerificationMail().execute(_invitation, applicationName, newTokenString);
	}

	private String newTokenString() {
		return String.valueOf(SecureRandomService.getInstance().getRandom().nextInt(100_000, 1_000_000));
	}

	private String failureKey() {
		return _invitation.getId() + "@" + "token";
	}
}

