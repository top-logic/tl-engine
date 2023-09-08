/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.col.ConcatenatedClosableIterator;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.util.MetaElementProvider;
import com.top_logic.reporting.flex.chart.config.util.MetaElementProvider.MetaElementProviderFormat;

/**
 * Simple {@link ChartDataSource} that provides objects for a configured meta-element-type.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class MetaElementObjectProducer implements ChartDataSource<DataContext>,
		InteractiveBuilder<MetaElementObjectProducer, ChartContextObserver>,
		ConfiguredInstance<MetaElementObjectProducer.Config> {

	/**
	 * Config-interface for {@link MetaElementObjectProducer}.
	 */
	public interface Config extends PolymorphicConfiguration<MetaElementObjectProducer> {

		@Override
		@ClassDefault(MetaElementObjectProducer.class)
		public Class<MetaElementObjectProducer> getImplementationClass();

		/**
		 * the meta-element (via {@link MetaElementProvider}) of the objects this instance
		 *         should provide
		 */
		@Format(MetaElementProviderFormat.class)
		public MetaElementProvider getType();

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link MetaElementObjectProducer}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public MetaElementObjectProducer(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Collection<? extends Object> getRawData(DataContext context) {
		Set<? extends TLClass> types = getConfig().getType().getTypes();
		ArrayList<Object> buffer = new ArrayList<>();
		for (TLClass type : types) {
			try (ConcatenatedClosableIterator<TLObject> instances =
				AttributeOperations.allInstancesIterator(type, TLObject.class)) {
				while (instances.hasNext()) {
					buffer.add(instances.next());
				}
			}
		}
		return buffer;

	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		// no gui-elements necessary
	}

	@Override
	public MetaElementObjectProducer build(FormContainer container) {
		return this;
	}

}