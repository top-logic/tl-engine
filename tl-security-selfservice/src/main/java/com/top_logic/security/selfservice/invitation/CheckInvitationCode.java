/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.Version;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.security.selfservice.model.Invitation;

/**
 * Dialog to check the invitation code.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CheckInvitationCode extends AbstractVerificationCodeDialog {

	private final Invitation _invitation;

	private Command _continuation;

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
				resource(I18NConstants.MESSAGE_REQUEST_VERIFICATION_CODE)),
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
				.andThen(getDiscardClosure())
				.andThen(_continuation)));

		buttons.add(MessageBox.button(I18NConstants.REQUEST_CODE,
			ctx -> updateCode(this::handleNewCode, this::failureKey)));
	}

	private void handleNewCode(String code) {
		sendCodeMail(code);
		InfoService.showInfo(I18NConstants.MESSAGE_VERIFICATION_CODE_SENT);
	}

	private void sendCodeMail(String code) {
		String applicationName = Version.getApplicationName();
		InvitationModule.getInstance().getVerificationMail().execute(_invitation, applicationName, code);
	}

	private String failureKey() {
		return _invitation.getId() + "@" + "code";
	}
}

