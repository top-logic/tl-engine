/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilderUtil;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class TransformedDataSourceBuilder implements
		InteractiveBuilder<ChartDataSource<DataContext>, ChartContextObserver>, ChartDataSource<DataContext>,
		ConfiguredInstance<TransformedDataSourceBuilder.Config> {

	/**
	 * Config-interface for {@link TransformedDataSourceBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<TransformedDataSourceBuilder> {

		/**
		 * <code>TRANSFORMER</code> Attribute name for transformer-property
		 */
		public static final String TRANSFORMER = "transformer";

		/**
		 * <code>DATA_SOURCE</code> Attribute name for data-source-property
		 */
		public static final String DATA_SOURCE = "data-source";

		/**
		 * the transformer that translates the initial objects from the inner data-source to
		 *         the output-objects
		 */
		@InstanceFormat
		@Name(TRANSFORMER)
		public Transformer getTransformer();
		
		/**
		 * the data-source that provides the initial objects
		 */
		@InstanceFormat
		@Name(DATA_SOURCE)
		public ChartDataSource<DataContext> getDataSource();
		
	}

	private final Config _config;

	private ChartDataSource<DataContext> _innerDataSource;

	private Transformer _transformer;

	/**
	 * Config-Constructor for {@link TransformedDataSourceBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public TransformedDataSourceBuilder(InstantiationContext context, Config config) {
		_config = config;
		_innerDataSource = config.getDataSource();
		_transformer = config.getTransformer();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		InteractiveBuilderUtil.fillContainer(container, getConfig(), observer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ChartDataSource<DataContext> build(FormContainer container) {
		Map<String, Object> map = InteractiveBuilderUtil.create(container, getConfig());
		_innerDataSource = (ChartDataSource<DataContext>) map.get(Config.DATA_SOURCE);
		_transformer = (Transformer) map.get(Config.TRANSFORMER);
		return this;
	}

	@Override
	public Collection<? extends Object> getRawData(DataContext context) {
		Collection<Wrapper> wrappers = CollectionUtil.dynamicCastView(Wrapper.class, _innerDataSource.getRawData(context));
		return _transformer.transform(wrappers);
	}

}
