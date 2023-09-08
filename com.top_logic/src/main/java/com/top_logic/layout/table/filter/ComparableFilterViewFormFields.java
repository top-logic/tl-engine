/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.SelectField;

/**
 * Holds {@link FormField}s of {@link ComparableFilterView}
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class ComparableFilterViewFormFields {
	
	private SelectField selectableValuesField;
	private SelectField operatorField;
	private FormField primaryFilterPatternField;
	private FormField secondaryFilterPatternField;

	/* For filter internal use only */
	ComparableFilterViewFormFields() {
	}

	public SelectField getSelectableValuesField() {
		return selectableValuesField;
	}

	public void setSelectableValuesField(SelectField selectableValuesField) {
		this.selectableValuesField = selectableValuesField;
	}

	public SelectField getOperatorField() {
		return operatorField;
	}
	
	public void setOperatorField(SelectField operatorField) {
		this.operatorField = operatorField;
	}
	
	public FormField getPrimaryFilterPatternField() {
		return (primaryFilterPatternField);
	}
	
	public void setPrimaryFilterPatternField(FormField primaryFilterPatternField) {
		this.primaryFilterPatternField = primaryFilterPatternField;
	}
	
	public FormField getSecondaryFilterPatternField() {
		return (secondaryFilterPatternField);
	}
	
	public void setSecondaryFilterPatternField(FormField secondaryFilterPatternField) {
		this.secondaryFilterPatternField = secondaryFilterPatternField;
	}
}
