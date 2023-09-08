/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.chart.flex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.ResPrefix;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.DateCriterion;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;

/**
 * {@link PartitionFunction} for report dates.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ElementDatePartition implements PartitionFunction, ConfiguredInstance<ElementDatePartition.Config> {

	/** Resource prefix for UI elements. */
	public static final ResPrefix RES_PREFIX = ResPrefix.legacyClassLocal(ElementDatePartition.class);

	/**
	 * Config interface for {@link ElementDatePartition}.
	 */
	public interface Config extends PartitionFunction.Config {

		@Override
		@ClassDefault(ElementDatePartition.class)
		public Class<? extends ElementDatePartition> getImplementationClass();

		/**
		 * Gets the date filter.
		 */
		@InstanceFormat
		@InstanceDefault(TrueFilter.class)
		public Filter<Object> getDateFilter();

		/**
		 * @see #getDateFilter()
		 */
		public void setDateFilter(Filter<?> filter);

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link ElementDatePartition}.
	 * 
	 * @param aContext
	 *        - default config-constructor
	 * @param aConfig
	 *        - default config-constructor
	 */
	public ElementDatePartition(InstantiationContext aContext, Config aConfig) {
		_config = aConfig;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<Partition> result = new ArrayList<>();
		List<Object> objects = aParent.getObjects();
		if (objects.contains(null)) {
			return result;
		}
		List<StructuredElement> reports = CollectionUtil.dynamicCastView(StructuredElement.class, objects);
		Filter<Object> filter = getConfig().getDateFilter();
		for (StructuredElement element : reports) {
			Date date = (Date) element.getValue("date");
			if (date == null || !filter.accept(date)) {
				continue;
			}
			Partition partition = new Partition(aParent, date, Collections.singletonList(element));
			partition.put(aParent);
			result.add(partition);
		}

		return result;
	}

	@Override
	public Criterion getCriterion() {
		return DateCriterion.DATE_INSTANCE;
	}

}
