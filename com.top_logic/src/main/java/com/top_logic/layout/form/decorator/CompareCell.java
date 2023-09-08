/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.layout.table.CellAdapter;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link CellAdapter} for compare table.
 * 
 * <p>
 * The {@link CompareCell} is an adapter for {@link Cell}s, which have a {@link CompareRowObject} as
 * {@link Cell#getRowObject()} and a {@link CompareInfo} as {@link Cell#getValue()}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareCell extends CellAdapter {

	/**
	 * Enumeration, whether old or new value shall be used.
	 */
	public static enum ValueType {
		/**
		 * Use heuristic to get base or change value.
		 */
		AUTO,

		/**
		 * Always use base value.
		 */
		BASE,

		/**
		 * Always use change value.
		 */
		CHANGE
	}

	private final boolean _treeTable;

	private final Cell _implementation;

	private ValueType _valueType;

	/**
	 * Creates a new {@link CompareCell}.
	 * 
	 * @param implementation
	 *        Wrapped {@link Cell}.
	 * @param treeTable
	 *        Whether the table is a tree table, i.e. the {@link Cell} has a {@link TLTreeNode} as
	 *        {@link Cell#getRowObject()}.
	 */
	public CompareCell(Cell implementation, boolean treeTable) {
		_implementation = implementation;
		_treeTable = treeTable;
		_valueType = ValueType.AUTO;
	}

	@Override
	public Object getRowObject() {
		CompareRowObject compareRow = compareRow();
		if (useBaseValue()) {
			return compareRow.baseValue();
		} else {
			return compareRow.changeValue();
		}
	}

	/**
	 * Returns the {@link CompareRowObject} which is the {@link Cell#getRowObject() row object} of
	 * the implementation cell.
	 */
	public CompareRowObject compareRow() {
		Object rowObject = impl().getRowObject();
		CompareRowObject compareRow;
		if (_treeTable) {
			compareRow = (CompareRowObject) ((TLTreeNode<?>) rowObject).getBusinessObject();
		} else {
			compareRow = (CompareRowObject) rowObject;
		}
		return compareRow;
	}

	@Override
	public Object getValue() {
		CompareInfo compareValue = compareValue();
		if (useBaseValue()) {
			return compareValue.getBaseValue();
		} else {
			return compareValue.getChangeValue();
		}
	}

	/**
	 * which value shall be delivered when calling {@link #getValue()}.
	 */
	public ValueType getValueType() {
		return _valueType;
	}

	/**
	 * @see #getValueType()
	 */
	public void setValueType(ValueType valueType) {
		_valueType = valueType;
	}

	private boolean useBaseValue() {
		switch (getValueType()) {
			case BASE: {
				return true;
			}

			case CHANGE: {
				return false;
			}

			case AUTO:
			default: {
				return compareValue().getChangeInfo() == ChangeInfo.REMOVED;
			}
		}
	}

	/**
	 * Returns the {@link CompareInfo} which is the {@link Cell#getValue() value} of the
	 * implementation cell.
	 */
	public CompareInfo compareValue() {
		return (CompareInfo) impl().getValue();
	}

	@Override
	protected Cell impl() {
		return _implementation;
	}

}
