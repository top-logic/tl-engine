/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Command} to apply the selection of the table to the {@link SelectField} and close the
 * dialog.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableAcceptSelection implements Command {

	private TableSelectorContext _tableSelectorContext;

	private SelectField _targetSelectField;

	TableAcceptSelection(TableSelectorContext tableSelectorContext, SelectField targetSelectField) {
		_tableSelectorContext = tableSelectorContext;
		_targetSelectField = targetSelectField;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		final List<?> newSelection = getSelection();
		try {
			FormFieldInternals.setValue(_targetSelectField, newSelection);
			_tableSelectorContext.getCloseCommand().executeCommand(context);
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {

				@SuppressWarnings({ "synthetic-access", "hiding" })
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					_targetSelectField.setValue(newSelection);
					_tableSelectorContext.getCloseCommand().executeCommand(context);
					return HandlerResult.DEFAULT_RESULT;
				}
			});

			ex.process(context.getWindowScope());
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private List<?> getSelection() {
		if (!_targetSelectField.isOptionsTree()) {
			return _tableSelectorContext.getSelection();
		} else {
			List<?> treeTableSelection = _tableSelectorContext.getSelection();
			List<Object> result = new ArrayList<>();
			for (Object treeTableNode : treeTableSelection) {
				result.add(((DefaultTreeTableNode) treeTableNode).getBusinessObject());
			}
			return result;
		}
	}

}
