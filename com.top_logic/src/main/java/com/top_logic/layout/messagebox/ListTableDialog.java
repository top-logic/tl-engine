/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableModel;

/**
 * Creates a {@link TableFieldDialog} where the table is not a tree table.
 * 
 * @see TreeTableDialog
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ListTableDialog extends TableFieldDialog {

	/**
	 * Creates a new {@link ListTableDialog}.
	 */
	public ListTableDialog(ResPrefix resourcePrefix, DisplayDimension width, DisplayDimension height) {
		super(resourcePrefix, width, height);
	}

	@Override
	protected TableField createTableField(String tableFieldName) {
		TableField tableField = FormFactory.newTableField(tableFieldName, configKey());
		tableField.setTableModel(createTableModel());
		return tableField;
	}

	/**
	 * Creates the displayed {@link TableModel}.
	 */
	protected abstract TableModel createTableModel();

	/**
	 * The {@link ConfigKey key} used to store modifications on the table view in the personal
	 * configuration.
	 * 
	 * @return Not <code>null</code>. {@link ConfigKey#none()} when no personal adaption should be
	 *         stored.
	 */
	protected abstract ConfigKey configKey();

}

