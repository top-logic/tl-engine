/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.mail;

import static com.top_logic.common.webfolder.ui.mail.MailConstants.*;
import static com.top_logic.layout.basic.fragments.Fragments.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import java.text.Format;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.format.MailAddressFormat;
import com.top_logic.layout.form.format.StringTokenFormat;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;

/**
 * {@link MailDialog} allows the user to send some attachments as mail.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MailDialog extends AbstractFormPageDialog {

	private static final String SEND_MAIL_COMMAND_SUFFIX = "sendMail";

	private static final String MAIL_HEADER_SUFFIX = "mailHeader";

	private static final String BODY_HEADER_SUFFIX = "body";

	private final List _attachments;

	/**
	 * Creates a {@link MailDialog}.
	 */
	public MailDialog(List attachments, DisplayDimension width, DisplayDimension height) {
		super(I18NConstants.DIALOG, width, height);
		_attachments = attachments;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(createSendCommand());
		addClose(buttons, ButtonType.CANCEL);
	}

	private CommandModel createSendCommand() {
		ResKey labelKey = I18NConstants.DIALOG.key(SEND_MAIL_COMMAND_SUFFIX);
		SendMailCommand sendCommand = new SendMailCommand(this, getDiscardClosure(), I18NConstants.DIALOG);
		CommandModel commandModel = MessageBox.forwardStyleButton(labelKey, sendCommand);
		return commandModel;
	}

	@Override
	protected IconControl createTitleIcon() {
		return IconControl.icon(Icons.MAIL_FORWARD_60);
	}

	@Override
	protected HTMLFragment createBodyContent() {
		return concat(
			fieldset(
				legend(message(I18NConstants.DIALOG.key(MAIL_HEADER_SUFFIX))),
				tag(TABLE, attribute(STYLE_ATTR, "width: 100%"),
					fieldRow(SENDER),
					fieldRow(TO),
					fieldRow(CC),
					fieldRow(BCC),
					fieldRow(SUBJECT))),
			fieldset(
				legend(message(I18NConstants.DIALOG.key(BODY_HEADER_SUFFIX))),
				div(input(CONTENT)),
				div(
					span("label_left", label(ATTACHMENT), text(":"), nbsp()),
					span("content", input(ATTACHMENT)))));
	}

	private HTMLFragment fieldRow(String fieldName) {
		return tr(
			td("label", label(fieldName), text(":")),
			td("content", input(fieldName), nbsp(), errorIcon(fieldName)));
	}

	@Override
	protected ControlProvider getControlProvider() {
		return new DefaultFormFieldControlProvider() {

			@Override
			public Control visitFormField(FormField member, Void arg) {
				String memberName = member.getName();
				if (TO.equals(memberName)) {
					return createTextInputControl(member);
				} else if (CC.equals(memberName)) {
					return createTextInputControl(member);
				} else if (BCC.equals(memberName)) {
					return createTextInputControl(member);
				} else if (SUBJECT.equals(memberName)) {
					return createTextInputControl(member);
				} else if (CONTENT.equals(memberName)) {
					TextInputControl ctrl = createTextInputControl(member);
					ctrl.setRows(12);
					ctrl.setMultiLine(true);
					ctrl.setInputStyle("width: 100%;");
					return ctrl;
				} else {
					return super.visitFormField(member, arg);
				}
			}

			private TextInputControl createTextInputControl(FormField member) {
				TextInputControl ctrl = new TextInputControl(member);
				ctrl.setColumns(80);
				return ctrl;
			}

		};
	}

	@Override
	protected ResPrefix getResourcePrefix() {
		return I18NConstants.DIALOG;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		MailAddressFormat oneMailFormat = new MailAddressFormat();
		StringTokenFormat manyMailsFormat = new StringTokenFormat(oneMailFormat, ",; ", null, true);

		context.addMember(createSenderField(oneMailFormat));
		context.addMember(createAttachmentField());
		context.addMember(FormFactory.newComplexField(TO, manyMailsFormat, false, true, false, GenericMandatoryConstraint.SINGLETON));
		context.addMember(FormFactory.newComplexField(CC, manyMailsFormat, false));
		context.addMember(FormFactory.newComplexField(BCC, manyMailsFormat, false));
		context.addMember(FormFactory.newStringField(SUBJECT, true, false, new StringLengthConstraint(1, 254)));
		context.addMember(FormFactory.newStringField(CONTENT, false, false, new StringLengthConstraint(0, 10000)));
	}

	private FormField createAttachmentField() {
		if (this._attachments != null) {
			return FormFactory.newSelectField(ATTACHMENT, _attachments, false, _attachments, true);
		} else {
			return FormFactory.newStringField(ATTACHMENT, false, false, new StringLengthConstraint(0, 254));
		}
	}

	private FormField createSenderField(Format senderFormat) {
		String sender = PersonManager.getManager().getCurrentPerson().getInternalMail();
		ComplexField result = FormFactory.newComplexField(SENDER, senderFormat, true);
		result.initializeField(sender);
		return result;
	}

}
