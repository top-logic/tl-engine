/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.tableForm;

import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * Generator of a {@link FormGroup} with the fields for a single row object.
 * 
 * @see RowFormGroupHolder
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface RowFormGroupGenerator {

	/**
	 * Adds the form fields for the given row object.
	 * 
	 * @param aLC
	 *        The component displaying the fields.
	 * @param aFormGroup
	 *        The group to add the fields for the row to.
	 * @param aRowObject
	 *        The row object to create fields for.
	 */
    public void       addFormFields(ControlComponent aLC, FormGroup aFormGroup, Object aRowObject);

	/**
	 * The name of the {@link FormGroup} holding the fields of the given row object.
	 */
    public String     getRowGroupName(Object aRowObject);

	/**
	 * Processes the values entered in the fields of a row.
	 * 
	 * @param aComponent
	 *        The component displaying the fields.
	 * @param aFormGroup
	 *        The group containing the fields.
	 * @param aTableAttribute
	 *        The field holding the table rows.
	 * @param aModel
	 *        The model to apply the result to.
	 */
    public void       handleResult(ControlComponent aComponent, FormGroup aFormGroup, FormField aTableAttribute, Object aModel);

	/**
	 * The {@link ControlProvider} creating the controls for the generated fields.
	 */
	ControlProvider getControlProvider();
}
