/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;

/**
 * Simple {@link ChartDataSource} that provides the model of the master of the component-context.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class MasterModelProducer implements ChartDataSource<ComponentDataContext>,
		InteractiveBuilder<MasterModelProducer, ChartContextObserver>,
		ConfiguredInstance<MasterModelProducer.Config> {

	/**
	 * Config-interface for {@link MasterModelProducer}.
	 */
	public interface Config extends PolymorphicConfiguration<MasterModelProducer> {

		@Override
		@ClassDefault(MasterModelProducer.class)
		public Class<MasterModelProducer> getImplementationClass();

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link MasterModelProducer}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public MasterModelProducer(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Collection<? extends Object> getRawData(ComponentDataContext context) {
		LayoutComponent master = context.getComponent().getMaster();
		if (master == null) {
			return Collections.emptyList();
		}
		Object model = master.getModel();
		if (model == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(model);

	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver arg) {
		// no gui-elements necessary
	}

	@Override
	public MasterModelProducer build(FormContainer container) {
		return this;
	}

}