/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveChartBuilder;
import com.top_logic.reporting.flex.chart.config.util.Configs;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class ChartConfigComponent extends FormComponent implements Selectable {

	/**
	 * <code>CHART</code>: Name of the {@link FormContainer} used by the
	 * {@link InteractiveBuilder}
	 */
	public static final String CHART_FIELD = "chart";

	private InteractiveChartBuilder.Config _baseConfig;

	private InteractiveChartBuilder _chartBuilder;

	/**
	 * Configuration interface for {@link ChartConfigComponent}.
	 */
	public interface Config extends FormComponent.Config {

		/**
		 * Inline {@link InteractiveChartBuilder} configuration.
		 * 
		 * <p>
		 * If this does not exists {@link #getBaseConfigFile()} is resolved instead.
		 * </p>
		 */
		public InteractiveChartBuilder.Config getBaseConfig();

		/**
		 * Resource name of the XML file containing an {@link InteractiveChartBuilder}
		 * configuration.
		 * 
		 * <p>
		 * Only relevant if no inline {@link #getBaseConfig()} exists.
		 * </p>
		 * 
		 * <p>
		 * If neither {@link #getBaseConfigFile()} nor {@link #getBaseConfig()} is given, a chart
		 * can only displayed by
		 * {@link ChartConfigComponent#setBaseConfig(com.top_logic.reporting.flex.chart.config.gui.InteractiveChartBuilder.Config)
		 * setting a chart configuration by program code}.
		 * </p>
		 */
		@Nullable
		public String getBaseConfigFile();

	}

	/**
	 * Creates a {@link ChartConfigComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChartConfigComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		resetBaseConfig();
	}

	/**
	 * Resets everything but the base-config
	 */
	protected void reset() {
		resetChart();
		removeFormContext();
	}

	@Override
	public void clearSelection() {
		resetChart();
	}

	/**
	 * Resets the {@link ChartModel}.
	 */
	protected void resetChart() {
		setSelected(null);
	}

	/**
	 * Resets the base-config to the initial configured state.
	 */
	protected void resetBaseConfig() {
		setBaseConfig(getConfiguredBuilderConfig());
	}

	private InteractiveChartBuilder.Config getConfiguredBuilderConfig() {
		Config config = config();
		InteractiveChartBuilder.Config result = config.getBaseConfig();
		if (result == null) {
			String file = config.getBaseConfigFile();
			if (file == null) {
				return null;
			}
			result = Configs.readBuilderConfig(file);
		}
		return result;
	}

	private Config config() {
		return (Config) getConfig();
	}

	/**
	 * Updates the builder-config
	 * 
	 * @param config
	 *        the new base-config
	 */
	public void setBaseConfig(InteractiveChartBuilder.Config config) {
		_baseConfig = config;
		_chartBuilder = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(_baseConfig);
		reset();
		invalidate();
	}

	/**
	 * The {@link InteractiveChartBuilder} created from {@link #getBaseConfig()}.
	 */
	protected InteractiveChartBuilder getChartBuilder() {
		return _chartBuilder;
	}

	/**
	 * the config of the {@link InteractiveChartBuilder} that describes the config-part of
	 *         the GUI
	 */
	protected InteractiveChartBuilder.Config getBaseConfig() {
		return _baseConfig;
	}

	@Override
	public boolean isModelValid() {
		return super.isModelValid() && chartModelValid();
	}

	@Override
	public boolean validateModel(DisplayContext dc) {
		boolean changes = super.validateModel(dc);

		if (!chartModelValid()) {
			initChartModel();
			changes = true;
		}

		return changes;
	}

	private boolean chartModelValid() {
		if (getSelected() != null) {
			return true;
		}
		return !hasData();
	}

	private boolean hasData() {
		if (_baseConfig == null) {
			return false;
		}
		JFreeChartBuilder<?> chartBuilder = _baseConfig.getChartBuilder();
		if (chartBuilder == null) {
			return false;
		}
		if (chartBuilder.getDatasetBuilder() == null) {
			return false;
		}
		return true;
	}

	private void initChartModel() {
		ChartConfig chartConfig = createChartConfig();
		setSelected(new ChartModel(chartConfig, getModel()));
	}

	/**
	 * Creates the actual {@link ChartConfig} from current UI values.
	 */
	public ChartConfig createChartConfig() {
		FormContext context = getFormContext();
		FormContainer chartGroup = context.getContainer(CHART_FIELD);
		return _chartBuilder.build(chartGroup);
	}

	@Override
	public FormContext createFormContext() {
		if (_baseConfig == null) {
			return null;
		}
	
		FormContext result = new FormContext(this);

		final FormGroup chart = new FormGroup(CHART_FIELD, I18NConstants.INTERACTIVE_CHART_BUILDER);
		result.addMember(chart);
		ChartContextObserver observer = new ChartContextObserver(getModel(), result);
		_chartBuilder.createUI(chart, observer);

		return result;
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		resetChart();
		ChartContextObserver observer = ChartContextObserver.getObserver(this);
		if (observer != null) {
			observer.updateModel(oldModel, newModel);
		}
	}

	/**
	 * {@link CommandHandler} applying settings and updating the chart.
	 */
	public static class ApplySettings extends AbstractCommandHandler {

		/**
		 * Creates a {@link ChartConfigComponent.ApplySettings} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public ApplySettings(InstantiationContext context, Config config) {
			super(context, config);
		}
	
		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
				Object model, Map<String, Object> arguments) {
			ChartConfigComponent configurator = (ChartConfigComponent) component;
			FormContext formContext = configurator.getFormContext();
			if (!formContext.checkAll()) {
				return AbstractApplyCommandHandler.createErrorResult(formContext);
			}
	
			configurator.resetChart();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
