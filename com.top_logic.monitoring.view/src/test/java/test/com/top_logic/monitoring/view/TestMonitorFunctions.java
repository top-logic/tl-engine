/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.monitoring.view;

import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.monitoring.view.MonitorFunctions;
import com.top_logic.util.sched.MemoryObserverThread;
import com.top_logic.util.sched.MemoryObserverThread.MemoryUsageEntry;

/**
 * Test of {@link MonitorFunctions}.
 */
@SuppressWarnings("javadoc")
public class TestMonitorFunctions extends TestCase {

	/**
	 * How long the observer thread may take to record {@link #MIN_ENTRIES} entries after its start,
	 * recording one every 100 milliseconds in the test configuration.
	 */
	private static final long ENTRIES_TIMEOUT = 10000;

	/** Number of entries a test waits for, so that it sees the differences between entries. */
	static final int MIN_ENTRIES = 3;

	public void testMemoryUsage() throws InterruptedException {
		List<Map<String, Object>> entries = awaitEntries();
		assertTrue(entries.size() >= MIN_ENTRIES);

		Map<String, Object> previous = null;
		for (Map<String, Object> entry : entries) {
			long used = number(entry, MonitorFunctions.USED);
			long total = number(entry, MonitorFunctions.TOTAL);
			long max = number(entry, MonitorFunctions.MAX);
			long free = number(entry, MonitorFunctions.FREE);
			long gcCount = number(entry, MonitorFunctions.GC_COUNT);
			long gcTime = number(entry, MonitorFunctions.GC_TIME);
			assertTrue(entry.get(MonitorFunctions.DATE) instanceof Date);
			assertEquals(total - free, used);
			assertTrue(used >= 0);
			assertTrue(total <= max);
			assertTrue(gcCount >= 0);
			assertTrue(gcTime >= 0);

			if (previous != null) {
				Date before = (Date) previous.get(MonitorFunctions.DATE);
				Date after = (Date) entry.get(MonitorFunctions.DATE);
				assertFalse("Entries are in the order they were recorded.", after.before(before));
				assertTrue(gcCount >= number(previous, MonitorFunctions.GC_COUNT));
				assertTrue(gcTime >= number(previous, MonitorFunctions.GC_TIME));
			}
			previous = entry;
		}
	}

	public void testSnapshotGarbageCollection() {
		MemoryUsageEntry before = MemoryUsageEntry.snapshot();
		System.gc();
		MemoryUsageEntry after = MemoryUsageEntry.snapshot();

		assertTrue(before.getGcCount() >= 0);
		assertTrue(before.getGcTime() >= 0);
		assertTrue("The garbage collection count never decreases.", after.getGcCount() >= before.getGcCount());
		assertTrue("The garbage collection time never decreases.", after.getGcTime() >= before.getGcTime());
	}

	/**
	 * The entries of {@link MonitorFunctions#memoryUsage()}, once the observer thread has recorded
	 * at least {@link #MIN_ENTRIES} of them.
	 */
	static List<Map<String, Object>> awaitEntries() throws InterruptedException {
		assertTrue("The memory observer runs in the test setup.", MemoryObserverThread.Module.INSTANCE.isActive());
		long deadline = System.currentTimeMillis() + ENTRIES_TIMEOUT;
		while (true) {
			List<Map<String, Object>> entries = MonitorFunctions.memoryUsage();
			if (entries.size() >= MIN_ENTRIES) {
				return entries;
			}
			if (System.currentTimeMillis() > deadline) {
				fail("The memory observer recorded " + entries.size() + " entries within " + ENTRIES_TIMEOUT
					+ "ms, expected " + MIN_ENTRIES + ".");
			}
			// Garbage to collect between the entries.
			System.gc();
			Thread.sleep(50);
		}
	}

	private static long number(Map<String, Object> entry, String key) {
		Object value = entry.get(key);
		assertTrue("Value of '" + key + "' is a number: " + value, value instanceof Number);
		return ((Number) value).longValue();
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(TestMonitorFunctions.class, MemoryObserverThread.Module.INSTANCE));
	}

}
