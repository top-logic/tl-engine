/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.component.configuration;

import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.util.FormFieldConstants;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.view.component.DefaultProducerFilterComponent;

/**
 * This class is a filter configuration for the
 * {@link DefaultProducerFilterComponent}.
 * 
 * @see DefaultProducerFilterComponent
 * 
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface ProducerFilterConfiguration extends FormFieldConstants {

	/**
	 * This method fills the given form context with the necessary information
	 * of the given model. The form context is never <code>null</code>.
	 */
	public void fill(Object aModel, FormContext aContext);

	/**
	 * This method fills the given chart context with the data from the form
	 * context. The given object is never <code>null</code>. The model is
	 * already set.
	 */
	public void fill(FormContext aContext, ChartContext aChartContext);

	/**
	 * This method returns <code>true</code> if the form context must be reset
	 * for the given model. This method is normally called if a new model is set
	 * to the filter component. But for some filter configurations it isn't
	 * neccessary to reset the form context.
	 * 
	 * @param aModel
	 *            The current model.
	 */
	public boolean resetFormContext(Object aModel);

	/**
	 * This method returns the field set label (NOT the resource key) or
	 * <code>null</code> if no field set is required.
	 */
	public String getFieldsetLabel();

}
