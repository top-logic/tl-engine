/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;

/**
 * Simple {@link ChartDataSource} that provides the model of the context-component.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ModelProducer implements ChartDataSource<ComponentDataContext>,
		InteractiveBuilder<ModelProducer, ChartContextObserver>, ConfiguredInstance<ModelProducer.Config> {

	/**
	 * Config-interface for {@link ModelProducer}
	 */
	public interface Config extends PolymorphicConfiguration<ModelProducer> {

		@Override
		@ClassDefault(ModelProducer.class)
		public Class<ModelProducer> getImplementationClass();

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link ModelProducer}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public ModelProducer(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<? extends Object> getRawData(ComponentDataContext context) {
		Object model = context.getComponent().getModel();
		List<? extends Object> rawData;
		if (model == null) {
			rawData = Collections.emptyList();
		} else if (model instanceof List<?>) {
			rawData = (List<? extends Object>) model;
		} else {
			rawData = Collections.singletonList(model);
		}
		return rawData;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver arg) {
		// no gui-elements necessary
	}

	@Override
	public ModelProducer build(FormContainer container) {
		return this;
	}

}