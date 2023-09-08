/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link ListModifySelection} is the command which is called when some
 * selection on the GUI has changed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ListModifySelection implements Command {

	enum ModifyMode {
		ADD, REMOVE, ADD_ALL, REMOVE_ALL, UP, DOWN
	}
	
	private static final Property<Boolean> SELECTION_CHANGED =
		TypedAnnotatable.property(Boolean.class, "SELECTION_CHANGED", Boolean.FALSE);

	private SelectorContext selectorContext;
	private ModifyMode mode;
	private boolean onEnter;

	ListModifySelection(SelectorContext selectorContext, ModifyMode mode) {
		this.selectorContext = selectorContext;
		this.mode = mode;
	}

	public void executeOnEnter(boolean executeOnEnter) {
		this.onEnter = executeOnEnter;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		ListField selectionList = selectorContext.getSelectionList();
		ListField optionList = selectorContext.getOptionList();

		switch (mode) {
			case ADD: {
				boolean clearPattern = false;
				FormField patternField = selectorContext.getField(SelectorContext.PATTERN_FIELD_NAME);
				if (onEnter) {
					if (hasSelectionChanged(selectorContext) && StringServices.isEmpty((String) selectorContext.getPattern().getValue())
							&& optionList.getSelectionList().isEmpty()) {
						/*
						 * Accept changes with empty pattern, empty option
						 * selection, and return.
						 */
						return selectorContext.getAcceptCommand().executeCommand(context);
					}

					/*
					 * Only accept a selection, if its uniquely specified by the
					 * pattern.
					 */
					if (optionList.getSelectionList().size() < 1) {
						return HandlerResult.DEFAULT_RESULT;
					}

					if (patternField.hasError()) {
						return HandlerResult.DEFAULT_RESULT;
					}

					clearPattern = true;
				}

				List delta = optionList.getSelectionList();
				if (!delta.isEmpty()) {
					setSelectionChanged(selectorContext);
					selectorContext.addSelection(delta);
				}

				if (clearPattern) {
					selectorContext.clearPattern();
				}
				break;
			}
			case REMOVE: {
				List delta = selectionList.getSelectionList();
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
			case UP: {
				selectorContext.moveSelection(-1);
				break;
			}
			case DOWN: {
				selectorContext.moveSelection(1);
				break;
			}
			default: {
				throw new UnreachableAssertion("Unknown mode:" + mode);
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}
	
	private static void setSelectionChanged(SelectorContext ctx) {
		ctx.set(SELECTION_CHANGED, Boolean.TRUE);
	}

	private static boolean hasSelectionChanged(SelectorContext ctx) {
		return ctx.get(SELECTION_CHANGED).booleanValue();
	}

}
