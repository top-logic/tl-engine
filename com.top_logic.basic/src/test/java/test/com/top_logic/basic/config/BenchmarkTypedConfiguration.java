/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;
import com.top_logic.basic.util.StopWatch;

/**
 * A {@link TestCase} comparing runtime behavior (performance, memory consumption) of hand-made
 * classes to configuration interfaces.
 * 
 * <p>
 * Note: Run this test with the settings: <code>-Xms1024m -Xmx1024M</code>.
 * </p>
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
@SuppressWarnings("javadoc")
public class BenchmarkTypedConfiguration extends TestCase {

	private static final int ROWS = 1000000;

	private List<Object> _rows;

	private WeakReference<Object> _ref;

	private Object _referent;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_rows = new ArrayList<>(ROWS);
	}

	/**
	 * Create a large amount storage rows using {@link CustomImpl} and measure the average creation
	 * time for each row.
	 */
	public void testCustomImplCreation() throws InterruptedException {
		final String item = "item";
		final String kpi = "kpi";
		final String context = "context";

		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
		gcAndWait();
		final long memBefore = bean.getHeapMemoryUsage().getUsed();

		final StopWatch watch = StopWatch.createStartedWatch();
		for (int i = 0; i < ROWS; i++) {
			final CustomImpl row = new CustomImpl();

			row.setItem(item);
			row.setKpi(kpi);
			row.setContext(context);
			row.setValue(i);

			_rows.add(row);
		}
		watch.stop();
		gcAndWait();
		final long memAfter = bean.getHeapMemoryUsage().getUsed();
		final long memUsage = memAfter - memBefore;

		Logger.info(
			String.format("Created %,d " + CustomImpl.class.getSimpleName() + " instances in %,d ms (%,d ns per row)",
				ROWS,
				watch.getElapsedMillis(),
				watch.getElapsedNanos() / ROWS),
			BenchmarkTypedConfiguration.class);
		Logger.info(
			String.format(
				"Used %,d bytes for %,d " + CustomImpl.class.getSimpleName() + " instances (%,d bytes per row)",
				memUsage,
				ROWS,
				(memUsage / ROWS)),
			BenchmarkTypedConfiguration.class);
	}

	/**
	 * Create a large amount storage rows using {@link ConfigurationItem} and measure the average
	 * creation time for each row.
	 */
	public void testGenericCreation() throws InterruptedException {
		final String item = "item";
		final String kpi = "kpi";
		final String context = "context";

		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
		gcAndWait();
		final long memBefore = bean.getHeapMemoryUsage().getUsed();

		final StopWatch watch = StopWatch.createStartedWatch();
		for (int i = 0; i < ROWS; i++) {
			final ConfigImpl row = TypedConfiguration.newConfigItem(ConfigImpl.class);

			row.setItem(item);
			row.setKpi(kpi);
			row.setContext(context);
			row.setValue(i);

			_rows.add(row);
		}
		watch.stop();
		gcAndWait();
		final long memAfter = bean.getHeapMemoryUsage().getUsed();
		final long memUsage = memAfter - memBefore;

		Logger.info(
			String.format("Created %,d " + ConfigImpl.class.getSimpleName() + " instances in %,d ms (%,d ns per row)",
			ROWS,
			watch.getElapsedMillis(),
			watch.getElapsedNanos() / ROWS),
			BenchmarkTypedConfiguration.class);
		Logger.info(
			String.format(
				"Used %,d bytes for %,d " + ConfigImpl.class.getSimpleName() + " instances (%,d bytes per row)",
				memUsage,
				ROWS,
				(memUsage / ROWS)),
			BenchmarkTypedConfiguration.class);
	}

	/**
	 * Create a large amount storage rows using {@link ConfigurationItem} and measure the average
	 * creation time for each row.
	 */
	public void testGeneratedCreation() throws InterruptedException {
		final String item = "item";
		final String kpi = "kpi";
		final String context = "context";

		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
		gcAndWait();
		final long memBefore = bean.getHeapMemoryUsage().getUsed();

		final StopWatch watch = StopWatch.createStartedWatch();
		for (int i = 0; i < ROWS; i++) {
			final GeneratedImpl row = TypedConfiguration.newConfigItem(GeneratedImpl.class);

			row.setItem(item);
			row.setKpi(kpi);
			row.setContext(context);
			row.setValue(i);

			_rows.add(row);
		}
		watch.stop();
		gcAndWait();
		final long memAfter = bean.getHeapMemoryUsage().getUsed();
		final long memUsage = memAfter - memBefore;

		Logger.info(String.format(
			"Created %,d " + GeneratedImpl.class.getSimpleName() + " instances in %,d ms (%,d ns per row)",
			ROWS,
			watch.getElapsedMillis(),
			watch.getElapsedNanos() / ROWS),
			BenchmarkTypedConfiguration.class);
		Logger.info(
			String.format(
				"Used %,d bytes for %,d " + GeneratedImpl.class.getSimpleName() + " instances (%,d bytes per row)",
				memUsage,
				ROWS,
				(memUsage / ROWS)),
			BenchmarkTypedConfiguration.class);
	}

	private void gcAndWait() throws InterruptedException {
		_referent = new Object();
		_ref = new WeakReference<>(_referent);
		while (_ref.get() != null) {
			System.gc();
			Thread.sleep(100);
			_referent = null;
		}
	}

	/**
	 * An example class providing basic functionality of storage rows.
	 * 
	 * @author <a href=mailto:wta@top-logic.com>wta</a>
	 */
	public static class CustomImpl {

		private Object _item;

		private Object _kpi;

		private Object _context;

		private int _value;

		public Object getItem() {
			return _item;
		}

		public void setItem(final String item) {
			_item = item;
		}

		public Object getKpi() {
			return _kpi;
		}

		public void setKpi(final String kpi) {
			_kpi = kpi;
		}

		public Object getContext() {
			return _context;
		}

		public void setContext(final String context) {
			_context = context;
		}

		public int getValue() {
			return _value;
		}

		public void setValue(final int value) {
			_value = value;
		}
	}

	/**
	 * An example {@link ConfigurationItem} providing basic functionality of storage rows.
	 * 
	 * @author <a href=mailto:wta@top-logic.com>wta</a>
	 */
	public static interface GeneratedImpl extends ConfigurationItem {

		String getItem();

		void setItem(String item);

		String getKpi();

		void setKpi(String kpi);

		String getContext();

		void setContext(String context);

		int getValue();

		void setValue(int value);
	}

	/**
	 * An example {@link ConfigurationItem} providing basic functionality of storage rows.
	 * 
	 * @author <a href=mailto:wta@top-logic.com>wta</a>
	 */
	@NoImplementationClassGeneration
	public static interface ConfigImpl extends ConfigurationItem {

		String getItem();

		void setItem(String item);

		String getKpi();

		void setKpi(String kpi);

		String getContext();

		void setContext(String context);

		int getValue();

		void setValue(int value);
	}
}
