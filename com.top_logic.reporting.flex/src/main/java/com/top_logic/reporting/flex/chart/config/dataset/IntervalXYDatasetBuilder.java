/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import java.util.Date;
import java.util.List;

import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeriesCollection;

import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.chart.config.UniqueName;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;

/**
 * Dataset-builder for {@link XYSeriesCollection}
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class IntervalXYDatasetBuilder extends AbstractDatasetBuilder<IntervalXYDataset> {

	/**
	 * Config-interface for {@link IntervalXYDatasetBuilder}.
	 */
	public interface Config extends AbstractDatasetBuilder.Config {

		@Override
		@ClassDefault(IntervalXYDatasetBuilder.class)
		public Class<IntervalXYDatasetBuilder> getImplementationClass();

		@StringDefault("start")
		public String getStartAttribute();

		@StringDefault("end")
		public String getEndAttribute();
	}

	/**
	 * Config-Constructor for {@link IntervalXYDatasetBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public IntervalXYDatasetBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public Class<IntervalXYDataset> getDatasetType() {
		return IntervalXYDataset.class;
	}

	@Override
	protected IntervalXYDataset internalCreateDataset(ChartTree tree) {
		XYIntervalSeriesCollection result = new XYIntervalSeriesCollection();
		int depth = tree.getDepth();

		Object[] keys = new Object[depth];
		parseChildren(result, tree.getRoot().getChildren(), keys, 0);

		return result;
	}

	private void parseChildren(XYIntervalSeriesCollection result, List<ChartNode> children, Object[] keys, int level) {

		for (int i = 0; i < children.size(); i++) {
			ChartNode child = children.get(i);
			keys[level] = child.getKey();

			if (child.isLeaf()) {
				Comparable key = createKey(keys);
				XYIntervalSeries series = createSeries(result, key);
				List<?> objects = child.getObjects();
				for (Object object : objects) {
					Object start = start(object);
					Object end = end(object);
					if (start != null && end != null) {
						addItem(result, series, start, end);
					}
				}
				String id = child.getID();
				int index = result.getSeriesCount() - 1;
				DataKey dataKey = XYSeriesBuilder.createDataKey((UniqueName) series.getKey(), Double.valueOf(index));
				child.getTree().registerDataKey(dataKey, id);
			} else {
				List<ChartNode> subChildren = child.getChildren();
				parseChildren(result, subChildren, keys, level + 1);
			}
		}
	}

	private void addItem(XYIntervalSeriesCollection coll, XYIntervalSeries series, Object start, Object end) {
		int index = coll.getSeriesCount() - 1;
		double startValue = periodStart(start);
		double endValue = periodEnd(end);
		series.add(index, index - 0.25, index + 0.25, startValue, startValue, endValue);
	}

	private double periodStart(Object value) {
		return toDouble(value);
	}

	private double periodEnd(Object value) {
		return toDouble(value);
	}

	private double toDouble(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		if (value instanceof Date) {
			return ((Date) value).getTime();
		}
		return 0.0;
	}

	private Object start(Object object) {
		return period(object, getConfig().getStartAttribute());
	}

	private Object end(Object object) {
		return period(object, getConfig().getEndAttribute());
	}

	private Object period(Object object, String attr) {
		if (object instanceof TLObject) {
			return ((TLObject) object).tValueByName(attr);
		}
		return null;
	}

	private Comparable createKey(Object[] keys) {
		UniqueName[] names = new UniqueName[keys.length];
		for (int i = 0; i < keys.length; i++) {
			names[i] = getUniqueName((Comparable<?>) keys[i]);
		}
		return getUniqueName(TupleFactory.newTupleCopy(names));
	}

	private XYIntervalSeries createSeries(XYIntervalSeriesCollection result, Comparable key) {
		XYIntervalSeries res = new XYIntervalSeries(key);
		result.addSeries(res);
		return res;
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config defaultConfig() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	/**
	 * Factory method to create an initialized {@link IntervalXYDatasetBuilder}.
	 * 
	 * @return a new IntervalXYDatasetBuilder.
	 */
	public static IntervalXYDatasetBuilder newInstance() {
		return (IntervalXYDatasetBuilder) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(defaultConfig());
	}

}
