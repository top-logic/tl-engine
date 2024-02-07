/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBoxContentView;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link ValueVetoListener} which reacts on non programmatic updates and omits setting a value when
 * the constraints of the field is not allowed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConstraintCheckingVetoListener implements ValueVetoListener {

	private HTMLFragment _title;

	private LayoutData _layoutData;

	/**
	 * Creates a new {@link ConstraintCheckingVetoListener}.
	 */
	public ConstraintCheckingVetoListener() {
		_title = ConstantDisplayValue.EMPTY_STRING;
		DisplayDimension width = DisplayDimension.dim(500, DisplayUnit.PIXEL);
		DisplayDimension height = DisplayDimension.dim(200, DisplayUnit.PIXEL);
		_layoutData = DefaultLayoutData.newLayoutData(width, height);
	}

	/**
	 * Sets the title of the dialog which is displayed when some constraint is not fulfilled
	 */
	public void setDialogTitle(HTMLFragment title) {
		_title = title;
	}

	/**
	 * Determines the size of the dialog which is displayed when some constraint is not fulfilled.
	 */
	public void setLayoutData(LayoutData data) {
		_layoutData = data;
	}

	@Override
	public void checkVeto(FormField field, Object newValue) throws VetoException {
		final List<String> errors = getErrors(field, newValue);
		if (!errors.isEmpty()) {
			throw new VetoException() {
				@Override
				public void process(WindowScope window) {
					openDialog(window, errors);
				}
			};
		}
	}

	private List<String> getErrors(FormField field, Object newValue) {
		List<Constraint> constraints = field.getConstraints();
		List<String> errors = new ArrayList<>();
		for (Constraint constraint : constraints) {
			try {
				boolean wasChecked = constraint.check(newValue);
				if (!wasChecked) {
					continue;
				}
			} catch (CheckException ex) {
				errors.add(ex.getLocalizedMessage());
			}
		}
		return errors;
	}

	void openDialog(WindowScope window, List<String> errors) {
		DialogWindowControl dialog = createDialog(errors);
		window.openDialog(dialog);
	}

	private DialogWindowControl createDialog(final List<String> errors) {
		DefaultDialogModel dialogModel = new DefaultDialogModel(_layoutData, _title, DefaultDialogModel.RESIZABLE,
				DefaultDialogModel.CLOSE_BUTTON, DefaultDialogModel.NO_HELP_ID);
		MessageBoxContentView content = new MessageBoxContentView(new HTMLFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				out.writeText(context.getResources().getString(I18NConstants.ERROR_CONSTRAINTS_NOT_FULFILLED));
				out.beginTag(HTMLConstants.UL);
				for (String error: errors) {
					out.beginTag(HTMLConstants.LI);
					out.writeText(error);
					out.endTag(HTMLConstants.LI);
				}
				out.endTag(HTMLConstants.UL);
			}
		});
		CommandModel okButton = MessageBox.button(ButtonType.OK, dialogModel.getCloseAction());
		List<CommandModel> buttons = Collections.singletonList(okButton);
		dialogModel.setDefaultCommand(okButton);
		DialogWindowControl dialog = MessageBox.createDialog(dialogModel, content, buttons);
		return dialog;
	}

}

