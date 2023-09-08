/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.io.IOException;

import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultButtonRenderer;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.selection.TreeModifySelection.ModifyMode;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.list.DoubleClickCommand;
import com.top_logic.layout.list.DropAcceptor;
import com.top_logic.layout.list.ListControl;
import com.top_logic.layout.tree.dnd.DragSelectDialog;
import com.top_logic.layout.tree.dnd.DropFromTreeAcceptor;
import com.top_logic.layout.tree.dnd.DropSelectDialog;

/**
 * A {@link ControlProvider} used for tree dialogs, which allow multiple selections
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TreeMultiSelectDialogProvider extends TreeSelectDialogControlProvider {

	private final ListControl selectionFieldControl;

	/**
	 * Create a new {@link TreeMultiSelectDialogProvider}
	 */
	public TreeMultiSelectDialogProvider(TreeSelectorContext context, SelectDialogConfig selectDialogConfig) {
		super(context);

		selectionFieldControl = createSelectionFieldControl(context, selectDialogConfig);
	}

	@Override
	protected Command getDoubleClickCommand(TreeSelectorContext context) {
		return new TreeModifySelection(context, ModifyMode.ADD);
	}

	@SuppressWarnings("hiding")
	private ListControl createSelectionFieldControl(TreeSelectorContext context,
			SelectDialogConfig selectDialogConfig) {
		ListField selectionField = (ListField) context.getMember(TreeSelectorContext.SELECTION_FIELD_NAME);
		ListControl selectionFieldControl = new ListControl(selectionField);
		final TreeModifySelection remove =
			new TreeModifySelection(context, ModifyMode.REMOVE);
		selectionFieldControl.setDblClickAction(new DoubleClickCommand(remove));
		selectionFieldControl.setListRenderer(selectDialogConfig.getDialogListRenderer());

		// connect selection field control with option field control via D&D
		DropAcceptor<Object> comparableAcceptor =
			new DropFromTreeAcceptor(new SelectionDropAccessor(context.getSelectField()));
		if (context.getSelectField().hasCustomOrder()) {
			selectionFieldControl.addDropTarget(selectionFieldControl);
		} else {
			selectionFieldControl.setDropAcceptor(comparableAcceptor);
		}
		DragSelectDialog dragSelectDialog = new DragSelectDialog(getTreeControl());
		dragSelectDialog.addDropTarget(selectionFieldControl);
		getTreeControl().setLegacyTreeDragOperation(dragSelectDialog);

		DropSelectDialog dropSelectDialog = new DropSelectDialog(getTreeControl());
		getTreeControl().setLegacyTreeDropOperation(dropSelectDialog);
		selectionFieldControl.addDropTarget(getTreeControl());

		return selectionFieldControl;
	}

	@Override
	public Control visitCommandField(CommandField member, Void arg) {
		final String theName = member.getName();
		if (theName.equals(TreeSelectorContext.ADD_TO_SELECTION)
			|| theName.equals(TreeSelectorContext.ADD_ALL_TO_SELECTION)
			|| theName.equals(TreeSelectorContext.REMOVE_ALL_FROM_SELECTION)
			|| theName.equals(TreeSelectorContext.REMOVE_FROM_SELECTION)) {
			ButtonControl control = new ButtonControl(member, DefaultButtonRenderer.NO_REASON_INSTANCE);
			member.setShowProgress();
			member.set(ButtonControl.SHOW_PROGRESS_DIV_ID, new AbstractDisplayValue() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					getButtonBarControl().fetchID(context.getExecutionScope().getFrameScope());
					getButtonBarControl().appendProgressDivID(out);
				}
			});
			return control;
		}

		return super.visitCommandField(member, arg);
	}

	@Override
	public Control visitListField(final ListField member, Void arg) {
		if (member.getName().equals(TreeSelectorContext.SELECTION_FIELD_NAME)) {
			return selectionFieldControl;
		}
		return super.visitListField(member, arg);
	}
}
