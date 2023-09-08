/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.layout.Control;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * A {@link ControlProvider} used for tree dialogs, which allow single selections
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TreeSingleSelectDialogProvider extends TreeSelectDialogControlProvider {

	private final TextInputControl patternFieldControl;

	/**
	 * Create a new {@link TreeSingleSelectDialogProvider}.
	 */
	public TreeSingleSelectDialogProvider(TreeSelectorContext context) {
		super(context);
		patternFieldControl = createPatternFieldControl(context);
	}

	@Override
	protected Command getDoubleClickCommand(TreeSelectorContext context) {
		return context.getAcceptCommand();
	}

	private TextInputControl createPatternFieldControl(TreeSelectorContext context) {
		StringField patternField = (StringField) context.getMember(TreeSelectorContext.PATTERN_FIELD_NAME);
		TextInputControl result = new TextInputControl(patternField);
		// Width must be smaller than 100%, due to defined border of text input control
		result.setInputStyle("width:97%;float:left;");
//		result.setOnKeyUp(onKeyUp);
		return result;
	}

	@Override
	public Control visitStringField(StringField member, Void arg) {
		if (member.getName().equals(SelectorContext.PATTERN_FIELD_NAME)) {
			return patternFieldControl;
		}
		return super.visitStringField(member, arg);
	}
}
