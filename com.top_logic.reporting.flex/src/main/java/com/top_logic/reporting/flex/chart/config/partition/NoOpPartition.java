/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.Collections;
import java.util.List;

import org.jfree.chart.JFreeChart;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.reporting.flex.chart.config.chartbuilder.AbstractJFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.NoneCriterion;

/**
 * {@link PartitionFunction} adding another dimension to a {@link JFreeChart} to match constraints
 * or to modify the view.
 * 
 * <p>
 * Example: A bar-chart with one {@link PartitionFunction} classifying the objects based on a
 * {@link TLEnumeration} will result in a chart with one series (column) per {@link TLClassifier}
 * and each column will have its own color.<br/>
 * A bar-chart with a {@link NoOpPartition} and then another {@link PartitionFunction} (see above)
 * will result in a chart with one series (labeled using the key from {@link Config#getLabelKey()})
 * and bars for each category ({@link TLClassifier}) in the same color.
 * </p>
 * 
 * @see AbstractJFreeChartBuilder#getMaxDimensions()
 * @see AbstractJFreeChartBuilder#getMinDimensions()
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class NoOpPartition implements PartitionFunction, ConfiguredInstance<NoOpPartition.Config> {

	/**
	 * Config-interface for {@link NoOpPartition}.
	 */
	public interface Config extends PartitionFunction.Config {

		@Override
		@ClassDefault(NoOpPartition.class)
		public Class<NoOpPartition> getImplementationClass();

		/**
		 * the resource-key used to label this {@link PartitionFunction} in a chart.
		 */
		@Mandatory
		public String getLabelKey();

		/**
		 * see {@link #getLabelKey()}
		 */
		public void setLabelKey(String key);

	}

	private final Config _config;

	/**
	 * Config-constructor for {@link MetaElementTypePartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public NoOpPartition(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		return Collections.singletonList(new Partition(aParent, getConfig().getLabelKey(), aParent.getObjects()));
	}

	@Override
	public Criterion getCriterion() {
		return NoneCriterion.INSTANCE;
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(String key) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setLabelKey(key);
		return item;
	}

}