/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilderUtil;

/**
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ProxyListModelBuilderProducer implements ChartDataSource<ComponentDataContext>,
		InteractiveBuilder<ProxyListModelBuilderProducer, ChartContextObserver>,
		ConfiguredInstance<ProxyListModelBuilderProducer.Config> {

	/**
	 * Config-interface for {@link ProxyListModelBuilderProducer}.
	 */
	public interface Config extends PolymorphicConfiguration<ProxyListModelBuilderProducer> {

		/**
		 * <code>LIST_MODEL_BUILDER</code> Attribute name for list-model-builder-property
		 */
		String LIST_MODEL_BUILDER = "list-model-builder";

		/**
		 * <code>DATA_SOURCE</code> Attribute name for data-source-property
		 */
		String DATA_SOURCE = "data-source";

		@Override
		@ClassDefault(ProxyListModelBuilderProducer.class)
		public Class<ProxyListModelBuilderProducer> getImplementationClass();

		/**
		 * the configured {@link ListModelBuilder} to be called with the model from the
		 *         {@link ChartDataSource}.
		 */
		@InstanceFormat
		@Name(LIST_MODEL_BUILDER)
		public ListModelBuilder getListModelBuilder();

		/**
		 * the {@link ChartDataSource} providing the model to be used in a
		 *         {@link ProxyComponent} for the {@link ListModelBuilder}.
		 */
		@InstanceFormat
		@Name(DATA_SOURCE)
		public ChartDataSource<DataContext> getDataSource();

		/**
		 * true if the model should be set to a ProxyComponent acting as master, false if
		 *         the model should be set directly to the new instance.
		 */
		@BooleanDefault(true)
		public boolean getAsMaster();
	}

	private final Config _config;

	private ChartDataSource<DataContext> _dataSource;

	/**
	 * Config-Constructor for {@link ProxyListModelBuilderProducer}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public ProxyListModelBuilderProducer(InstantiationContext context, Config config) {
		_config = config;
		_dataSource = config.getDataSource();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Collection<?> getRawData(ComponentDataContext context) {
		Collection<? extends Object> rawData = getInnerRawData(context);
		assert rawData.size() <= 1;
		Object model = CollectionUtil.getFirst(rawData);
		Config config = getConfig();
		LayoutComponent component;
		try {
			component = new ProxyComponent(model, getConfig().getAsMaster());
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		component.setModel(model);
		ListModelBuilder builder = config.getListModelBuilder();

		if (builder.supportsModel(model, component)) {
			Collection<? extends Wrapper> result =
				CollectionUtil.dynamicCastView(Wrapper.class,
					(Collection<?>) builder.getModel(component.getModel(), component));
			return result;
		}
		else {
			return Collections.emptyList();
		}

	}

	private Collection<? extends Object> getInnerRawData(ComponentDataContext context) {
		Collection<? extends Object> rawData =
			_dataSource == null ? Collections.emptyList() : _dataSource.getRawData(context);
		return rawData;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		InteractiveBuilderUtil.fillContainer(container, getConfig(), observer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProxyListModelBuilderProducer build(FormContainer container) {
		Map<String, Object> create = InteractiveBuilderUtil.create(container, getConfig());
		_dataSource = (ChartDataSource<DataContext>) create.get(Config.DATA_SOURCE);
		return this;
	}

}