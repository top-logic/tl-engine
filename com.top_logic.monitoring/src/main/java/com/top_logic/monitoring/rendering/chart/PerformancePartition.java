/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.rendering.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.admin.component.PerformanceMonitor.PerformanceDataEntryAggregated;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.ValueCriterion;
import com.top_logic.reporting.flex.chart.config.partition.PartitionFunction;
import com.top_logic.reporting.flex.chart.config.util.Configs;
import com.top_logic.util.Resources;

/**
 * {@link PartitionFunction}-implementation for performance-chart.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class PerformancePartition implements PartitionFunction {

	/**
	 * Creates a new {@link PerformancePartition}
	 */
	public PerformancePartition() {
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {

		List<Partition> result = new ArrayList<>();

		Map<String, Partition> map = new HashMap<>();

		Map<Long, Set<PerformanceDataEntryAggregated>> performanceData =
			(Map<Long, Set<PerformanceDataEntryAggregated>>) CollectionUtil.getFirst(aParent.getObjects());
		if (performanceData == null) {
			return result;
		}
		Resources resources = Resources.getInstance();
		Map<Long, Set<PerformanceDataEntryAggregated>> performanceDataGroupedBy =
			PerformanceMonitor.getInstance().getPerformanceDataGroupedBy(performanceData, false, false, true, true);
		Long baseInterval = PerformanceMonitor.getInstance().getCurrentInterval();
		for (Long interval : performanceDataGroupedBy.keySet()) {
			long intervalDiff = interval - baseInterval;
			if (intervalDiff > -PerformanceMonitor.getMaxCacheIntervals()) {
				for (PerformanceDataEntryAggregated pde : performanceDataGroupedBy.get(interval)) {
					ResKey key = pde.getTriggerName();
					String seriesName = resources.getString(key);
					Partition series = map.get(seriesName);
					if (series == null) {
						series = new Partition(aParent, seriesName, new ArrayList<>());
						map.put(seriesName, series);
					}
					series.getObjects().add(
						Configs.yHighLowInterval(intervalDiff, pde.getAvgTime(), pde.getMinTime(), pde.getMaxTime()));
				}
			}
		}

		result.addAll(map.values());
		Collections.sort(result);

		performanceDataGroupedBy =
			PerformanceMonitor.getInstance().getPerformanceDataGroupedBy(performanceData, true, true, true, true);
		Partition series =
			new Partition(aParent, resources.getString(I18NConstants.OVERALL_PARTITION), new ArrayList<>());
		result.add(series);
		for (Long interval : performanceDataGroupedBy.keySet()) {
			long intervalDiff = interval - baseInterval;
			if (intervalDiff > -PerformanceMonitor.getMaxCacheIntervals()) {
				PerformanceDataEntryAggregated performanceDataEntry =
					CollectionUtil.getFirst(performanceDataGroupedBy.get(interval));
				if (performanceDataEntry != null) {
					series.getObjects().add(
						Configs.yHighLowInterval(intervalDiff,
							performanceDataEntry.getAvgTime(),
							performanceDataEntry.getMinTime(),
							performanceDataEntry.getMaxTime()));
				}
			}
		}

		return result;
	}

	@Override
	public Criterion getCriterion() {
		return ValueCriterion.INSTANCE;
	}

}