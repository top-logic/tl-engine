/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.compare.I18NConstants;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;

/**
 * Creates a {@link TableFieldDialog} based on a tree table.
 * 
 * @see TreeTableDialog
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TreeTableDialog extends TableFieldDialog {

	/**
	 * Creates a new {@link TreeTableDialog}.
	 */
	public TreeTableDialog(DisplayDimension width, DisplayDimension height) {
		super(I18NConstants.COMPARE_DIALOG, width, height);
	}

	@Override
	protected TableField createTableField(String tableFieldName) {
		return FormFactory.newTreeTableField(tableFieldName, configKey(), createTreeModel());
	}

	/**
	 * Creates the {@link AbstractTreeTableModel} for the displayed table.
	 */
	protected abstract AbstractTreeTableModel<?> createTreeModel();

	/**
	 * The {@link ConfigKey key} used to store modifications on the table view in the personal
	 * configuration.
	 * 
	 * @return Not <code>null</code>. {@link ConfigKey#none()} when no personal adaption should be
	 *         stored.
	 */
	protected abstract ConfigKey configKey();

}

