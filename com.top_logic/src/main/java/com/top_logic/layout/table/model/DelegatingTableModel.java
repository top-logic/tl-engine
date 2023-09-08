/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.WrappedModel;
import com.top_logic.layout.table.TableRowFilter;
import com.top_logic.model.export.AccessContext;

/**
 * {@link EditableRowTableModel} delegating to another implementation.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DelegatingTableModel implements EditableRowTableModel, WrappedModel {
    
    private HashSet/*<TableModelListener>*/ listeners;
    
    private EditableRowTableModel inner;

    /** 
     * Creates a {@link DelegatingTableModel}.
     * 
     */
    public DelegatingTableModel(EditableRowTableModel anInner) {
        this.inner = anInner;
        this.inner.addTableModelListener(new InnerTableModelListener());
    }
    
	@Override
	public Header getHeader() {
		return inner.getHeader();
	}

	@Override
	public int getColumnIndex(String columnName) {
		return inner.getColumnIndex(columnName);
	}

	@Override
	public TableConfiguration getTableConfiguration() {
		return inner.getTableConfiguration();
	}

    /**
     * Checks, whether this model is observed by any {@link TableModelListener}s.
     */
    protected final boolean hasTableModelListeners() {
        return listeners != null && !listeners.isEmpty(); 
    }

    @Override
	public void addTableModelListener(TableModelListener listener) {
        assert listener != null : "Will not add a \"null\"-listener.";
        
        if (listeners == null) {
            listeners = new HashSet();
        }
        
        listeners.add(listener);
    }

    @Override
	public boolean removeTableModelListener(TableModelListener listener) {
        if (! hasTableModelListeners()) return false;
        
        return listeners.remove(listener);
    }

    @Override
	public boolean canRemove(int aRowNr) {
        return inner.canRemove(aRowNr);
    }

	@Override
	public void showRowAt(Object rowObject, int beforeRow) {
		inner.showRowAt(rowObject, beforeRow);
	}

	@Override
	public void moveRow(int from, int to) {
		inner.moveRow(from, to);
	}

    @Override
	public void moveRowDown(int aMoveDownRow) {
    	inner.moveRowDown(aMoveDownRow);
    }

    @Override
	public void moveRowUp(int aMoveUpRow) {
        inner.moveRowUp(aMoveUpRow);
    }

    @Override
	public void removeRow(int aRemovedRow) {
        inner.removeRow(aRemovedRow);
    }

	@Override
	public void removeRows(int start, int stop) {
		inner.removeRows(start, stop);
	}

    @Override
	public int getColumnCount() {
        return inner.getColumnCount();
    }

    @Override
	public String getColumnName(int aColumnIndex) {
        return inner.getColumnName(aColumnIndex);
    }

	@Override
	public List<String> getColumnNames() {
		return inner.getColumnNames();
	}

	@Override
	public void setColumns(List<String> newColumns) throws VetoException {
		inner.setColumns(newColumns);
	}

    @Override
	public int getRowCount() {
        return inner.getRowCount();
    }

	@Override
	public boolean isDisplayedEmpty() {
		return inner.isDisplayedEmpty();
	}

    @Override
	public Object getValueAt(int aRowIndex, int aColumnIndex) {
        return inner.getValueAt(aRowIndex, aColumnIndex);
    }

	@Override
	public Object getValueAt(int rowIndex, String columnName) {
		return inner.getValueAt(rowIndex, columnName);
	}

    @Override
	public void setValueAt(Object aValue, int aRowIndex, int aColumnIndex) {
        inner.setValueAt(aValue, aRowIndex, aColumnIndex);
    }
    
	@Override
	public void setValueAt(Object aValue, int rowIndex, String columnName) {
		inner.setValueAt(aValue, rowIndex, columnName);
	}

	@Override
	public Object getValueAt(Object rowObject, String columnName) {
		return inner.getValueAt(rowObject, columnName);
	}

    protected final EditableRowTableModel getInner() {
        return inner;
    }

    @Override
	public void addAllRowObjects(List aNewRows) {
        inner.addAllRowObjects(aNewRows);
    }

    @Override
	public void addRowObject(Object aRowObject) {
        inner.addRowObject(aRowObject);
    }

    @Override
	public boolean containsRowObject(Object aAnObject) {
        return inner.containsRowObject(aAnObject);
    }

	@Override
	public boolean isDisplayed(Object rowObject) {
		return inner.isDisplayed(rowObject);
	}

    @Override
	public Object getRowObject(int aRow) {
        return inner.getRowObject(aRow);
    }

	@Override
	public List getDisplayedRows() {
		return inner.getDisplayedRows();
	}

	@Override
	public Collection getAllRows() {
		return inner.getAllRows();
	}

    @Override
	public int getRowOfObject(Object aRowObject) {
        return inner.getRowOfObject(aRowObject);
    }

	@Override
	public int findNearestDisplayedRow(Object rowObject) {
		return inner.findNearestDisplayedRow(rowObject);
	}

	@Override
	public Collection<Object> getNecessaryRows(Object rowObject) {
		return inner.getNecessaryRows(rowObject);
	}

	@Override
	public void insertRowObject(int aRow, Object aRowObject) {
        inner.insertRowObject(aRow,aRowObject);
    }

    @Override
	public void insertRowObject(int aRow, List aNewRows) {
        inner.insertRowObject(aRow,aNewRows);
    }

    @Override
	public void moveRowToBottom(int aMoveRowToBottom) {
        inner.moveRowToBottom(aMoveRowToBottom);
    }

    @Override
	public void moveRowToTop(int aMoveRowToTop) {
        inner.moveRowToTop(aMoveRowToTop);
    }

    @Override
	public void removeRowObject(Object aRowObject) {
        inner.removeRowObject(aRowObject);
    }
    
    @Override
	public void clear() {
    	inner.clear();
    }

    @Override
	public void setRowObjects(List aNewRowObjects) {
        inner.setRowObjects(aNewRowObjects);
    }

    @Override
	public void updateRows(int aFirstRow, int aLastRow) {
        inner.updateRows(aFirstRow, aLastRow);
    }
    
    /*package protected*/ class InnerTableModelListener implements TableModelListener {
        /**
         * @see com.top_logic.layout.table.model.TableModelListener#handleTableModelEvent(com.top_logic.layout.table.model.TableModelEvent)
         */
        @Override
		public void handleTableModelEvent(TableModelEvent aEvent) {
			TableModelEvent theEvent = new TableModelEvent(DelegatingTableModel.this, aEvent.getFirstRow(),
				aEvent.getLastRow(), aEvent.getType());
            DelegatingTableModel.this.fireTableModelEvent(theEvent);
        }
    }

    /*package protected*/ final void fireTableModelEvent(TableModelEvent event) {
        if (listeners == null) return;
        
        // Copy to prevent concurrent modification exceptions in case a listener
        // tries to remove itself as response to an event.
        TableModelListener[] currentListeners = 
            (TableModelListener[]) listeners.toArray(new TableModelListener[listeners.size()]);
        
        for (int n = 0, cnt = currentListeners.length; n < cnt; n++) {
            currentListeners[n].handleTableModelEvent(event);
        }
    }

	@Override
	public EditableRowTableModel getWrappedModel() {
		return inner;
	}
	
	@Override
	public ColumnConfiguration getColumnDescription(int aCol) {
	    return inner.getColumnDescription(aCol);
	}

	@Override
	public ColumnConfiguration getColumnDescription(String aColumnName) {
		return inner.getColumnDescription(aColumnName);
	}

	@Override
	public AccessContext prepareRows(Collection<?> accessedRows, List<String> accessedColumns) {
		return inner.prepareRows(accessedRows, accessedColumns);
	}

	@Override
	public TableRowFilter getFilter() {
		return inner.getFilter();
	}

	@Override
	public void setFilter(TableRowFilter filter, Comparator order) {
		inner.setFilter(filter, order);
	}

	@Override
	public void revalidateFilterMatchCount() {
		inner.revalidateFilterMatchCount();
	}

	@Override
	public Comparator getOrder() {
		return inner.getOrder();
	}

	@Override
	public void setOrder(Comparator order) {
		inner.setOrder(order);
	}

	@Override
	public boolean isFilterCountingEnabled() {
		return inner.isFilterCountingEnabled();
	}

}

