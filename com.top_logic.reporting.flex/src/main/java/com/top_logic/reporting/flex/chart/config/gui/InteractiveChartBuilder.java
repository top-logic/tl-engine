/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.gui;

import org.jfree.chart.JFreeChart;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.bar.BarChartBuilder;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.DefaultModelPreparation;
import com.top_logic.reporting.flex.chart.config.model.ModelPreparation;

/**
 * Top-level builder class for interactive charts.
 * 
 * <p>
 * Creates a generic GUI with containers for all properties with return type
 * {@link InteractiveBuilder}. By default these are
 * </p>
 * 
 * <ul>
 * <li>{@link ChartDataSource}</li>
 * <li>{@link ModelPreparation}</li>
 * </ul>
 * 
 * <p>
 * All sub-configs may use {@link InteractiveBuilder} itself. This will result in nested interactive
 * containers.
 * </p>
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class InteractiveChartBuilder implements
		InteractiveBuilder<ChartConfig, ChartContextObserver>,
		ConfiguredInstance<InteractiveChartBuilder.Config> {

	/**
	 * Provider class for a global template to set at the outer {@link FormContainer} if it is not
	 * possible to create a satisfying layout by concatenating all sub-parts.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public interface TemplateProvider {

		/**
		 * @param container
		 *        the top-level container to apply the template to
		 */
		public void applyTemplate(FormContainer container);

	}

	/**
	 * Base config-interface for {@link InteractiveChartBuilder}
	 */
	public interface Config extends PolymorphicConfiguration<InteractiveChartBuilder> {

		/**
		 * Property name for {@link #getDataSource()}.
		 */
		public static final String DATA_SOURCE = "data-source";

		/**
		 * Property name for {@link #getChartBuilder()}.
		 */
		public static final String CHART_BUILDER = "chart-builder";

		/**
		 * Property name for {@link #getModelPreparation()}.
		 */
		public static final String MODEL_PREPARATION = "model-preparation";

		/**
		 * Getter for the builder of a {@link ChartDataSource} that provides the initial data for
		 * the chart.
		 */
		@InstanceFormat
		@Name(DATA_SOURCE)
		public InteractiveBuilder<ChartDataSource<?>, ChartContextObserver> getDataSource();

		/**
		 * Getter for the builder of a {@link ModelPreparation} that builds a {@link ChartTree} from
		 * the initial data.
		 */
		@InstanceFormat
		@Name(MODEL_PREPARATION)
		public InteractiveBuilder<DefaultModelPreparation, ChartContextObserver> getModelPreparation();

		/**
		 * Getter for the {@link JFreeChartBuilder} that translates the internal modle (
		 * {@link ChartTree}) to a {@link JFreeChart}.
		 */
		@InstanceFormat
		@InstanceDefault(BarChartBuilder.class)
		@Name(CHART_BUILDER)
		public JFreeChartBuilder<?> getChartBuilder();

		/**
		 * see {@link #getChartBuilder()}
		 */
		public void setChartBuilder(JFreeChartBuilder<?> builder);

		/**
		 * Getter for an optional global {@link TemplateProvider}. Use with care: The provided
		 * template must match the configured sub-parts. Changing the config may lead to an invalid
		 * template and result in erros. Only use this if the layout resulting from all sub-parts is
		 * not satisfying.
		 * 
		 * @return the global {@link TemplateProvider}, may be null
		 */
		@InstanceFormat
		public TemplateProvider getTemplateProvider();
	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link InteractiveChartBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public InteractiveChartBuilder(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		TemplateProvider provider = getConfig().getTemplateProvider();

		InteractiveBuilderUtil.fillContainer(container, getConfig(), observer);
		if (provider != null) {
			provider.applyTemplate(container);
		}
	}

	@Override
	public ChartConfig build(FormContainer container) {
		return InteractiveBuilderUtil.create(container, getConfig(), ChartConfig.class);
	}

}
