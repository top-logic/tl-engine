/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.layout.table.TableModel;

/**
 * An extension of {@link TableModel} that allows actively removing table rows.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface EditableTableModel extends TableModel {

	/**
	 * Removes the given row from this model.
	 * 
	 * @param removedRow
	 *            The row to remove. Must be &gt; or equal to 0 and &lt; {@link #getRowCount()
	 *            number of rows}
	 */
	public void removeRow(int removedRow);

	/**
	 * Check, whether the given row can be removed. Must be &gt; or equal to 0 and &lt; {@link #getRowCount()
	 *            number of rows}
	 */
	public boolean canRemove(int rowNr);

	/**
	 * Moves the row with index <code>from</code> such that after this operation the row is
	 * positioned at index <code>to</code>.
	 * 
	 * <p>
	 * E.g. a call <code>moveRow(i, i)</code> is a no-op, <code>moveRow(i, 0)</code> moves the
	 * <code>i^th</code> row to the top.
	 * </p>
	 * 
	 * @param from
	 *        The index of the moved row.
	 * @param to
	 *        Index of the row after the row was moved. <code>{@link #getRowCount()}-1</code> moves
	 *        to the end of the table.
	 */
	public void moveRow(int from, int to);

	/**
	 * Moves the given row up by 1 row.
	 * 
	 * @param moveUpRow
	 *            The row to move. Must be &gt; or equal to 0 and &lt; {@link #getRowCount() number
	 *            of rows}
	 */
	public void moveRowUp(int moveUpRow);

	/**
	 * Moves the given row down by 1 row.
	 * 
	 * @param moveDownRow
	 *            The row to move. Must be &gt; or equal to 0 and &lt; {@link #getRowCount() number
	 *            of rows}
	 */
	public void moveRowDown(int moveDownRow);

	/**
	 * Moves the given row up to the top.
	 * 
	 * @param moveRowTop
	 *            The row to move. Must be &gt; or equal to 0 and &lt; {@link #getRowCount() number
	 *            of rows}
	 */
	public void moveRowToTop(int moveRowTop);

	/**
	 * Moves the given row down to the bottom.
	 * 
	 * @param moveRowBottom
	 *            The row to move. Must be &gt; or equal to 0 and &lt; {@link #getRowCount() number
	 *            of rows}
	 */
	public void moveRowToBottom(int moveRowBottom);

}
