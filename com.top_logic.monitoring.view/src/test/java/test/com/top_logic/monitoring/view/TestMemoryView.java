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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.chartjs.ChartElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.sched.MemoryObserverThread;

/**
 * Evaluates the chart of the view <code>admin/monitoring/memory.view.xml</code> against the entries
 * the running {@link MemoryObserverThread} recorded.
 */
@SuppressWarnings("javadoc")
public class TestMemoryView extends TestCase {

	private static final String VIEW = "admin/monitoring/memory.view.xml";

	public void testChartData() throws Exception {
		List<Map<String, Object>> entries = TestMonitorFunctions.awaitEntries();

		Map<?, ?> chart = ThreadContext.inSystemContext(TestMemoryView.class, () -> {
			ViewElement.Config view = ViewLoader.loadConfig(ViewLoader.fullPath(VIEW));
			List<ChartElement.Config> charts = new ArrayList<>();
			collect(view, ChartElement.Config.class, charts);
			assertEquals("The view shows one chart.", 1, charts.size());
			QueryExecutor data = InputScript.compile(charts.get(0), ChartElement.Config.DATA);
			// The single input is the refresh channel, which holds nothing before the first refresh.
			return (Map<?, ?>) data.execute(new Object[] { null });
		});

		assertEquals("line", chart.get("type"));
		Map<?, ?> data = (Map<?, ?>) chart.get("data");
		List<?> labels = (List<?>) data.get("labels");
		List<?> datasets = (List<?>) data.get("datasets");
		int recorded = labels.size();
		assertTrue("At least the entries recorded before evaluating the chart are shown.",
			recorded >= entries.size() && recorded >= TestMonitorFunctions.MIN_ENTRIES);
		assertEquals("Used, reserved and maximum memory and the garbage collections.", 4, datasets.size());
		for (Object dataset : datasets) {
			List<?> values = (List<?>) ((Map<?, ?>) dataset).get("data");
			assertEquals("One value per recorded entry.", recorded, values.size());
			for (Object value : values) {
				assertTrue("Value is a non-negative number: " + value,
					value instanceof Number number && number.doubleValue() >= 0);
			}
		}

		Object usedLabel = ((Map<?, ?>) datasets.get(0)).get("label");
		assertTrue("The label is the translated text: " + usedLabel, "Used".equals(usedLabel) || "Belegt".equals(usedLabel));

		Map<?, ?> collections = (Map<?, ?>) datasets.get(3);
		assertEquals("bar", collections.get("type"));
		assertEquals("y1", collections.get("yAxisID"));
		assertEquals("No garbage collections are counted before the first entry.", 0.0,
			((Number) ((List<?>) collections.get("data")).get(0)).doubleValue());
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

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestMemoryView.class),
				TypeIndex.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE,
				MemoryObserverThread.Module.INSTANCE));
	}

}
