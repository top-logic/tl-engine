/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.reporting.common.tree.TreeAxis;
import com.top_logic.reporting.common.tree.TreeInfo;
import com.top_logic.reporting.flex.chart.component.AbstractChartComponent;
import com.top_logic.reporting.flex.chart.component.handler.ImagePostHandler;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon.LabelIconProvider;
import com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon.ValueIconProvider;
import com.top_logic.reporting.flex.chart.config.dataset.DatasetBuilder;
import com.top_logic.reporting.flex.chart.config.dataset.TimeseriesDatasetBuilder;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;
import com.top_logic.reporting.flex.chart.config.tooltip.DefaultTooltipGenerator;
import com.top_logic.reporting.flex.chart.config.tooltip.XYToolTipGeneratorProvider;
import com.top_logic.reporting.flex.chart.config.url.XYURLGeneratorProvider;

/**
 * {@link AbstractJFreeChartBuilder} that builds timeseries-charts with a {@link TreeAxis}.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class TreeAxisTimeSeriesChartBuilder extends AbstractJFreeChartBuilder<AbstractXYDataset> {

	/**
	 * Config-interface for {@link TreeAxisTimeSeriesChartBuilder}.
	 */
	public interface Config extends JFreeChartBuilder.Config<AbstractXYDataset> {

		@Override
		@InstanceFormat
		@InstanceDefault(TreeAxisTimeseriesDatasetBuilder.class)
		public DatasetBuilder<? extends AbstractXYDataset> getDatasetBuilder();

		/**
		 * the provider for the icons of the labels in the tree-axis
		 */
		@InstanceFormat
		public LabelIconProvider getLabelIconProvider();

		/**
		 * the provider for the icons used to visualize the values in the chart
		 */
		@InstanceFormat
		public ValueIconProvider getValueIconProvider();

		@Override
		public XYURLGeneratorProvider getURLGeneratorProvider();

		/**
		 * Factory for a {@link XYToolTipGenerator}.
		 */
		@InstanceFormat
		@InstanceDefault(DefaultTooltipGenerator.Provider.class)
		public XYToolTipGeneratorProvider getTooltipGeneratorProvider();
	}

	/**
	 * Config-Constructor for {@link TreeAxisTimeSeriesChartBuilder}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public TreeAxisTimeSeriesChartBuilder(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);
	}

	@Override
	public Class<AbstractXYDataset> datasetType() {
		return AbstractXYDataset.class;
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected JFreeChart internalCreateChart(ChartContext context, ChartData<AbstractXYDataset> chartData) {
		LabelProvider labelProvider = getLabelProvider(0);
		TimeSeriesCollection dataset = (TimeSeriesCollection) chartData.getDataset();
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			Comparable<?> key = dataset.getSeriesKey(i);
			if (key instanceof UniqueName) {
				((UniqueName) key).setProvider(labelProvider);
			}
		}
		JFreeChart result = ChartFactory.createTimeSeriesChart(getTitle(), getXAxisLabel(),
			getYAxisLabel(), dataset, getConfig().getShowLegend(),
			false, false);
		return result;
	}

	@Override
	protected void adaptChart(JFreeChart model, ChartContext context, ChartData<AbstractXYDataset> chartData) {

		XYLineAndShapeRenderer renderer = getRenderer(chartData.getModel());

		XYPlot plot = (XYPlot) model.getPlot();

		plot.setDomainGridlinesVisible(false);
		plot.setRenderer(renderer);

		renderer.setDefaultShapesVisible(true);
		renderer.setDefaultShapesFilled(true);
		renderer.setDefaultLinesVisible(false);

	}

	private XYLineAndShapeRenderer getRenderer(final ChartTree chartTree) {

		return new XYLineAndShapeRenderer() {
			@Override
			protected void drawSecondaryPass(Graphics2D g2, XYPlot plot, XYDataset dataset, int pass, int series,
					int item, ValueAxis domainAxis, Rectangle2D dataArea, ValueAxis rangeAxis,
					CrosshairState crosshairState, EntityCollection entities) {

				Shape entityArea = null;

				/* Get the data point */
				double x1 = dataset.getXValue(series, item);
				double y1 = dataset.getYValue(series, item);
				if (Double.isNaN(y1) || Double.isNaN(x1)) {
					return;
				}

				PlotOrientation orientation = plot.getOrientation();
				RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
				RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
				double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
				double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

				if (getItemShapeVisible(series, item)) {

					/* Get the image for this item. */
					Image image = null;
					if (dataset instanceof TimeSeriesCollection) {

						DataKey dataKey =
							TimeseriesDatasetBuilder.toDataKey((TimeSeriesCollection) dataset, series, item);
						ChartNode node = chartTree.getNode(dataKey);
						image = getConfig().getValueIconProvider().getIcon(node);
					}

					if (image == null) {
						return;
					}

					int width = image.getWidth(null);
					int height = image.getHeight(null);
					Shape shape = new Ellipse2D.Double(0, 0, width, height);

					if (orientation == PlotOrientation.HORIZONTAL) {
						shape = ShapeUtils.createTranslatedShape(shape, transY1, transX1);
					} else if (orientation == PlotOrientation.VERTICAL) {
						shape = ShapeUtils.createTranslatedShape(shape, transX1, transY1);
					}

					/* Draw the image for the item and set the shape for the image map. */
					if (shape.intersects(dataArea)) {
						int wHalf = width / 2;
						int hHalf = height / 2;
						int x = (int) shape.getBounds().getX() - wHalf;
						int y = (int) shape.getBounds().getY() - hHalf;
						entityArea = new Ellipse2D.Double(x, y, width, height);
						g2.drawImage(image, x, y, width, height, null);
					}
				}

				/* Draw the item label. */
				if (isItemLabelVisible(series, item)) {
					double x = transX1;
					double y = transY1;
					if (orientation == PlotOrientation.HORIZONTAL) {
						x = transY1;
						y = transX1;
					}
					drawItemLabel(g2, orientation, dataset, series, item, x, y, (y1 < 0.0));
				}

				updateCrosshairValues(crosshairState, x1, y1, series, transX1, transY1, plot.getOrientation());

				/* Add the entity for the item. */
				if (entities != null) {
					addEntity(entities, entityArea, dataset, series, item, transX1, transY1);
				}

			}

		};
	}

	@Override
	public void modifyPlot(JFreeChart model, ChartContext context, ChartData<AbstractXYDataset> chartData) {
		super.modifyPlot(model, context, chartData);

		TreeInfo[] infos = (TreeInfo[]) ArrayUtil.toArray(chartData.getModel().getRootObjects());
		TreeAxis axis = new TreeAxis(infos, getIcons());

		axis.setIconLabelDistance(3);
		axis.setTreeIconsDistance(15);
		axis.setGridBandsVisible(false);
		axis.setTickMarksVisible(false);

		XYPlot plot = (XYPlot) model.getPlot();
		plot.setRangeAxis(axis);

	}

	private Image[] getIcons() {
		return getConfig().getLabelIconProvider().getIcons();
	}

	@Override
	protected void setTooltipGenerator(JFreeChart model, ChartContext context, ChartData<AbstractXYDataset> chartData) {
		XYPlot plot = (XYPlot) model.getPlot();
		XYToolTipGenerator generator = getConfig().getTooltipGeneratorProvider().getXYTooltipGenerator(model, context, chartData);
		plot.getRenderer().setDefaultToolTipGenerator(generator);
	}

	@Override
	protected void setUrlGenerator(JFreeChart model, ChartContext context, ChartData<AbstractXYDataset> chartData) {
		XYPlot plot = (XYPlot) model.getPlot();
		XYURLGenerator generator = getConfig().getURLGeneratorProvider().getXYURLGenerator(model, context, chartData);
		plot.getRenderer().setURLGenerator(generator);
	}

	@Override
	public int getMaxDimensions() {
		return 2;
	}

	@Override
	public int getMinDimensions() {
		return 2;
	}

	/**
	 * {@link ImagePostHandler} that modifies the height of the chart according to the
	 * {@link TreeAxis}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static class TreeAxisImagePostHandler implements ImagePostHandler {

		@SuppressWarnings("rawtypes")
		@Override
		public void prepareImage(AbstractChartComponent comp, DisplayContext aContext, String anImageId,
				Dimension aDimension, Map dimensions, String aKey) {

			/* The wish height from the jsp */
			Dimension dimension = (Dimension) dimensions.get(aKey);

			int width = (int) dimension.getWidth() - 20;

			/* Calculate the minimum and maximum height for the chart */
			XYPlot plot = (XYPlot) comp.getChart().getPlot();
			TreeAxis treeAxis = (TreeAxis) plot.getRangeAxis();

			int nodeCount = treeAxis.getInfos().length;
			int maxIconHeight = getMaxImageHeight(treeAxis.getIcons());

			// calculate the height needed to display the graphic
			// consists of number of nodes PLUS a height for the labels of the x-axis
			int neededHeight = (nodeCount * maxIconHeight) + ((nodeCount - 1) * 25) + 100;

			/* Set the new height and width to the dimension */
			dimension.setSize(Math.max(width, 150), neededHeight);

		}

		@Override
		public void postSetConfig(AbstractChartComponent configuredChartComponent) {
			// do nothing
		}

		/**
		 * Copied from ReportUtils.getMaxImageHeight because ReportUtils is located in risk-module
		 * and not accessable from reporting.
		 */
		private int getMaxImageHeight(Image[] someImages) {
			int theMaxHeight = 0;

			for (int i = 0; i < someImages.length; i++) {
				Image theImage = someImages[i];

				theMaxHeight = Math.max(theImage.getHeight(null), theMaxHeight);
			}

			return theMaxHeight;
		}
	}

	/**
	 * Specific {@link TimeseriesDatasetBuilder} for {@link TreeAxisTimeSeriesChartBuilder}.
	 */
	public static class TreeAxisTimeseriesDatasetBuilder extends TimeseriesDatasetBuilder {

		/**
		 * Creates a new {@link TreeAxisTimeseriesDatasetBuilder}.
		 */
		public TreeAxisTimeseriesDatasetBuilder(InstantiationContext context, Config config) {
			super(context, config);
		}

		/**
		 * The {@link TreeAxisTimeSeriesChartBuilder} consists of at least two partition levels, so
		 * that it already must be treated as empty if it has a depth of one, only.
		 */
		@Override
		protected boolean isEmpty(ChartTree tree) {
			return tree.getDepth() <= 1;
		}

	}

}
