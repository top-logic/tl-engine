/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import org.w3c.dom.Document;

import com.top_logic.basic.col.Provider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;

/**
 * {@link SelectDialogBase} which provides dialogs for table based selection.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableSelectDialog extends SelectDialogBase {

	private static final ResPrefix I18N_CONSTANT = I18NConstants.TREE_SELECT_DIALOG;

	/**
	 * Create a new {@link TableSelectDialog}
	 */
	public TableSelectDialog(SelectField targetSelectField, SelectDialogConfig config) {
		super(targetSelectField, config);
	}

	@Override
	protected LayoutControl createContentView(DialogWindowControl dialog) {
		TableSelectorContext context = createSelectorContext(dialog);
		FormGroupControl content =
			new FormGroupControl(context, new TableSelectDialogControlProvider(context), getTemplate(),
				I18N_CONSTANT);
		return new LayoutControlAdapter(content);
	}

	/**
	 * Creates the actual selector {@link FormContext}.
	 * 
	 * @param dialog
	 *        Dialog in which the context is displayed.
	 */
	protected TableSelectorContext createSelectorContext(DialogWindowControl dialog) {
		Command closeAction = dialog.getDialogModel().getCloseAction();
		TableSelectorContext context = new TableSelectorContext(getTargetField(), closeAction,
			getConfig().getOptionsPerPage(), getConfig().getInitialTreeExpansionDepth());
		dialog.getDialogModel().setDefaultCommand(context.getApplyCommand());
		return context;
	}

	@Override
	protected Provider<Document> getTemplateProvider() {
		return new TableSelectTemplateBuilder(getConfig());
	}

}
