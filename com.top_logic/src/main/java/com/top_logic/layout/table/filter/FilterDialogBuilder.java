/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Optional;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.control.TableControl.SortCommand;

/**
 * An interface to manage creation and content display of {@link TableFilter}s
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public interface FilterDialogBuilder {
	
	/**
	 * The {@link DialogModel} of the filter dialog.
	 */
	PopupDialogModel getDialogModel();

	/**
	 * This method opens a filter dialog
	 * 
	 * @param contentControl
	 *        - the content, that shall be displayed
	 * @param context
	 *        - the display context
	 * @param tableFilterModel
	 *        the model of the table filter
	 * @param filterContext - the content controls form group.
	 */
	public void openFilterDialog(Control contentControl, DisplayContext context, PopupHandler handler,
								 TableFilterModel tableFilterModel, FormGroup filterContext);
	
	/**
	 * This method closes the filter dialog
	 * 
	 */
	public void closeFilterDialog();
	
	/**
	 * Creates the content of the filter dialog.
	 * 
	 * @param filterModel
	 *        The model of the table filter
	 * @param context
	 *        The display context
	 * @param form
	 *        The form parent to add filter dialog input elements to.
	 * @param sortControl
	 *        Optional {@link BlockControl} with {@link SortCommand}s. If empty the dialog will be
	 *        opened without {@link SortCommand}s.
	 * @return the control, representing the filter dialog content
	 */
	public Control createFilterDialogContent(TableFilterModel filterModel, DisplayContext context,
											 FormContext form, Optional<BlockControl> sortControl);
}
