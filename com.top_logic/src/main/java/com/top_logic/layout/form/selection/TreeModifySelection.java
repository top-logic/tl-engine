/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.TreeField;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link TreeModifySelection} is the command which is called when some
 * selection on the GUI has changed.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 * 
 * @see ListModifySelection
 */
class TreeModifySelection implements Command {

	enum ModifyMode {
		ADD, REMOVE, ADD_ALL, REMOVE_ALL
	}
	
	private static final Property<Boolean> SELECTION_CHANGED =
		TypedAnnotatable.property(Boolean.class, "SELECTION_CHANGED", Boolean.FALSE);

	private TreeSelectorContext selectorContext;
	private ModifyMode mode;
	private boolean onEnter;

	TreeModifySelection(TreeSelectorContext selectorContext, ModifyMode mode) {
		this.selectorContext = selectorContext;
		this.mode = mode;
	}

	public void executeOnEnter(boolean executeOnEnter) {
		this.onEnter = executeOnEnter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public HandlerResult executeCommand(DisplayContext context) {
		ListField selectionList = selectorContext.getSelection();
		TreeField optionTree = selectorContext.getOptionTree();

		switch (mode) {
			case ADD: {
				if (onEnter) {
					if (hasSelectionChanged(selectorContext) 
							&& optionTree.getSelectionModel().getSelection().isEmpty()) {
						/*
						 * Accept changes with empty pattern, empty option
						 * selection, and return.
						 */
						return selectorContext.getAcceptCommand().executeCommand(context);
					}

					if (optionTree.getSelectionModel().getSelection().isEmpty()) {
						return HandlerResult.DEFAULT_RESULT;
					}
				}
				
				List<Object> delta = new ArrayList<>(optionTree.getSelectionModel().getSelection());
				if (!delta.isEmpty()) {
					setSelectionChanged(selectorContext);
					selectorContext.addSelection(delta);
				}

				break;
			}
			case REMOVE: {
				List<Object> delta = selectionList.getSelectionList();
				if (!delta.isEmpty()) {
					setSelectionChanged(selectorContext);
					selectorContext.removeSelection(delta);
				}

				break;
			}
			case ADD_ALL: {
				selectorContext.addAllToSelection();
				break;
			}
			case REMOVE_ALL: {
				selectorContext.removeAllFromSelection();
				break;
			}
			default: {
				throw new UnreachableAssertion("Unknown mode:" + mode);
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}
	
	private static void setSelectionChanged(TreeSelectorContext ctx) {
		ctx.set(SELECTION_CHANGED, Boolean.TRUE);
	}

	private static boolean hasSelectionChanged(TreeSelectorContext ctx) {
		return ctx.get(SELECTION_CHANGED).booleanValue();
	}

}
