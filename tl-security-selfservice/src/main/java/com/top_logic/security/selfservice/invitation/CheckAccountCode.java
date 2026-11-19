/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Dialog in which a person without account enters the verification code mailed to the address of
 * an {@link AccountRequest}.
 *
 * <p>
 * A correct code replaces the dialog with the {@link CreateAccountDialog} of the request. The
 * button requesting a code lets the request send a new one.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CheckAccountCode extends AbstractVerificationCodeDialog {

	private final AccountRequest _request;

	/**
	 * Creates a new {@link CheckAccountCode} with default title and dimensions.
	 *
	 * @param request
	 *        The request the code proves the address of.
	 */
	public CheckAccountCode(AccountRequest request) {
		this(request, I18NConstants.CHECK_INVITATION_CODE_TITLE,
			DisplayDimension.dim(450, DisplayUnit.PIXEL),
			DisplayDimension.dim(300, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link CheckAccountCode}.
	 *
	 * @param request
	 *        The request the code proves the address of.
	 */
	public CheckAccountCode(AccountRequest request, ResKey dialogTitle, DisplayDimension width,
			DisplayDimension height) {
		super(dialogTitle, width, height);
		_request = request;
	}

	/**
	 * Creates a new {@link CheckAccountCode}.
	 *
	 * @param request
	 *        The request the code proves the address of.
	 */
	public CheckAccountCode(AccountRequest request, DialogModel dialogModel) {
		super(dialogModel);
		_request = request;
	}

	/**
	 * Creates the dialog for a code that has been sent already.
	 *
	 * @param request
	 *        The request the code proves the address of.
	 * @param code
	 *        The code that was mailed.
	 */
	public static CheckAccountCode forCode(AccountRequest request, VerificationCode code) {
		CheckAccountCode dialog = new CheckAccountCode(request);
		dialog.setCode(code);
		return dialog;
	}

	/**
	 * The request the code proves the address of.
	 */
	public AccountRequest getRequest() {
		return _request;
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(
			_request.getCodeMessage(),
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
				.andThen(ctx -> checkCode(_request::getThrottleKey))
				.andThen(getDiscardClosure())
				.andThen(this::openAccountDialog)));

		buttons.add(MessageBox.button(I18NConstants.REQUEST_CODE,
			ctx -> _request.sendNewCode(ctx, this)));
	}

	private HandlerResult openAccountDialog(DisplayContext context) {
		return new CreateAccountDialog(_request).open(context);
	}

}
