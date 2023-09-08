/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.MemoryBinaryContent;
import com.top_logic.basic.io.binary.MemoryBinaryData;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.ImageField;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.reporting.flex.chart.component.export.ExportManager;
import com.top_logic.reporting.flex.chart.component.export.MultipleChartExportManager;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.tool.export.ExportAware;

/**
 * Displays multiple configured charts in a cockpit view.
 * 
 * <p>
 * Depending of the number of displayed charts, they are arranged in rows and columns
 * </p>
 * 
 * <p>
 * This component operates on a {@link MultiChartModel}.
 * </p>
 * 
 * @see AbstractChartComponent
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class MultipleChartComponent extends FormComponent implements ExportAware {

	/**
	 * <code>CHARTS</code>: Name of the form-group containing the chart-fields.
	 */
	public static final String CHARTS = "charts";

	/**
	 * Config-interface for {@link MultipleChartComponent}.
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * a list of chart-configs to display in this component
		 */
		public List<ChartConfig> getCharts();

		/**
		 * the {@link ExportManager} for custom exports
		 */
		@InstanceFormat
		@InstanceDefault(MultipleChartExportManager.class)
		public ExportManager getExportManager();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			getExportManager().registerExportCommand(registry);
		}
	}

	private static final String CHART_PREFIX = "chart_";

	/**
	 * Config-Constructor for {@link MultipleChartComponent}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public MultipleChartComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);
		setCharts(config().getCharts());
	}

	/**
	 * Updates the list of charts to display and invalidates the GUI.
	 * 
	 * @param charts
	 *        the new charts to be displayed
	 */
	public void setCharts(MultiChartModel charts) {
		setModel(charts);
		removeFormContext();
		invalidate();
	}

	/**
	 * See {@link #setCharts(MultiChartModel)}
	 */
	public void setCharts(List<ChartConfig> charts) {
		setCharts(new MultiChartModel(charts));
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof MultiChartModel;
	}

	List<ChartConfig> getCharts() {
		if (getModel() instanceof MultiChartModel) {
			return ((MultiChartModel) getModel())._charts;
		}
		return Collections.emptyList();
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		removeFormContext();
	}

	@Override
	public FormContext createFormContext() {
		FormContext result = new FormContext(this);
		FormGroup group = new FormGroup(CHARTS, getResPrefix());

		List<ChartConfig> charts = getCharts();
		int count = charts.size();
		Templates.template(group, createTemplate(count));

		int i = 1;
		for (ChartConfig conf : charts) {
			ImageField field = ChartConfigImageProvider.newImageField(CHART_PREFIX + i++, conf, null, this);
			group.addMember(field);
		}

		result.addMember(group);
		return result;
	}

	/**
	 * the image-fields used to display the charts described by the current model
	 */
	public List<ImageField> getChartFields() {
		if (!hasFormContext()) {
			return Collections.emptyList();
		}
		List<ImageField> result = new ArrayList<>();
		FormContext context = getFormContext();
		FormContainer group = context.getContainer(CHARTS);
		for (int i = 1; i <= getCharts().size(); i++) {
			ImageField field = (ImageField) group.getMember(CHART_PREFIX + i);
			result.add(field);
		}
		return result;
	}

	private List<JFreeChart> getChartsForExport() {
		if (!hasFormContext()) {
			return Collections.emptyList();
		}
		List<JFreeChart> result = new ArrayList<>();
		for (ImageField field : getChartFields()) {
			ChartConfigImageProvider provider = (ChartConfigImageProvider) field.getImageComponent();
			JFreeChart chart = provider.createChart();
			result.add(chart);
		}
		return result;
	}

	public List<BinaryContent> getContentForExport() {
		List<JFreeChart> charts = getChartsForExport();
		if (charts.isEmpty()) {
			return Collections.emptyList();
		}
		List<BinaryContent> result = new ArrayList<>();
		int i = 1;
		Dimension dimension = getExportDimension();
		for (JFreeChart chart : charts) {
			result.add(writeContent(chart, CHART_PREFIX + i++, dimension));
		}
		return result;
	}

	private Dimension getExportDimension() {
		int width = 900;
		int height = 560;
		return new Dimension(width, height);
	}

	private HTMLTemplateFragment createTemplate(int count) {
		if (count == 1) {
			return div(member(CHART_PREFIX + "1"));
		}
		else if (count == 2) {
			return div(css("multiChartContainer"),
				div(css("multiChartRow"),
					member(CHART_PREFIX + "1")
				),
				div(css("multiChartRow"),
					member(CHART_PREFIX + "2")
				)
				);
		}
		else {
			return div(css("multiChartContainer"),
				rows(1, count)
			);
		}
	}

	private HTMLTemplateFragment rows(int i, int count) {
		List<HTMLTemplateFragment> contents = new ArrayList<>();
		for (; i <= count; i += 2) {
			contents.add(row(i, count));
		}
		return fragment(contents);
	}

	private HTMLTemplateFragment row(int i, int count) {
		return div(css("multiChartRow"),
			field(i++, count),
			field(i++, count));
	}

	private HTMLTemplateFragment field(int i, int count) {
		if (i <= count) {
			return div(css("multiChartColumn"),
				member(CHART_PREFIX + i));
		} else {
			return empty();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments) {
		return exportManager().getExportValues(progressInfo, arguments, this);
	}

	private ExportManager exportManager() {
		return config().getExportManager();
	}

	private Config config() {
		return (Config) getConfig();
	}

	/**
	 * Utility method to write a chart into a {@link MemoryBinaryData}.
	 * 
	 * @param chart
	 *        the chart to write into memory-content
	 * @param name
	 *        of the content for debug-reasons
	 * @param dimension
	 *        the size of the chart
	 * @return a new MemoryBinaryContent containing the chart-image
	 */
	public static MemoryBinaryContent writeContent(JFreeChart chart, String name, Dimension dimension) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ChartUtil.writeChartAsPng(out, chart, dimension, null);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return new MemoryBinaryContent(out.toByteArray(), name);
	}

}
