/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.monitoring.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.react.chartjs.ChartElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Evaluates the data script of the chart of a view, as the chart does when it is displayed.
 */
public class ViewCharts {

	/**
	 * The Chart.js configuration the single chart of the given view computes.
	 *
	 * @param view
	 *        The view, as referenced from another view, e.g.
	 *        <code>admin/monitoring/memory.view.xml</code>.
	 * @param inputs
	 *        The values of the inputs of the chart, in the order the chart declares them.
	 */
	public static Map<?, ?> evaluate(String view, Object... inputs) throws Exception {
		return ThreadContext.inSystemContext(ViewCharts.class, () -> {
			ViewElement.Config config = ViewLoader.loadConfig(ViewLoader.fullPath(view));
			List<ChartElement.Config> charts = new ArrayList<>();
			collect(config, ChartElement.Config.class, charts);
			Assert.assertEquals("The view shows one chart.", 1, charts.size());
			QueryExecutor data = InputScript.compile(charts.get(0), ChartElement.Config.DATA);
			return (Map<?, ?>) data.execute(inputs);
		});
	}

	/**
	 * Adds all items of the given type in the configuration tree of the given item to the result.
	 */
	private static <T> void collect(ConfigurationItem item, Class<T> type, List<T> result) {
		if (type.isInstance(item)) {
			result.add(type.cast(item));
		}
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			if (property.isDerived()) {
				continue;
			}
			Object value = item.value(property);
			if (value instanceof ConfigurationItem child) {
				collect(child, type, result);
			} else if (value instanceof Collection<?> entries) {
				for (Object entry : entries) {
					if (entry instanceof ConfigurationItem child) {
						collect(child, type, result);
					}
				}
			} else if (value instanceof Map<?, ?> entries) {
				for (Object entry : entries.values()) {
					if (entry instanceof ConfigurationItem child) {
						collect(child, type, result);
					}
				}
			}
		}
	}

}
