/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.table;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.NullValue;

/**
 * This class represents a ReportTable which is the datastructure the reporting framework can work with.
 * Usually a Reporttable is a Table of Values. It fully implements the swing TableModell for maximum compatibility
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class ReportTable implements TableModel {

    /** The result in a list . */
    private List tableData;

	/** guess what ??! */
	private Dimension tableSize;
	
	/** The Descriptor of this table */
	private TableDescriptor theDescriptor;
	
    /** The list of listeners to this model. */
    private List modelListeners;

	
	/**
	 * Constructs a new ReportTable
	 * 
	 * @param aDescriptor is the TableDescripor (see it's javadoc)
	 * @param tableData - the tableData is provides as List which contains other Lists which represent the tables rows.
	 * 
	 * Note:
	 * A ReportTable has to follow some rules zu be valid :
	 * - 1. All rows have to have excact the same number of columns as specified by the header row of the TableDescriptor
	 * - 2. The table has to have excact the same number of row as specified by the header column of the TableDescriptor
	 * - 3. All values (cells) of the table have to be of the same type described by the TableDescriptor (with the exception of NullValues, they are always allowed)
	 * - 4. Above rule also prohibits the usage of "null" for empty Cells. Those should be filled with NullValues instead.
	 * - 5. Although your Descriptor can specify any type you want, the ReportTable accepts Values only. So your described Type has to be an instanceof Value
	 *   In theory other types would be possible, but since this table is designed for use with the reporting Framework they are not allowed
	 *   This is mainly because default visualisation can work with values only and because the usage of the NullValue instead of null is mandatory
	 * 
	 * @exception ArrayIndexOutOfBoundsException if rules 1 or 2 are broken
	 * @exception ClassCastException if rules 3,4 or 5 are broken
	 */
	public ReportTable(TableDescriptor aDescriptor, List tableData) throws 
																	ArrayIndexOutOfBoundsException, 
                                               						ClassCastException {
		int numberRows 	= -1;
		int numberCols 	= -1;
		try{
			numberCols = aDescriptor.getHeaderRow().size();
			numberRows = aDescriptor.getHeaderColumn().size();
		}catch(Exception e){
            Logger.debug("Exception in CTor", e, this);
		    //one of the params was null, we react later to it
		}

		//checking if headerColumns has the same size as the number of rows in the data
		if(numberRows != tableData.size()){
            throw new ArrayIndexOutOfBoundsException(
                "Given header column specified a different row number than " +
                "the given table data actually had . Rownumber of header " +
                "column and table data have to match each other.");
		}

		//checking if the List given as Data contains other java.util.Lists (tablerows) only
		//Also here is checked if all rows have the same size which has to be the same
		//as specified by the headerRow
		for (int rowIdx = 0;rowIdx < tableData.size();rowIdx++){
			List aRow = new ArrayList();

			try{
				aRow = (List)tableData.get(rowIdx);
			}catch (ClassCastException e){
                throw new ClassCastException("Table data is expected to " +
				                            "be given as list of lists where each list " +
				                            "contains a row. The given table data " +
				                            "contained different objects.");
			}
		
			if (aRow.size() != numberCols){
	            throw new ArrayIndexOutOfBoundsException("Given header row " +
									                    "specifies a different number of Columns " +
									                    "than the given table data. The header row " +
									                    "has to have the same site as each table row " +
									                    "within the table data.");
			}


			for (int colIdx = 0; colIdx < aRow.size();colIdx++){
				Object aCell = aRow.get(colIdx);
				boolean isValue	 = aCell instanceof Value;
				boolean isNullValue = aCell instanceof NullValue;
				boolean isDescribedValue = aDescriptor.getType().isAssignableFrom(aCell.getClass());
				boolean typeIsValid = isValue && (isDescribedValue || isNullValue);
				if(!typeIsValid){
					throw new ClassCastException("All objects within the given table have to be of the" +
												  "same type as specified by the descriptor and an instanceof Value");
				}
			}
		}

		//all Checks performed, data seems to be valid
		this.theDescriptor	= aDescriptor;
		this.tableData		= tableData;
		this.tableSize		= new Dimension(numberCols,numberRows);
	}

	/**
	 * the TableDescripor
	 */
    public TableDescriptor getTableDescriptor(){
    	return this.theDescriptor;
    }
    

    /**
     * Return the data of the table.
     * 
     * The returned list contains lists, which can be seen as rows in a table.
     * 
     * @return    The list of rows in the table.
     */
    public List getTableData() {
        return this.tableData;
    }

	/**
	 * the size of the table
	 */
	public Dimension getSize(){
		return this.tableSize;
	}


	//Implementation TableModell

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return getSize().height;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return getSize().width;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return (String)theDescriptor.getHeaderRow().get(columnIndex);
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 * all Columns have the same class in this implementation
	 */
    @Override
	public Class getColumnClass(int anIndex) {
        return (this.theDescriptor.getType());
    }

	/**
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
        List theRow = (List) this.getTableData().get(rowIndex);
        return (theRow.get(columnIndex));
	}

	/**
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        List theRow = (List) this.getTableData().get(rowIndex);
        theRow.set(columnIndex, aValue);
        TableModelEvent theEvent = new TableModelEvent(this, 
                                                       rowIndex, 
                                                       rowIndex, 
                                                       columnIndex);
        this.notifyTableModelListeners(theEvent);
	}

	/**
	 * @see javax.swing.table.TableModel#addTableModelListener(TableModelListener)
	 */
	@Override
	public void addTableModelListener(TableModelListener aListener) {
	    if (aListener != null) {
		    this.getListeners().add(aListener);
		}
	}

	/**
	 * @see javax.swing.table.TableModel#removeTableModelListener(TableModelListener)
	 */
	@Override
	public void removeTableModelListener(TableModelListener aListener) {
    	if (aListener != null) {
            this.getListeners().remove(aListener);
        }
	}

    /**
     * Return the list of listeners to this model.
     * 
     * @return    The list of listeners (never <code>null</code>).
     */
    protected List getListeners() {
        if (this.modelListeners == null) {
            this.modelListeners = new Vector();
        }
        return (this.modelListeners);
    }

    /**
     * Send notification to all listeners of this model.
     */
    protected void notifyTableModelListeners(TableModelEvent anEvent) {
        TableModelListener theListener = null;
        Iterator           theIt       = this.getListeners().iterator();

        while (theIt.hasNext()) {
            try {
                theListener = (TableModelListener) theIt.next();

                theListener.tableChanged(anEvent);
            }
            catch (Exception ex) {
                Logger.info("Unable to notify " + theListener, ex, this);
            }
        }
    }


}
