/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import java.awt.Dimension;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.base.chart.ImageControl;
import com.top_logic.base.chart.component.JFreeChartComponent;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LinkGenerator;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.CommandHandlerCommand;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.component.Updatable;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.chart.component.export.ExportManager;
import com.top_logic.reporting.flex.chart.component.export.NoExportManager;
import com.top_logic.reporting.flex.chart.component.handler.ImagePostHandler;
import com.top_logic.reporting.flex.chart.component.handler.NoPostHandler;
import com.top_logic.reporting.flex.chart.component.handler.NoPostPreparationHandler;
import com.top_logic.reporting.flex.chart.component.handler.OpenChartDetailsCommand;
import com.top_logic.reporting.flex.chart.component.handler.PostPreparationHandler;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.datasource.ComponentDataContext;
import com.top_logic.reporting.flex.chart.config.datasource.DataContext;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ModelPreparation;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.tool.export.ExportAware;
/**
 * {@link JFreeChartComponent} to display a configured chart.
 * 
 * <p>
 * This component operates on a {@link ChartModel} model that is typically received from a
 * {@link ChartConfigComponent}.
 * </p>
 * 
 * @see MultipleChartComponent
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public abstract class AbstractChartComponent extends JFreeChartComponent implements ExportAware, ComponentDataContext,
		ChartContext, Updatable {

	/**
	 * Configuration options for {@link AbstractChartComponent}.
	 */
	public interface Config extends JFreeChartComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * See {@link ImagePostHandler}
		 * 
		 * @return the ImagePostHandler for this chart. Must not be null.
		 */
		@InstanceFormat
		@InstanceDefault(NoPostHandler.class)
		public ImagePostHandler getImagePostHandler();

		/**
		 * See {@link ExportManager}
		 * 
		 * @return the export-manager for this chart. Must not be null.
		 */
		@InstanceFormat
		@InstanceDefault(NoExportManager.class)
		public ExportManager getExportManager();

		/**
		 * See {@link PostPreparationHandler}
		 * 
		 * @return the post-preparation-manager configured for this chart-component. Must not be
		 *         null.
		 */
		@InstanceFormat
		@InstanceDefault(NoPostPreparationHandler.class)
		public PostPreparationHandler getPostPreparationHandler();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			JFreeChartComponent.Config.super.modifyIntrinsicCommands(registry);
			getExportManager().registerExportCommand(registry);
		}

	}

	private Dataset _dataSet;

	private JFreeChart _chart;

	private ChartTree _chartTree;

	private ChartModel _chartModel;

	private boolean _chartValid;

	/**
	 * Creates a new {@link AbstractChartComponent}
	 */
	public AbstractChartComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public boolean isModelValid() {
		return super.isModelValid() && isChartValid();
	}

	private boolean isChartValid() {
		return _chartValid && (getChartModel() == null || _chart != null);
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean res = super.validateModel(context);
		initChart();
		return res;
	}

	private void initChart() {
		if (!_chartValid) {
			_chartValid = true;
			rebuildChartModel();
		}

		if (getChartModel() == null) {
			return;
		}

		initChartData();

		@SuppressWarnings("unchecked")
		JFreeChartBuilder<Dataset> chartBuilder = getChartModel().getConfig().getChartBuilder();

		_chart = chartBuilder.createChart(this, new ChartData<>(_chartTree, _dataSet));

		imagePostHandler().postSetConfig(this);
	}

	@Override
	public String getUrl(String path) {
		if (!StringServices.isEmpty(path)) {
			CommandHandler handler = getDetailsHandler();
			if (handler != null) {
				Map<String, Object> args = new HashMap<>();
				args.put(OpenChartDetailsCommand.PARAMETER_ID, path);

				CommandHandlerCommand executingCommand = new CommandHandlerCommand(getDetailsHandler(), this, args);
				return LinkGenerator.createLink(DefaultDisplayContext.getDisplayContext(), executingCommand);
			}
		}
		return null;
	}

	/**
	 * {@link CommandHandler} that is called, when chart details should be opened.
	 */
	protected CommandHandler getDetailsHandler() {
		return getCommandById(OpenChartDetailsCommand.COMMAND_ID);
	}

	/**
	 * The model of this component.
	 * 
	 * @see #getModel()
	 */
	protected final ChartModel getChartModel() {
		return _chartModel;
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		update();
	}

	/**
	 * Trigger an rebuild of the current chart.
	 */
	protected void resetChart() {
		_chartValid = false;
		_chartTree = null;
		_dataSet = null;
		_chart = null;
		requestRepaint();
	}

	@Override
	protected void resetCaches() {
		super.resetCaches();
		resetChart();
	}

	@Override
	protected void handleNewModel(Object newModel) {
		// No not invalidate, use incremental update.
	}

	@Override
	public final void update() {
		resetChart();
	}


	private void rebuildChartModel() {
		_chartModel = createChartModel(getModel());
	}

	/**
	 * Transforms the given component's {@link #getModel() model} into the {@link ChartModel} to
	 * display.
	 */
	protected abstract ChartModel createChartModel(Object newModel);

	/**
	 * Triggers a repaint of the chart.
	 */
	protected void requestRepaint() {
		Control control = getControl(getName() + "chart");
		if (control instanceof ImageControl) {
			((ImageControl) control).update();
		} else if (control != null) {
			((AbstractControl) control).requestRepaint();
		} else {
			invalidate();
		}
	}

	@Override
	protected Dataset createDataSet(String anImageId) {
		initChartData();
		return _dataSet;
	}

	private void initChartData() {
		if (_dataSet == null) {
			Collection<? extends Object> rawData = getRawData();

			ChartModel chartModel = getChartModel();
			ModelPreparation preparation = chartModel.getConfig().getModelPreparation();
			_chartTree = preparation.prepare(rawData);

			_dataSet = chartModel.getConfig().getChartBuilder().getDatasetBuilder().getDataset(_chartTree);
			postPreparationHandler().onChartPrepared(this, _dataSet, _chartTree);
		}
	}

	/**
	 * the raw-data as provided by the configured data-source
	 */
	protected Collection<? extends Object> getRawData() {
		@SuppressWarnings("unchecked")
		ChartDataSource<DataContext> dataSource = getChartModel().getConfig().getDataSource();

		return dataSource.getRawData(this);
	}

	@Override
	public LayoutComponent getComponent() {
		return this;
	}

	@Override
	protected final JFreeChart createChart(String anImageId) {
		return getChart();
	}

	@Override
	public final JFreeChart getChart() {
		initChart();
		return _chart;
	}

	@Override
	protected void customizeChart(String anImageId, JFreeChart aChart) {
		// Nothing in here, will be done by AbstractChartProducer
	}

	@Override
	public BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return fallbackCurrentObject(super.getSecurityObject(commandGroup, potentialModel));
	}

	/**
	 * Most likely the security should not be checked based on the chart-configuration that is used
	 * as model here but on the master-model. If the current model is null, this method tries to
	 * retrieve the master-model.
	 */
	private BoundObject fallbackCurrentObject(BoundObject currentObject) {
		if (currentObject == null) {
			ChartModel chartModel = getChartModel();
			if (chartModel != null && chartModel.getModel() instanceof BoundObject) {
				return (BoundObject) chartModel.getModel();
			}
		}
		return currentObject;
	}

	/**
	 * Checks if the list contains only one element and tries to show the single object via
	 * {@link GotoHandler}.
	 * 
	 * @param aComponent
	 *        the component for command-execution
	 * @param values
	 *        the list of objects to display
	 * @return true if the list contains only one object and it there is a default-layout for this
	 */
	public static boolean showSingleObject(LayoutComponent aComponent, List<?> values) {
		boolean result = false;
		if (values.size() == 1) {
			Object object = CollectionUtil.getSingleValueFromCollection(values);

			if (GotoHandler.canShow(object)) {
				result = aComponent.gotoTarget(object);
			}
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments) {
		return exportManager().getExportValues(progressInfo, arguments, this);
	}

	@Override
	public void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException {
		super.prepareImage(context, imageId, dimension);
		ImagePostHandler handler = imagePostHandler();
		handler.prepareImage(this, context, imageId, dimension, dimensions, getKey(imageId));
	}

	/**
	 * The current generic chart data for the current {@link #getChartModel()}.
	 */
	public final ChartTree getChartTree() {
		initChartData();
		return _chartTree;
	}

	private PostPreparationHandler postPreparationHandler() {
		return config().getPostPreparationHandler();
	}

	private ImagePostHandler imagePostHandler() {
		return config().getImagePostHandler();
	}

	private ExportManager exportManager() {
		return config().getExportManager();
	}

	private Config config() {
		return (Config) getConfig();
	}

}
