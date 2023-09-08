/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.List;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.list.model.ListModelUtilities;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link ListAcceptSelection} is the command to apply the selection to the {@link SelectField} and clos the command
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ListAcceptSelection implements Command {
	
	private final boolean onEnter;
	private final SelectorContext ctx;
	private final SelectField selectField;

	ListAcceptSelection(SelectorContext ctx, SelectField selectField, boolean onEnter) {
		this.ctx = ctx;
		this.onEnter = onEnter;
		this.selectField = selectField;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		if (onEnter) {
			if (ctx.getField(SelectorContext.PATTERN_FIELD_NAME).hasError()) {
				return HandlerResult.DEFAULT_RESULT;
			}

			if ((!ctx.isMultiSelect()) && ctx.getOptionList().getSelectionList().size() != 1) {
				// Enter only accepts the selection, if the pattern uniquely matches.
				return HandlerResult.DEFAULT_RESULT;
			}
		}

		if (!ctx.checkAll()) {
			return AbstractApplyCommandHandler.createErrorResult(ctx);
		}

		final List newSelection;
		if (ctx .isMultiSelect()) {
			newSelection = ListModelUtilities.asList(ctx.getSelectionList().getListModel());
		} else {
			newSelection = ctx.getOptionList().getSelectionList();
		}

		try {
			FormFieldInternals.setValue(selectField, newSelection);
			ctx.getCloseCommand().executeCommand(aContext);
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {

				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					selectField.setValue(newSelection);
					ctx.getCloseCommand().executeCommand(context);
					return HandlerResult.DEFAULT_RESULT;
				}
			});

			ex.process(aContext.getWindowScope());
		}

		return HandlerResult.DEFAULT_RESULT;
	}
	
}
