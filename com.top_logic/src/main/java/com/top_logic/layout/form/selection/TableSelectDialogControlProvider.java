/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.ArrayList;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.buttonbar.ButtonBarControl;
import com.top_logic.layout.buttonbar.ButtonBarFactory;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;

/**
 * {@link ControlProvider} used for table based popup dialogs.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableSelectDialogControlProvider extends DefaultFormFieldControlProvider {

	private final ButtonBarControl _buttonBarControl;

	/**
	 * Create a new {@link TableSelectDialogControlProvider}
	 */
	public TableSelectDialogControlProvider(TableSelectorContext context) {
		_buttonBarControl = createButtonBarControl(context);
	}

	private ButtonBarControl createButtonBarControl(TableSelectorContext context) {
		ArrayList<FormMember> buttonList = CollectionUtil.toList(context.getButtons().getDescendants());
		return ButtonBarFactory
			.createButtonBar(CollectionUtil.dynamicCastView(CommandModel.class, buttonList));
	}

	@Override
	public Control visitFormGroup(FormGroup member, Void arg) {
		if (member.getName().equals(SelectorContext.BUTTONS)) {
			return _buttonBarControl;
		}
		return super.visitFormGroup(member, arg);
	}
}
