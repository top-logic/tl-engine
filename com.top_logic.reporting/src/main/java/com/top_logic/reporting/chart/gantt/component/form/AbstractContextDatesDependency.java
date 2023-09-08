/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component.form;

import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.listener.AbstractValueListenerDependency;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.reporting.chart.gantt.component.GanttChartFilterComponent;
import com.top_logic.util.Utils;

/**
 * Sets the start-/end- date-fields to the start-/end- date of the current context element if one of
 * the related fields changes.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public abstract class AbstractContextDatesDependency extends AbstractValueListenerDependency {
	private final ComplexField _startDateField;

	private final ComplexField _endDateField;
	
	/**
	 * Creates a new {@link AbstractContextDatesDependency}.
	 */
	public AbstractContextDatesDependency(FormField[] relatedFields, ComplexField startDateField,
			ComplexField endDateField) {
		super(relatedFields);
		_startDateField = startDateField;
		_endDateField = endDateField;
	}

	@Override
	protected void valueChanged(int fieldIndex, FormField field, Object oldValue, Object newValue) {
		if (GanttChartFilterComponent.PROPERTY_USE_CONTEXT_DATES.equals(field.getName())) {
			// "useContextDatesField" was changed
			if (Utils.getbooleanValue(newValue)) {
				// "useContextDatesField" was enabled --> get start-/end-dates from context element and disable date fields
				setStartEndDateFromContextElt(getStartEndDateFromContextElt());
				disableStartEndField(true);
			} else {
				// "useContextDatesField" was disabled --> enable date fields
				disableStartEndField(false);
			}
		} else if (GanttChartFilterComponent.PROPERTY_SHOW_FORECAST.equals(field.getName()) && Utils.getbooleanValue(get(0).getValue())) {
			// "useForecastValuesField" was changed and "useContextDatesField" is enabled --> get start-/end-dates from context element
			setStartEndDateFromContextElt(getStartEndDateFromContextElt());
		}
	}

	/**
	 * Return tuple of start and end date from current context
	 */
	protected abstract Tuple getStartEndDateFromContextElt();

	private void setStartEndDateFromContextElt(Tuple contextStartEndDate) {
		_startDateField.setValue(contextStartEndDate.get(0));
		_endDateField.setValue(contextStartEndDate.get(1));
	}

	private void disableStartEndField(boolean immute) {
		_startDateField.setDisabled(immute);
		_endDateField.setDisabled(immute);
	}

	protected final ComplexField getStartDateField() {
		return _startDateField;
	}

	protected final ComplexField getEndDateField() {
		return _endDateField;
	}
}