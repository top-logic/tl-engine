/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.util.Comparator;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.decorator.DecorateService;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.RowObjectCreator;
import com.top_logic.layout.table.RowObjectRemover;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableListControl;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * Factory for a Control to displays an editable list as table specified by a TableField
 * with an ObjectTableModel.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class TableListTag extends TableTag {

    private RowObjectCreator rowObjectCreator;
    private RowObjectRemover rowObjectRemover;

	private ITableRenderer renderer;
    private boolean     appendAlways = false;

    private int         initialSortColumn = -1;
    private boolean     ascending = true;
    private boolean     sortable = false;

    private Comparator[] comparators;



    public void setRowObjectCreator(RowObjectCreator rowObjectCreator) {
        this.rowObjectCreator = rowObjectCreator;
    }

    public void setRowObjectRemover(RowObjectRemover rowObjectRemover) {
        this.rowObjectRemover = rowObjectRemover;
    }

    @Override
	public void setRenderer(ITableRenderer renderer) {
        this.renderer = renderer;
    }

    public void setAppendAlways(boolean appendAlways) {
        this.appendAlways = appendAlways;
    }

    public void setInitialSortColumn(int aInitialSortColumn) {
        this.initialSortColumn = aInitialSortColumn;
    }

    public void setComparators(Comparator[] someComparators) {
        this.comparators = someComparators;
    }

    public void setAscending(boolean aAscending) {
        this.ascending = aAscending;
    }

    public void setSortable(boolean aSortable) {
        this.sortable = aSortable;
    }

    @Override
	protected void teardown() {
        super.teardown();
        rowObjectCreator = null;
        rowObjectRemover = null;
        renderer = null;
        appendAlways = false;

        initialSortColumn = -1;
        ascending = true;
        sortable = false;
    }


    @Override
	public Control createControl(FormMember member, String displayStyle) {
		boolean decorated = DecorateService.isDecorated(member.getFormContext());
		if (decorated) {
			return super.createControl(member, displayStyle);
		}

        TableField theTableField = (TableField) member;
        if (renderer == null) {
            renderer = DefaultTableRenderer.newInstance();
        }

        if (!(theTableField.getTableModel() instanceof EditableRowTableModel)) {
        	// Note: The TableListControl offers functionality to modify the base rows of a table model and therefore 
        	// depends on the editability of the table model.
            throw new IllegalArgumentException("The TableModel of the TableField must be instanceof EditableRowTableModel.");
        }

        TableListControl theControl = TableListControl.createTableListControl(theTableField, renderer, rowObjectCreator, rowObjectRemover, theTableField.isSelectable(), !theTableField.isImmutable(), sortable);
		TableViewModel theViewModel = theControl.getViewModel();
        theControl.setAppendAlways(appendAlways);
        
		theControl.addVisibilityListenerFor(member);

        if (this.comparators != null) {
        	theViewModel.setColumnComparators(this.comparators);
        }
        theViewModel.setSortedApplicationModelColumn(this.initialSortColumn, this.ascending);
        return theControl;
    }

}
