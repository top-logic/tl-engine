/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.layout.table.TableModel;

/**
 * Implementation of events that are sent in response to changes of a
 * {@link TableModel}.
 * 
 * <p>
 * This interface is modeled in the style of
 * {@link javax.swing.event.TableModelEvent}. However, the Swing version could
 * not be reused, beacuse it has a direct dependency to the Swing interface
 * {@link javax.swing.table.TableModel}, which could not be reused.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableModelEvent {

	/**
	 * @see javax.swing.event.TableModelEvent#INSERT
	 */
	public static final int INSERT = 1;
	
	/**
	 * @see javax.swing.event.TableModelEvent#UPDATE
	 */
	public static final int UPDATE = 2;
	
	/**
	 * @see javax.swing.event.TableModelEvent#DELETE
	 */
	public static final int DELETE = 3;

	/**
	 * The sender has been invalidated.
	 * 
	 * <p>
	 * The number of rows and their contents may have been changed. The
	 * {@link #getFirstRow()} and {@link #getLastRow()} have no meaning in
	 * events of this type.
	 * </p>
	 */
	public static final int INVALIDATE = 4;

	/**
	 * The table filter settings were changed
	 */
	public static final int COLUMN_FILTER_UPDATE = 5;

	/**
	 * The table column sort orders were changed
	 */
	public static final int COLUMN_SORT_UPDATE = 6;

	/**
	 * The table column order were changed
	 */
	public static final int COLUMN_ORDER_UPDATE = 7;

	/**
	 * The {@link TableModel#getColumnNames()} columns have changed.
	 * 
	 * @implNote The {@link TableModelEvent} is an instance of {@link TableModelColumnsEvent}.
	 */
	public static final int COLUMNS_UPDATE = 10;

	private final Object source;
	private final int firstRow;
	private final int lastRow;
	private final int type;

	/**
	 * @see javax.swing.event.TableModelEvent#TableModelEvent(javax.swing.table.TableModel, int, int, int)
	 */
	public TableModelEvent(final TableModel source, final int firstRow, final int lastRow,
			final int type) {
		this.source = source;
		this.firstRow = firstRow;
		this.lastRow = lastRow;
		this.type = type;
	}

	/**
	 * @see javax.swing.event.TableModelEvent#getFirstRow()
	 */
	public int getFirstRow() {
		return firstRow;
	}

	/**
	 * @see javax.swing.event.TableModelEvent#getLastRow()
	 */
	public int getLastRow() {
		return lastRow;
	}

	/**
	 * @see javax.swing.event.TableModelEvent#getSource()
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @see javax.swing.event.TableModelEvent#getType()
	 */
	public int getType() {
		return type;
	}
}
