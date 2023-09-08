/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.mail;

import static com.top_logic.common.webfolder.ui.mail.MailConstants.*;

import java.util.List;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.base.mail.MailHelper.SendMailResult;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Send the mail prepared by the {@link MailDialog}.
 * 
 *          com.top_logic.knowledge.gui.layout.mail.MailSendComponent$SendMailCommandHandler
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SendMailCommand implements Command {

	private final FormHandler _formHandler;

	private final Command _continuation;

	private final ResPrefix _resourcePrefix;

	public SendMailCommand(FormHandler formHandler, Command continuation, ResPrefix resPrefix) {
		_formHandler = formHandler;
		_continuation = continuation;
		_resourcePrefix = resPrefix;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		FormContext formContext = _formHandler.getFormContext();

		if (!formContext.checkAll()) {
			HandlerResult result = new HandlerResult();
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, result);
			return result;
		}

		FormField theDocField = formContext.getField(ATTACHMENT);
		String sender = (String) formContext.getField(SENDER).getValue();
		String subject = (String) formContext.getField(SUBJECT).getValue();
		String content = (String) formContext.getField(CONTENT).getValue();
		if (content == null) {
			content = StringServices.EMPTY_STRING;
		}
		List attachments = (theDocField instanceof SelectField) ? (List) theDocField.getValue() : null;
		List receiver = (List) formContext.getField(TO).getValue();
		List cc = (List) formContext.getField(CC).getValue();
		List bcc = (List) formContext.getField(BCC).getValue();
		SendMailResult sendResult =
			MailHelper.getInstance().sendMail(sender, receiver, cc, bcc, subject, content, attachments,
				CONTENT_TYPE);

		if (!sendResult.isSuccess()) {
			return sendResult;
		} else {
			return _continuation.executeCommand(context);
		}
	}

}