/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.filter;

import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.reporting.filter.model.FilterModel;

/**
 * A {@link FormFilterConfiguration} works together with a {@link FormFilterComponent}.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface FormFilterConfiguration {

	/**
	 * This method fills or adds all filter fields to the form context.
	 * 
	 * Note, this fill method is called out of the method
	 * {@link FormComponent#createFormContext()}. Thus it isn't possible to do
	 * something like this filterComponent.getFormContext(). StackOverFlow!!!!
	 * 
	 * @param filterComponent
	 *            A {@link FormFilterComponent} must NOT be <code>null</code>.
	 * @param formContext
	 *            A {@link FormContext} Must NOT be <code>null</code>.
	 */
	public void fillFormContext(FormFilterComponent filterComponent, FormContext formContext);
	
	/**
	 * This method creates a new {@link FilterModel}. This method is called if
	 * the component gets a new model.
	 * 
	 * @param filterComponent
	 *            A {@link FormFilterComponent} must NOT be <code>null</code>.
	 * @return Returns a new {@link FilterModel}.
	 */
	public FilterModel createNewFilterModel(FormFilterComponent filterComponent);

	/**
	 * This method is called if the update command is called. It is time to
	 * extract the filter information from the form context and update the
	 * filter model.
	 * 
	 * Call {@link FormFilterComponent#getFilterModel()} to get the current
	 * {@link FilterModel}.
	 * 
	 * @param filterComponent
	 *            A {@link FormFilterComponent} must NOT be <code>null</code>.
	 */
	public void updateFilterModel(FormFilterComponent filterComponent);
	
	/**
	 * This method returns the used event type (e.g. {@link ModelEventListener#MODEL_MODIFIED}).
	 * 
	 * @return Returns the used event type.
	 */
	public int getEventType();
	
	/**
	 * This method returns <code>true</code> if the given model is supported,
	 * <code>false</code> otherwise.
	 * 
	 * @param model
	 *            A arbitrary model.
	 * @return Returns <code>true</code> if the given model is supported.
	 */
	public boolean supportModel(Object model);
	
	/**
	 * This method returns <code>true</code> if the form context should be
	 * reset if the component gets a new model, <code>false</code> otherwise.
	 * 
	 * @return Returns <code>true</code> if the form context should be reset
	 */
	public boolean resetFormContext();
	
}
