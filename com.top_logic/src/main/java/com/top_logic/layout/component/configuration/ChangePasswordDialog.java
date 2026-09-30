/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.knowledge.gui.layout.person.ChangePasswordComponent;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Dialog to change the password for a {@link Person}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangePasswordDialog extends AbstractTemplateDialog {

	private Person _person;

	private Command _continuation;

	/**
	 * Creates a new {@link ChangePasswordDialog} with default values.
	 * 
	 * @param person
	 *        The {@link Person} to change password for.
	 * @param continuation
	 *        {@link Command} to execute after successful password change.
	 */
	public ChangePasswordDialog(Person person, Command continuation) {
		this(person, continuation, I18NConstants.CHANGE_PASSWORD,
			DisplayDimension.dim(400, DisplayUnit.PIXEL),
			DisplayDimension.dim(300, DisplayUnit.PIXEL));
	}

	/**
	 * Creates a new {@link ChangePasswordDialog}.
	 * 
	 * @param person
	 *        The {@link Person} to change password for.
	 * @param continuation
	 *        {@link Command} to execute after successful password change.
	 */
	public ChangePasswordDialog(Person person, Command continuation, ResKey dialogTitle, DisplayDimension width,
			DisplayDimension height) {
		super(dialogTitle, width, height);
		_person = person;
		_continuation = continuation;
	}

	/**
	 * Creates a new {@link ChangePasswordDialog}.
	 * 
	 * @param person
	 *        The {@link Person} to change password for.
	 * @param continuation
	 *        {@link Command} to execute after successful password change.
	 */
	public ChangePasswordDialog(Person person, Command continuation, DialogModel dialogModel) {
		super(dialogModel);
		_person = person;
		_continuation = continuation;
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(
			fieldBox(ChangePasswordComponent.NEW_PASSWORD_1),
			fieldBox(ChangePasswordComponent.NEW_PASSWORD_2));
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	protected ResPrefix getResourcePrefix() {
		return com.top_logic.knowledge.gui.layout.person.I18NConstants.CHANGE_PASSWORD_FORM;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		ChangePasswordComponent.addChangePasswordFields(context, _person, false);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(I18NConstants.CHANGE_PASSWORD, getApplyClosure()));
	}

	@Override
	public Command getApplyClosure() {
		return checkContextCommand()
			.andThen(this::changePassword)
			.andThen(getDiscardClosure())
			.andThen(_continuation);
	}

	private HandlerResult changePassword(@SuppressWarnings("unused") DisplayContext context) {
		return ChangePasswordComponent.ApplyPasswordCommand.applyPasswordChange(getFormContext(), _person);
	}

}

