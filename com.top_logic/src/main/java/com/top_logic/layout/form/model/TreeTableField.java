/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.tree.TreeTableData;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TreeTableModel;

/**
 * {@link TableField} to display a {@link TableModel} retrieved from a
 * {@link AbstractTreeTableModel tree table model}.
 * 
 * <p>
 * The {@link TableField#getTableModel() table model} can not be set direct. It is set implicit when
 * the corresponding tree is set.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeTableField extends TableField implements TreeTableData {

	/**
	 * Creates a new TreeTableField.
	 * 
	 * @param name
	 *        See {@link TableField#TableField(String, Mapping, boolean)}.
	 * @param configNameMapping
	 *        See {@link TableField#TableField(String, Mapping, boolean)}.
	 */
	protected TreeTableField(String name, Mapping<FormMember, String> configNameMapping) {
		super(name, configNameMapping, false);
	}

	/**
	 * Creates a new TreeTableField.
	 * 
	 * @param name
	 *        See {@link TableField#TableField(String, ConfigKey, boolean)}.
	 * @param configKey
	 *        See {@link TableField#TableField(String, ConfigKey, boolean)}.
	 */
	protected TreeTableField(String name, ConfigKey configKey) {
		super(name, configKey, false);
	}

	/**
	 * Sets the {@link AbstractTreeTableModel} on which the table bases.
	 */
	@Override
	public void setTree(AbstractTreeTableModel<?> treeModel) {
		internalSetTableModel(treeModel.getTable());
		treeModel.setViewModel(this);
	}

	/**
	 * The underlying tree table model.
	 */
	@Override
	public AbstractTreeTableModel<?> getTree() {
		TreeTableModel table = (TreeTableModel) getTableModel();
		return (AbstractTreeTableModel<?>) table.getTreeModel();
	}

	/**
	 * This method must not be used.
	 * 
	 * <p>
	 * The table model is set implicit by setting the corresponding tree model.
	 * </p>
	 * 
	 * @see com.top_logic.layout.form.model.TableField#setTableModel(com.top_logic.layout.table.TableModel)
	 * 
	 * @throws UnsupportedOperationException
	 *         The table model must be set by setting the corresponding
	 *         {@link AbstractTreeTableModel}.
	 * 
	 * @see TreeTableField#setTree(AbstractTreeTableModel)
	 */
	@Override
	public void setTableModel(TableModel tableModel) {
		throw new UnsupportedOperationException("The table model of this " + TreeTableField.class.getName() + " '"
			+ this + "' must not be set direct. Set the corresponding tree model.");
	}

	/**
	 * Internal method to set the {@link TableModel}.
	 * 
	 * <p>
	 * This model must be called as the table model is not set from outside.
	 * </p>
	 */
	protected void internalSetTableModel(TableModel tableModel) {
		super.setTableModel(tableModel);
	}

}

