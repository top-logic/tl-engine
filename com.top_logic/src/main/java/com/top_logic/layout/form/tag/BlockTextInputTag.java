/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.BlockTextInputControl;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class BlockTextInputTag extends TextInputTag {

    private int colsCollapsed = -1; // mandatory
    private int rowsCollapsed = -1;  // mandatory
    private int colsExpanded  = -1;
    private int rowsExpanded  = -1;
    private int colsCollapsedEdit = -1;
    private int rowsExpandedEdit  = 5;
    private int colsExpandedEdit  = -1;

    /** 
     * Creates a {@link BlockTextInputTag}.
     * 
     */
    public BlockTextInputTag() {
    }

    public int getColsCollapsed() {
        return (this.colsCollapsed);
    }

    public void setColsCollapsed(int aColsCollapsed) {
        this.colsCollapsed = aColsCollapsed;
    }

    public int getRowsCollapsed() {
        return (this.rowsCollapsed);
    }

    public void setRowsCollapsed(int aRowsCollapsed) {
        this.rowsCollapsed = aRowsCollapsed;
    }

    public int getColsExpanded() {
        return (this.colsExpanded);
    }

    public void setColsExpanded(int aColsExpanded) {
        this.colsExpanded = aColsExpanded;
    }

    public int getRowsExpanded() {
        return (this.rowsExpanded);
    }

    public void setRowsExpanded(int aRowsExpanded) {
        this.rowsExpanded = aRowsExpanded;
    }

    public int getColsCollapsedEdit() {
        return (this.colsCollapsedEdit);
    }

    public void setColsCollapsedEdit(int aColsCollapsedEdit) {
        this.colsCollapsedEdit = aColsCollapsedEdit;
    }

    public int getRowsExpandedEdit() {
        return (this.rowsExpandedEdit);
    }

    public void setRowsExpandedEdit(int aRowsExpandedEdit) {
        this.rowsExpandedEdit = aRowsExpandedEdit;
    }

    public int getColsExpandedEdit() {
        return (this.colsExpandedEdit);
    }

    public void setColsExpandedEdit(int aColsExpandedEdit) {
        this.colsExpandedEdit = aColsExpandedEdit;
    }

    @Override
	public Control createControl(FormMember aMember, String displayStyle) {
        BlockTextInputControl theControl = new BlockTextInputControl((FormField) aMember);
        
        // mandatory
        if (colsCollapsed > -1) {
            theControl.setBlockColsCollapsed(colsCollapsed);
        }

        if (rowsCollapsed > -1) {
            theControl.setBlockRowsCollapsed(rowsCollapsed);
        }
        
        if (colsCollapsedEdit > -1) {
            theControl.setSingleLineColumns(colsCollapsedEdit);
        }
//        else {
//            // same width as in view mode
//            theControl.setSingleLineColumns(colsCollapsed);
//        }

        // can handle -1
        theControl.setBlockRowsExpanded(rowsExpanded);
        
        if (colsExpanded > -1) {
            theControl.setBlockColsExpanded(colsExpanded);
        }
        else if (colsCollapsed > -1){ 
            // same width as in collapse state
            theControl.setBlockColsExpanded(colsCollapsed);
        }
        
        if (colsExpandedEdit > -1) {
            theControl.setMultiLineColumns(colsExpandedEdit);
        }
//        else {
//            theControl.setMultiLineColumns(colsCollapsed);
//        }
        
        theControl.setMultiLineRows(rowsExpandedEdit);
        
        // copied from super
        if (style.isSet())
            theControl.setInputStyle(style.get());
        if (immutableStyle.isSet())
            theControl.setStyle(immutableStyle.get());
        if (!StringServices.isEmpty(onFocus)) {
        	theControl.setOnFocus(new ConstantDisplayValue(onFocus));
        }
        if (!StringServices.isEmpty(onInput)) {
			theControl.setOnInput(new ConstantDisplayValue(onInput));
		}
        theControl.setUnsafeHTML(unsafeHTML);
        theControl.setType(this.type);
        if (tabindex.isSet())
            theControl.setTabIndex(tabindex.get());
        
        return theControl;
    }

}

