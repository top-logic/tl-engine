/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.filter;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.component.Updatable;
import com.top_logic.layout.component.UpdateCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.reporting.filter.model.FilterModel;

/**
 * A {@link FormFilterComponent} is a generic form filter component and works together
 * with a {@link FormFilterConfiguration}.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class FormFilterComponent extends FormComponent implements Updatable {

	public interface Config extends FormComponent.Config {
		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(LAYOUT_ATTRIBUTE_CONFIGURATION_CLASS)
		@Mandatory
		@InstanceFormat
		FormFilterConfiguration getConfigurationClass();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(UpdateCommandHandler.COMMAND_ID);
		}
	}

	/** The layout xml attribute for the filter configuration class name. */
    public static final String LAYOUT_ATTRIBUTE_CONFIGURATION_CLASS = "configurationClass";
	
	private FilterModel filterModel;
	private FormFilterConfiguration configuration;
	
	/** 
	 * The layout framework uses this constructor to create new {@link FormFilterComponent}s.
	 */
	public FormFilterComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		
		setConfiguration(atts.getConfigurationClass());
		this.filterModel = null;
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext("FormFilterComponent", getResPrefix());
		
		getConfiguration().fillFormContext(this, formContext);
		
		return formContext;
	}

	@Override
	public void update() {
		FilterModel filterModel = getFilterModel();
		if (filterModel != null) {
			FormFilterConfiguration configuration = getConfiguration();
			configuration.updateFilterModel(this);
		}
	}
	
	public void sendSlaveModel() {
		FormFilterConfiguration configuration = getConfiguration();
		setFilterModel(configuration.createNewFilterModel(this));
		getMainLayout().modelChanged(getFilterModel(), this, configuration.getEventType());
	}
	
	@Override
	protected boolean isChangeHandlingDefault() {
		return false;
	}
	
	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);

		if (getConfiguration().resetFormContext()) {
			removeFormContext();
		}

		sendSlaveModel();
	}
	
	/**
	 * This method returns the filterModel.
	 * 
	 * @return Returns the filterModel.
	 */
	public FilterModel getFilterModel() {
		return this.filterModel;
	}

	/** See {@link #getFilterModel}. */
	public void setFilterModel(FilterModel filterModel) {
		this.filterModel = filterModel;
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return getConfiguration().supportModel(object);
	}

	/**
	 * Returns the configuration.
	 */
	public FormFilterConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * See get-method.
	 */
	public void setConfiguration(FormFilterConfiguration configuration) {
		this.configuration = configuration;
	}

}
