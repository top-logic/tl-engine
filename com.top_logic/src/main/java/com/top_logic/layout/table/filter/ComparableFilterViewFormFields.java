/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.SelectField;

/**
 * Holds {@link FormField}s of {@link ComparableFilterView}.
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

	/** The field selecting the values to filter for. */
	public SelectField getSelectableValuesField() {
		return selectableValuesField;
	}

	/** Setter for {@link #getSelectableValuesField()}. */
	public void setSelectableValuesField(SelectField selectableValuesField) {
		this.selectableValuesField = selectableValuesField;
	}

	/** The field selecting the comparison operator. */
	public SelectField getOperatorField() {
		return operatorField;
	}
	
	/** Setter for {@link #getOperatorField()}. */
	public void setOperatorField(SelectField operatorField) {
		this.operatorField = operatorField;
	}
	
	/** The field for entering the first value to compare with. */
	public FormField getPrimaryFilterPatternField() {
		return (primaryFilterPatternField);
	}
	
	/** Setter for {@link #getPrimaryFilterPatternField()}. */
	public void setPrimaryFilterPatternField(FormField primaryFilterPatternField) {
		this.primaryFilterPatternField = primaryFilterPatternField;
	}
	
	/** The field for entering the second value to compare with, used for a range comparison. */
	public FormField getSecondaryFilterPatternField() {
		return (secondaryFilterPatternField);
	}
	
	/** Setter for {@link #getSecondaryFilterPatternField()}. */
	public void setSecondaryFilterPatternField(FormField secondaryFilterPatternField) {
		this.secondaryFilterPatternField = secondaryFilterPatternField;
	}
}
