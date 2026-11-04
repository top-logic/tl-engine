/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.sched.MemoryObserverThread;
import com.top_logic.util.sched.MemoryObserverThread.MemoryUsageEntry;

/**
 * TL-Script functions reporting how the application uses the resources of its VM over time.
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #memoryUsage()} is called <code>monitorMemoryUsage</code>. The functions
 *           describe the internals of the running application, which only an administrator may
 *           inspect from a script entered interactively.
 */
@ScriptPrefix(MonitorFunctions.PREFIX)
@AdminOnly
public class MonitorFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "monitor";

	/** Key of the time of an entry of {@link #memoryUsage()}, see {@link MemoryUsageEntry#getDate()}. */
	public static final String DATE = "date";

	/**
	 * Key of the number of bytes used by objects of an entry of {@link #memoryUsage()}, see
	 * {@link MemoryUsageEntry#getUsed()}.
	 */
	public static final String USED = "used";

	/**
	 * Key of the number of bytes reserved by the VM of an entry of {@link #memoryUsage()}, see
	 * {@link MemoryUsageEntry#getTotal()}.
	 */
	public static final String TOTAL = "total";

	/**
	 * Key of the maximum number of bytes available to the VM of an entry of
	 * {@link #memoryUsage()}, see {@link MemoryUsageEntry#getMax()}.
	 */
	public static final String MAX = "max";

	/**
	 * Key of the number of reserved bytes not used by objects of an entry of
	 * {@link #memoryUsage()}, see {@link MemoryUsageEntry#getFree()}.
	 */
	public static final String FREE = "free";

	/**
	 * Key of the number of garbage collections since the start of the VM of an entry of
	 * {@link #memoryUsage()}, see {@link MemoryUsageEntry#getGcCount()}.
	 */
	public static final String GC_COUNT = "gcCount";

	/**
	 * Key of the milliseconds spent in garbage collection since the start of the VM of an entry
	 * of {@link #memoryUsage()}, see {@link MemoryUsageEntry#getGcTime()}.
	 */
	public static final String GC_TIME = "gcTime";

	/**
	 * The memory usage the {@link MemoryObserverThread} has recorded.
	 *
	 * <p>
	 * Each entry is a dictionary with the time it was recorded under {@value #DATE}, the bytes
	 * used by objects under {@value #USED}, reserved by the VM under {@value #TOTAL}, available to
	 * the VM at most under {@value #MAX} and reserved but unused under {@value #FREE}. The number
	 * of garbage collections and the milliseconds spent in them since the start of the VM, summed
	 * over all garbage collectors, are given under {@value #GC_COUNT} and {@value #GC_TIME}; the
	 * difference of two entries tells how much garbage collection happened between them.
	 * </p>
	 *
	 * @return The recorded entries in the order they were recorded, the oldest first. Empty if
	 *         the {@link MemoryObserverThread} is not running or keeps no entries.
	 */
	@Label("Memory usage over time")
	public static List<Map<String, Object>> memoryUsage() {
		if (!MemoryObserverThread.Module.INSTANCE.isActive()) {
			return List.of();
		}
		List<? extends MemoryUsageEntry> newestFirst = MemoryObserverThread.getInstance().getEntries();
		List<Map<String, Object>> result = new ArrayList<>(newestFirst.size());
		for (int n = newestFirst.size() - 1; n >= 0; n--) {
			result.add(toMap(newestFirst.get(n)));
		}
		return result;
	}

	private static Map<String, Object> toMap(MemoryUsageEntry entry) {
		return Map.of(
			DATE, entry.getDate(),
			USED, entry.getUsed(),
			TOTAL, entry.getTotal(),
			MAX, entry.getMax(),
			FREE, entry.getFree(),
			GC_COUNT, entry.getGcCount(),
			GC_TIME, entry.getGcTime());
	}

}
