/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import java.util.List;
import java.util.Set;

import org.jfree.data.UnknownKeyException;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.SetBuilder;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.dataset.CategoryDatasetBuilder.CategoryDataKey;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.AbstractDataKey;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;
import com.top_logic.reporting.flex.chart.config.partition.CoordinatePartition.Coordinate;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ClassificationCriterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.CoordinateCriterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ValueCriterion;

/**
 * Dataset-builder for {@link XYSeriesCollection}
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class XYSeriesBuilder extends AbstractDatasetBuilder<XYSeriesCollection> {

	/**
	 * Config-interface for {@link XYSeriesBuilder}.
	 */
	public interface Config extends AbstractDatasetBuilder.Config {

		@Override
		@ClassDefault(XYSeriesBuilder.class)
		public Class<XYSeriesBuilder> getImplementationClass();
	}

	/**
	 * Config-Constructor for {@link XYSeriesBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public XYSeriesBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public Class<XYSeriesCollection> getDatasetType() {
		return XYSeriesCollection.class;
	}

	@Override
	protected XYSeriesCollection internalCreateDataset(ChartTree tree) {
		int posX = tree.getCriterionIndex(CoordinateCriterion.class, Coordinate.X);
		int posY = tree.getCriterionIndex(CoordinateCriterion.class, Coordinate.Y);
		int posCat = getCategoryPos(tree, posX, posY);
		Object[] keys = new Object[tree.getDepth()];
		XYSeriesCollection result = new XYSeriesCollection();
		parseChildren(result, tree.getRoot().getChildren(), keys, 0, posX, posY, posCat);
		return result;
	}

	private int getCategoryPos(ChartTree tree, int posX, int posY) {
		int posCat = 0;
		if (tree.getDepth() == 3) {
			Set<Integer> positions = new SetBuilder<Integer>(3).add(0).add(1).add(2).toSet();
			positions.remove(posX);
			positions.remove(posY);
			posCat = CollectionUtil.getFirst(positions);
		}
		else {
			posCat = tree.getCriterionIndex(ClassificationCriterion.class);
			if (posCat == -1) {
				posCat = tree.getCriterionIndex(ValueCriterion.class);
			}
		}
		return posCat;
	}

	private void parseChildren(XYSeriesCollection aResultSet, List<ChartNode> children,
			Object[] keys, int level, int posX, int posY, int posCat) {

		for (int i = 0; i < children.size(); i++) {
			ChartNode child = children.get(i);
			keys[level] = child.getKey();

			if (child.isLeaf()) {
				Double xVal = (Double) keys[posX];
				Double yVal = (Double) keys[posY];
				XYSeries series = getOrCreateSeries(aResultSet, keys, posCat);
				series.add(xVal, yVal);
				String id = child.getID();
				DataKey dataKey = createDataKey((UniqueName) series.getKey(), xVal);
				child.getTree().registerDataKey(dataKey, id);
			}
			else {
				List<ChartNode> subChildren = child.getChildren();
				parseChildren(aResultSet, subChildren, keys, level + 1, posX, posY, posCat);
			}
		}
	}

	/**
	 * {@link DataKey}-implementation used for {@link XYDataset}
	 */
	public static class XYSeriesDataKey extends AbstractDataKey<UniqueName, Double> {

		/**
		 * Creates a new {@link CategoryDataKey}
		 * 
		 * @param seriesKey
		 *        the key indicating the series
		 * @param xVal
		 *        the value on the x-axis
		 */
		public XYSeriesDataKey(UniqueName seriesKey, Double xVal) {
			super(seriesKey, xVal);
		}

		/**
		 * The key of this instance.
		 */
		public final UniqueName getKey() {
			return getFirst();
		}

	}

	/**
	 * a new {@link XYSeriesDataKey} for the given series and x-value.
	 */
	public static XYSeriesDataKey createDataKey(UniqueName seriesKey, Double xVal) {
		return new XYSeriesDataKey(seriesKey, xVal);
	}

	/**
	 * a new {@link XYSeriesDataKey} identifying the data represented in the given dataset
	 *         by the given series and item.
	 */
	public static XYSeriesDataKey toDataKey(XYDataset dataset, int series, int item) {
		UniqueName seriesKey = (UniqueName) dataset.getSeriesKey(series);
		Number x = dataset.getX(series, item);
		return XYSeriesBuilder.createDataKey(seriesKey, x.doubleValue());
	}

	private XYSeries getOrCreateSeries(XYSeriesCollection aResultSet, Object[] keys, int posCat) {
		Comparable<?> comp = getCategory(keys, posCat);
		XYSeries series = null;
		try {
			series = aResultSet.getSeries(comp);
		} catch (UnknownKeyException ex) {
			// ignore - new series will be created.
		}
		if (series == null) {
			series = new XYSeries(comp);
			aResultSet.addSeries(series);
		}
		return series;
	}

	private Comparable<?> getCategory(Object[] keys, int posCat) {
		Object key = keys[posCat];
		if (!(key instanceof UniqueName)) {
			key = new UniqueName((Comparable<?>) key);
			keys[posCat] = key;
		}
		return (Comparable<?>) key;
	}

}
