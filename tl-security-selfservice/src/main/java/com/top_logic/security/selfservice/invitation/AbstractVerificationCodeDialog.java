/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.base.accesscontrol.LoginFailure;
import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Superclass for dialogs that handle verification codes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractVerificationCodeDialog extends AbstractTemplateDialog {

	/** Name of the field to enter the verification code. */
	protected static final String CODE_FIELD = "code";

	private LoginFailuresModule<?> _loginFailures = LoginFailuresModule.Module.INSTANCE.getImplementationInstance();

	private String _code;

	private long _codeCreatedAt;

	private int _retryCount;

	/**
	 * Creates a new {@link AbstractVerificationCodeDialog}.
	 */
	public AbstractVerificationCodeDialog(ResKey dialogTitle, DisplayDimension width, DisplayDimension height) {
		super(dialogTitle, width, height);
	}

	/**
	 * Creates a new {@link AbstractVerificationCodeDialog}.
	 */
	public AbstractVerificationCodeDialog(DialogModel dialogModel) {
		super(dialogModel);
	}

	/**
	 * Checks the entered value in {@link #CODE_FIELD} against the stored code.
	 * 
	 * @param failureKey
	 *        Supplier for a key to use in {@link LoginFailuresModule}.
	 */
	protected HandlerResult checkCode(Supplier<String> failureKey) {
		if (_code == null) {
			return HandlerResult.error(I18NConstants.ERROR_NO_VALID_CODE);
		}
		if (isCodeExpired()) {
			return HandlerResult.error(I18NConstants.ERROR_NO_VALID_CODE);
		}
		if (_retryCount >= 3) {
			_code = null;
			return HandlerResult.error(I18NConstants.ERROR_CODE_MISMATCH_TOO_MANY_TIMES);
		}
		if (!_code.equals(getFormContext().getField(CODE_FIELD).getValue())) {
			_retryCount++;
			return HandlerResult.error(I18NConstants.ERROR_CODE_MISMATCH);
		} else {
			_loginFailures.notifyLoginSuccessed(failureKey.get());
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Updates the verification code.
	 * 
	 * @param codeConsumer
	 *        Will be filled with the new code. Supplier for a key to use in
	 *        {@link LoginFailuresModule}.
	 * @param failureKey
	 *        Supplier for a key to use in {@link LoginFailuresModule}.
	 */
	protected HandlerResult updateCode(Consumer<String> codeConsumer, Supplier<String> failureKey) {
		String key = failureKey.get();
		LoginFailure existingFailure = _loginFailures.getFailureFor(key);
		if (existingFailure != null) {
			if (existingFailure.allowRetry()) {
				existingFailure.incFailures();
			} else {
				HandlerResult warn =
					HandlerResult.error(
						I18NConstants.REQUEST_CODE_NOT_ALLOWED__TIMEOUT.fill(existingFailure.retryDelay() / 1000));
				warn.setErrorSeverity(ErrorSeverity.WARNING);
				return warn;
			}
		} else {
			// Fake multiple retries to avoid directly multiple code requests.
			LoginFailure failure = _loginFailures.notifyLoginFailed(key);
			failure.incFailures();
			failure.incFailures();
			failure.incFailures();
		}

		String code = installNewCode();
		codeConsumer.accept(code);
		return HandlerResult.DEFAULT_RESULT;
	}

	private String installNewCode() {
		_code = newCode();
		_codeCreatedAt = now();
		_retryCount = 0;

		return _code;
	}

	private boolean isCodeExpired() {
		return now() > _codeCreatedAt + InvitationModule.getInstance().getConfig().getCodeValidity();
	}

	private static long now() {
		return System.currentTimeMillis();
	}

	/**
	 * Creates the mandatory {@link #CODE_FIELD}.
	 */
	protected StringField createCodeField() {
		StringField code = FormFactory.newStringField(CODE_FIELD);
		code.setLabel(I18NConstants.CODE_FIELD_LABEL);
		code.setMandatory(true);
		return code;
	}

	private String newCode() {
		int verificationCodeSize = InvitationModule.getInstance().getConfig().getVerificationCodeSize();
		long upperBound = (long) Math.pow(10, verificationCodeSize);
		String code = String.valueOf(SecureRandomService.getInstance().getRandom().nextLong(0, upperBound));
		int leadingZeros = verificationCodeSize - code.length();
		if (leadingZeros > 0) {
			StringBuilder b = new StringBuilder(verificationCodeSize);
			for (int i = 0; i < leadingZeros; i++) {
				b.append(0);
			}
			b.append(code);
			code = b.toString();
		}
		return code;
	}

}

