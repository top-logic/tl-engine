/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.ArrayList;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.buttonbar.ButtonBarControl;
import com.top_logic.layout.buttonbar.ButtonBarFactory;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.list.DoubleClickCommand;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeData;

/**
 * A {@link ControlProvider} used for tree dialogs
 * 
 * @author <a href="mailto:stefan.steinert@top-logic.com">Stefan Steinert</a>
 */
abstract class TreeSelectDialogControlProvider extends DefaultFormFieldControlProvider {

	private final ButtonBarControl _buttonBarControl;
	private final TreeControl _treeControl;
	
	public TreeSelectDialogControlProvider(TreeSelectorContext context) {
		_treeControl = createOptionField(context);
		_buttonBarControl = createButtonBarControl(context);
	}

	private TreeControl createOptionField(TreeSelectorContext context) {
		TreeData optionsField = (TreeData) context.getMember(TreeSelectorContext.OPTIONS_FIELD_NAME);
		TreeControl resultControl = new TreeControl(optionsField);
		DoubleClickCommand dblClickCommand = new DoubleClickCommand(getDoubleClickCommand(context));
		dblClickCommand.setIsWaitPaneRequested(true);
		resultControl.setDblClickAction(dblClickCommand);
		return resultControl;
	}

	/**
	 * {@link Command}, that shall be performed, if double click on a certain tree node in
	 *         options tree occurred.
	 */
	protected abstract Command getDoubleClickCommand(TreeSelectorContext context);

	private ButtonBarControl createButtonBarControl(TreeSelectorContext context) {
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

	@Override
	protected Control visitTreeData(FormMember member, TreeData data, Void arg) {
		if (member.getName().equals(TreeSelectorContext.OPTIONS_FIELD_NAME)) {
			return _treeControl;
		}
		return super.visitTreeData(member, data, arg);
	}

	protected final ButtonBarControl getButtonBarControl() {
		return _buttonBarControl;
	}

	protected final TreeControl getTreeControl() {
		return _treeControl;
	}
}
