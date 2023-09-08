/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.NullList;

/**
 * Cached version of {@link ObjectTableModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CachedObjectTableModel extends ObjectTableModel {

	/**
	 * Replacement for <code>null</code> values in the cache.
	 * 
	 * <p>
	 * This allows to distinguish entries in the cache, that have not been
	 * looked up in the source (encoded as <code>null</code>) and
	 * <code>null</code> values provided by the source (encoded as
	 * {@link #NULL}).
	 * </p>
	 */
	private static final Object NULL = new NamedConstant("CachedObjectTableModel.NULL");

	/**
	 * Array of cached rows.
	 * 
	 * <p>
	 * A cached row is represented as object array with fixed length of
	 * {@link #getColumnCount()}.
	 * </p>
	 * 
	 * <p>
	 * The row cache is sparse in the sense that it may contain
	 * <code>null</code> values for rows of the source that are not (yet) in
	 * the cache.
	 * </p>
	 */
	/*package protected*/ final ArrayList rowCache = new ArrayList();

	private boolean attached;

	private final TableModelListener cacheUpdateListener = new TableModelListener() {
		@Override
		public void handleTableModelEvent(TableModelEvent event) {
			int lastRow = event.getLastRow();
			int firstRow = event.getFirstRow();
			int cacheLength = rowCache.size();
			switch (event.getType()) {
			case TableModelEvent.INSERT: {
				if (firstRow < cacheLength) {
					if (lastRow > firstRow) {
						// More than one row was inserted.
						int insertionCount = lastRow - firstRow + 1;
						rowCache.addAll(firstRow, new NullList(insertionCount));
					} else {
						rowCache.add(firstRow, null);
					}
				}
				break;
			}

			case TableModelEvent.DELETE: {
				if (lastRow + 1 < cacheLength) {
					// Deletion happened completely within the cached range.
					if (lastRow > firstRow) {
						// More than one row was removed.
						int cnt = cacheLength - (lastRow + 1);

						// Copy rows after the removed section.
						for (int n = 0; n < cnt; n++) {
							rowCache.set(firstRow + n, rowCache.get(lastRow + 1
									+ n));
						}

						// Drop rows at the end of the cache.
						for (int n = cacheLength - 1, stop = cacheLength - cnt; n >= stop; n++) {
							rowCache.remove(n);
						}
					} else {
						rowCache.remove(firstRow);
					}
				} else if (firstRow == 0) {
					rowCache.clear();
				} else if (firstRow < cacheLength) {
					// Deletion started in the cached range, but exceeds the
					// cached range at the right.
					for (int n = cacheLength - 1; n >= firstRow; n++) {
						rowCache.remove(n);
					}
				}
				break;
			}

			case TableModelEvent.UPDATE: {
				// Drop affected cached rows.
				for (int n = firstRow, end = Math.min(cacheLength, lastRow + 1); n < end; n++) {
					rowCache.set(n, null);
				}
				break;
			}
			
			case TableModelEvent.INVALIDATE: {
				rowCache.clear();
				rowCache.addAll(new NullList(getRowCount()));
				break;
			}
			}
		}
	};

	public CachedObjectTableModel(String[] columnNames, TableConfiguration aCDM, List rows) {
		super(columnNames, aCDM, rows);
	}

	public CachedObjectTableModel(String[] columnNames, TableConfiguration aCDM, List rows, boolean priorityTable) {
		super(columnNames, aCDM, rows, priorityTable);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// Read-trough in unattached state.
		if (!attached) {
			return super.getValueAt(rowIndex, columnIndex);
		}

		Object[] cachedRow;
		if (rowIndex < rowCache.size()) {
			cachedRow = (Object[]) rowCache.get(rowIndex);

			if (cachedRow == null) {
				cachedRow = new Object[getColumnCount()];
				rowCache.set(rowIndex, cachedRow);
			}
		} else {
			cachedRow = new Object[getColumnCount()];
			for (int n = 0, cnt = (rowIndex + 1) - rowCache.size() - 1; n < cnt; n++) {
				rowCache.add(null);
			}
			rowCache.add(cachedRow);
		}

		Object cellValue = cachedRow[columnIndex];
		if (cellValue == null) {
			cellValue = super.getValueAt(rowIndex, columnIndex);
			if (cellValue == null) {
				cachedRow[columnIndex] = NULL;
			} else {
				cachedRow[columnIndex] = cellValue;
			}
		}

		if (cellValue == NULL) {
			return null;
		} else {
			return cellValue;
		}
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		// Write-trough in unattached state.
		if (!attached) {
			super.setValueAt(value, rowIndex, columnIndex);
			return;
		}

		// Write back, only if there is already a cache row in this row cache.
		if (rowIndex < rowCache.size()) {
			Object[] cachedRow = (Object[]) rowCache.get(rowIndex);
			if (cachedRow != null) {
				if (value == null) {
					cachedRow[columnIndex] = NULL;
				} else {
					cachedRow[columnIndex] = value;
				}
			}
		}

		// Write through.
		super.setValueAt(value, rowIndex, columnIndex);
	}

	public boolean isAttached() {
		return attached;
	}

	/**
	 * The model starts to cache itself, if a listener will be registered.
	 * 
	 * @see com.top_logic.layout.table.model.AbstractTableModel#addTableModelListener(com.top_logic.layout.table.model.TableModelListener)
	 */
	@Override
	public void addTableModelListener(TableModelListener listener) {
		if (!isAttached()) {
			super.addTableModelListener(cacheUpdateListener);
			attached = true;
		}
		super.addTableModelListener(listener);
	}

	/**
	 * If the last listener is deregistered the cache will be deleted.
	 * 
	 * @see com.top_logic.layout.table.model.AbstractTableModel#removeTableModelListener(com.top_logic.layout.table.model.TableModelListener)
	 */
	@Override
	public boolean removeTableModelListener(TableModelListener listener) {
		boolean result = super.removeTableModelListener(listener);
		if (result && hasTableModelListeners()) {
			// The complexity of detaching relies on the fact that the internal
			// "attach-listener" and the external "cache-listeners" are
			// registered at the same object.
			super.removeTableModelListener(cacheUpdateListener);
			if (!hasTableModelListeners()) {
				// No external listeners are registered so the cache will be
				// deleted
				attached = false;
				rowCache.clear();
			} else {
				// There is still an external listener.
				super.addTableModelListener(cacheUpdateListener);
			}
		} /* else {
			This case occurs if someone tries to remove a listener in an
			unattached state.
		} */
		return result;
	}

}
