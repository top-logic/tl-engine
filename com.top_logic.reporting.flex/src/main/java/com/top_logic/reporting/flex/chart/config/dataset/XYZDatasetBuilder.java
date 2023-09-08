/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.dataset.XYSeriesBuilder.XYSeriesDataKey;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;
import com.top_logic.reporting.flex.chart.config.partition.CoordinatePartition.Coordinate;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.CoordinateCriterion;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class XYZDatasetBuilder extends AbstractDatasetBuilder<XYZDataset> {

	/**
	 * Defines how the size of the bubbles is scaled with respect to the axes:
	 * XAXIS means that the size of the bubbles are at most getMaxScale() % of the range of the xAxis
	 * YAXIS means that the size of the bubbles are at most getMaxScale() % of the range of the yAxis
	 * BOTH means that the size of the bubbles are at most getMaxScale() % of the min(range of xAxis, range of yAxis)
	 */
	public enum ScaleType {
		XAXIS, YAXIS, BOTH;
	}

	/**
	 * Config-interface for {@link XYSeriesBuilder}.
	 */
	public interface Config extends AbstractDatasetBuilder.Config {

		@Override
		@ClassDefault(XYZDatasetBuilder.class)
		public Class<XYZDatasetBuilder> getImplementationClass();

		public ResKey getSeriesName();

		@FormattedDefault("BOTH")
		public ScaleType getScaleType();

		/**
		 * the maximum size in % of the scale axis which is used as diameter for a bubble
		 */
		@IntDefault(20)
		public int getMaxScale();
	}

	/**
	 * Config-Constructor for {@link XYZDatasetBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public XYZDatasetBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public Class<XYZDataset> getDatasetType() {
		return XYZDataset.class;
	}

	@Override
	protected XYZDataset internalCreateDataset(ChartTree tree) {
		String seriesName = Resources.getInstance().getString(getConfig().getSeriesName());

		int posX = tree.getCriterionIndex(CoordinateCriterion.class, Coordinate.X);
		int posY = tree.getCriterionIndex(CoordinateCriterion.class, Coordinate.Y);
		int posZ = tree.getCriterionIndex(CoordinateCriterion.class, Coordinate.Z);

		List<XYZValue> values = new ArrayList<>();

		Object[] keys = new Object[tree.getDepth()];
		DefaultXYZDataset result = new DefaultXYZDataset();
		parseChildren(values, tree.getRoot().getChildren(), keys, 0, posX, posY, posZ);

		double[][] data = scaleData(values);

		for (int i = 0; i < values.size(); i++) {
			data[0][i] = values.get(i).getX();
			data[1][i] = values.get(i).getY();
			data[2][i] = values.get(i).getZ();
		}

		UniqueName seriesKey = getUniqueName(StringServices.nonNull(seriesName));
		result.addSeries(seriesKey, data);

		// save keys for tooltips
		for (int i = 0; i < values.size(); i++) {
			DataKey key = createDataKey(seriesKey, i);
			tree.registerDataKey(key, values.get(i).getNode().getID());
		}

		return result;
	}

	private double[][] scaleData(List<XYZValue> values) {
		double data[][] = new double[3][values.size()];

		// data must be normalized
		// do it here for y-axis

		// calculate yRange
		// calculate max of z-values
		double xMin = Double.MAX_VALUE;
		double xMax = Double.MIN_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = Double.MIN_VALUE;
		double zMax = Double.MIN_VALUE;
		double zMin = Double.MAX_VALUE;
		for (XYZValue value : values) {

			double x = value.getX();
			if (x > xMax) {
				xMax = x;
			}
			if (x < xMin) {
				xMin = x;
			}

			double y = value.getY();
			if (y > yMax) {
				yMax = y;
			}
			if (y < yMin) {
				yMin = y;
			}

			double z = value.getZ();
			if (z > zMax) {
				zMax = z;
			}
			if (z < zMin) {
				zMin = z;
			}
		}


		double xRange = xMax - xMin;
		double yRange = yMax - yMin;

		double relevantRange = 0;
		switch (getConfig().getScaleType()) {
			case XAXIS:
				relevantRange = xRange;
				break;
			case YAXIS:
				relevantRange = yRange;
				break;
			case BOTH:
				relevantRange = Math.min(xRange, yRange);
				break;
		}

		// calculate scaled values
		// maxSacle is the percentage of the size of a bubble according to the relevant axis
		double maxZValue = relevantRange / 100 * getConfig().getMaxScale();
		// change data
		for (XYZValue value : values) {
			double z = value.getZ();
			double newZ = z / zMax * maxZValue;
			value.setZ(newZ);
		}
		return data;
	}

	/**
	 * a new {@link XYSeriesDataKey} for the given series and x-value.
	 */
	public static XYSeriesDataKey createDataKey(UniqueName seriesKey, int xVal) {
		return new XYSeriesDataKey(seriesKey, (double) xVal);
	}

	/**
	 * a new {@link XYSeriesDataKey} identifying the data represented in the given dataset
	 *         by the given series and item.
	 */
	public static XYSeriesDataKey toDataKey(XYDataset dataset, int series, int item) {
		UniqueName seriesKey = (UniqueName) dataset.getSeriesKey(series);
		return createDataKey(seriesKey, item);
	}

	private void parseChildren(List<XYZValue> values, List<ChartNode> children, Object[] keys, int level, int posX,
			int posY, int posZ) {

		for (int i = 0; i < children.size(); i++) {
			ChartNode child = children.get(i);

			// key is also the value
			keys[level] = child.getKey();

			if (child.isLeaf()) {
				Double xVal = (Double) keys[posX];
				Double yVal = (Double) keys[posY];
				Double zVal = (Double) keys[posZ];
				values.add(new XYZValue(xVal, yVal, zVal, child));
			} else {
				List<ChartNode> subChildren = child.getChildren();
				parseChildren(values, subChildren, keys, level + 1, posX, posY, posZ);
			}
		}
	}

	private static class XYZValue {

		private Double _xVal;

		private Double _yVal;

		private Double _zVal;

		private ChartNode _node;

		public XYZValue(Double xVal, Double yVal, Double zVal, ChartNode node) {
			_xVal = xVal;
			_yVal = yVal;
			_zVal = zVal;
			_node = node;
		}

		public void setZ(double newZ) {
			_zVal = newZ;
		}

		public double getZ() {
			return _zVal == null ? 0 : _zVal;
		}

		public double getY() {
			return _yVal == null ? 0 : _yVal;
		}

		public double getX() {
			return _xVal == null ? 0 : _xVal;
		}

		public ChartNode getNode() {
			return _node;
		}

	}


}
