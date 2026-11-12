/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.base.accesscontrol.LoginFailuresModule;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * Superclass for dialogs that handle verification codes.
 *
 * <p>
 * The dialog keeps the {@link VerificationCode} it has sent on the server. A code is accepted for
 * the configured validity and refused after {@link VerificationCode#MAX_MISMATCHES} wrong entries;
 * code requests are throttled per key (see {@link VerificationCode#throttleRequest(String)}).
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractVerificationCodeDialog extends AbstractTemplateDialog {

	/** Name of the field to enter the verification code. */
	protected static final String CODE_FIELD = "code";

	private VerificationCode _code;

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
	 * Checks the entered value in {@link #CODE_FIELD} against the code that was sent.
	 * 
	 * @param failureKey
	 *        Supplier for the key under which code requests are throttled in
	 *        {@link LoginFailuresModule}; a correct code lifts the throttling.
	 */
	protected HandlerResult checkCode(Supplier<String> failureKey) {
		if (_code == null) {
			return HandlerResult.error(I18NConstants.ERROR_NO_VALID_CODE);
		}
		try {
			_code.check((String) getFormContext().getField(CODE_FIELD).getValue());
		} catch (TopLogicException ex) {
			return HandlerResult.error(ex.getErrorKey());
		}
		LoginFailuresModule.getInstance().notifyLoginSuccessed(failureKey.get());
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Creates a new verification code, unless code requests under the given key must wait.
	 * 
	 * @param codeConsumer
	 *        Will be filled with the new code, e.g. to send it by mail.
	 * @param failureKey
	 *        Supplier for the key under which code requests are throttled in
	 *        {@link LoginFailuresModule}.
	 */
	protected HandlerResult updateCode(Consumer<String> codeConsumer, Supplier<String> failureKey) {
		try {
			VerificationCode.throttleRequest(failureKey.get());
		} catch (TopLogicException ex) {
			HandlerResult warn = HandlerResult.error(ex.getErrorKey());
			warn.setErrorSeverity(ErrorSeverity.WARNING);
			return warn;
		}

		VerificationCode code = new VerificationCode();
		setCode(code);
		codeConsumer.accept(code.getCode());
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Sets the code the entered value is checked against, a code that has been sent by someone
	 * else.
	 */
	protected void setCode(VerificationCode code) {
		_code = code;
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

}
