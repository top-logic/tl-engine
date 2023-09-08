/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.maintenance;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.monitor.ApplicationMonitor;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorResult;
import com.top_logic.util.sched.MemoryObserverThread;

/**
 * DPM specific variant of the {@link MemoryObserverThread} as of QC2558.
 *
 * It will not log internally but use use log4j to write a special log file.
 *
 * TODO Think about using a {@link MemoryNotificationInfo} so we log only when thresholds
 * are {@link MemoryNotificationInfo#MEMORY_THRESHOLD_EXCEEDED exceeded}.
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@ServiceDependencies({ AliasManager.Module.class, ApplicationMonitor.Module.class, FormatterService.Module.class })
public class MemoryObserver extends ConfiguredManagedClass<MemoryObserver.Config>
		implements MonitorComponent, Reloadable {

	/**
	 * Configuration for {@link MemoryObserver}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<MemoryObserver> {

		/**
		 * Logging interval of memory usage in milliseconds. See {@link Config#getLoggingInterval}.
		 */
		String LOGGING_INTERVAL = "loggingInterval";

		/**
		 * Names of Memory pools found in the used VMs, this may change with the type of GC used. See
		 * {@link Config#getPools}.
		 */
		String POOLS = "pools";

		/**
		 * See {@link Config#getThresholds}.
		 */
		String THRESHOLDS = "thresholds";

		/** Getter for {@link Config#LOGGING_INTERVAL}. */
		@Name(LOGGING_INTERVAL)
		@LongDefault(60000) // 1 minute
		long getLoggingInterval();

		/** Getter for {@link Config#POOLS}. */
		@Name(POOLS)
		Pools getPools();

		/** Getter for {@link Config#THRESHOLDS}. */
		@Name(THRESHOLDS)
		@Mandatory
		Thresholds getThresholds();
	}

	/**
	 * Configuration of {@link Config#POOLS}.
	 */
	public interface Pools extends ConfigurationItem {

		/**
		 * See {@link Pools#getMetaspace}.
		 */
		String METASPACE = "metaspace";

		/** Default value for {@link Pools#getMetaspace}. */
		public static final String METASPACE_DEFAULT = "Metaspace,Compressed Class Space";

		/**
		 * See {@link Pools#getOld}.
		 */
		String OLD = "old";

		/** Default value for {@link Pools#getOld}. */
		public static final String OLD_DEFAULT = "Tenured Gen,PS Old Gen,G1 Old Gen";

		/**
		 * See {@link Pools#getEden}.
		 */
		String EDEN = "eden";

		/** Default value for {@link Pools#getEden}. */
		public static final String EDEN_DEFAULT = "Eden Space,PS Eden Space,G1 Eden Space";

		/**
		 * See {@link Pools#getSurvivor}.
		 */
		String SURVIVOR = "survivor";

		/** Default value for {@link Pools#getSurvivor}. */
		public static final String SURVIVOR_DEFAULT = "Survivor Space,PS Survivor Space,G1 Survivor Space";

		/**
		 * See {@link Pools#getIgnored}.
		 */
		String IGNORED = "ignored";

		/** Default value for {@link Pools#getIgnored}. */
		public static final String IGNORED_DEFAULT =
			"Code Cache,CodeHeap 'non-nmethods',CodeHeap 'profiled nmethods',CodeHeap 'non-profiled nmethods'";

		/** Getter for {@link Pools#METASPACE}. */
		@Name(METASPACE)
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(METASPACE_DEFAULT)
		List<String> getMetaspace();

		/** Getter for {@link Pools#OLD}. */
		@Name(OLD)
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(OLD_DEFAULT)
		List<String> getOld();

		/** Getter for {@link Pools#EDEN}. */
		@Name(EDEN)
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(EDEN_DEFAULT)
		List<String> getEden();

		/** Getter for {@link Pools#SURVIVOR}. */
		@Name(SURVIVOR)
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(SURVIVOR_DEFAULT)
		List<String> getSurvivor();

		/** Getter for {@link Pools#IGNORED}. */
		@Name(IGNORED)
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(IGNORED_DEFAULT)
		List<String> getIgnored();
	}
	
	/**
	 * Configuration of {@link Config#THRESHOLDS}.
	 */
	public interface Thresholds extends ConfigurationItem {

		/**
		 * See {@link Thresholds#getMetaspace}.
		 */
		String METASPACE = "metaspace";

		/**
		 * See {@link Thresholds#getOldGen}.
		 */
		String OLD_GEN = "oldGen";

		/**
		 * See {@link Thresholds#getYoungGen}.
		 */
		String YOUNG_GEN = "youngGen";

		/**
		 * See {@link Thresholds#getHeap}.
		 */
		String HEAP = "heap";

		/** Getter for {@link Thresholds#METASPACE}. */
		@Name(METASPACE)
		Threshold getMetaspace();

		/** Getter for {@link Thresholds#OLD_GEN}. */
		@Name(OLD_GEN)
		Threshold getOldGen();

		/** Getter for {@link Thresholds#YOUNG_GEN}. */
		@Name(YOUNG_GEN)
		Threshold getYoungGen();

		/** Getter for {@link Thresholds#HEAP}. */
		@Name(HEAP)
		Threshold getHeap();
	}

	/**
	 * Configuration of a threshold.
	 */
	public interface Threshold extends ConfigurationItem {

		/**
		 * Percentage of the red threshold. See {@link Threshold#getRed}.
		 */
		String RED = "red";

		/**
		 * Percentage of the yellow threshold. See {@link Threshold#getYellow}.
		 */
		String YELLOW = "yellow";

		/** Getter for {@link Threshold#RED}. */
		@Name(RED)
		@Mandatory
		long getRed();

		/** Getter for {@link Threshold#YELLOW}. */
		@Name(YELLOW)
		@Mandatory
		long getYellow();
	}

	private static final String LOAD_RED = "Red";
	private static final String LOAD_YELLOW = "Yellow";
	private static final String LOAD_GREEN = "Green";

	/** Used in thresholds long[] arrays */
	private static final int YELLOW_INDEX = 0;

	/** Used in thresholds long[] arrays */
	private static final int RED_INDEX    = 1;

	/** Uused in thresholds long[] arrays, maximum as given at reload */
	private static final int MAX_INDEX    = 2;

	/** These memory pools will be ignored */
	private Set<String> ignoredNames;

	/** These names will be considered for youngGen. */
	private Set<String> edenNames;

	/** These names will be considered for youngGen. */
	private Set<String> survivorNames;

	/** These names will be considered calculating oldGen. */
	private Set<String> oldNames;

	/** These names will be considered calculating permGen. */
	private Set<String> metaspaceNames;

	/** [YELLOW_INDEX, RED_INDEX, MAX_INDEX */
	long[] heapThreshold = new long[] { Long.MAX_VALUE, Long.MAX_VALUE, 0 };

	long[] metaspaceThreshold        = new long[] {Long.MAX_VALUE, Long.MAX_VALUE , 0};

	/** Set to {@link Long#MAX_VALUE} to avoid any strange logs in transient states */
	long[] tenuredThreshold     = new long[] {Long.MAX_VALUE, Long.MAX_VALUE , 0};

	/** Set to {@link Long#MAX_VALUE} to avoid any strange logs in transient states */
	long[] edenThreshold        = new long[] {Long.MAX_VALUE, Long.MAX_VALUE , 0};

	/** Set to {@link Long#MAX_VALUE} to avoid any strange logs in transient states */
	long[] survivorThreshold    = new long[] {Long.MAX_VALUE, Long.MAX_VALUE , 0};

	private MonitorMessage runningOK;

	/** Actual logging happens here */
	private MemoryLogThread logThread;

	/** Last MonitorMessage (if any) */
	private MonitorMessage monitorMessage;

	/** Must reload this in case maximum values have changed. */
	private boolean needReload;

	/**
	 * Creates a new {@link MemoryObserver}.
	 */
	public MemoryObserver(InstantiationContext context, Config config) {
		super(context, config);
		runningOK = new MonitorMessage(MonitorMessage.Status.INFO, "running", this);
		this.reload();
	}

	/**
	 * Stop logging via the background thread,
	 */
	private synchronized void stopLogging() {
		if (logThread != null && !logThread.isInterrupted()) {
			logThread.interrupt();
		}
		logThread = null;
		monitorMessage = new MonitorMessage(MonitorMessage.Status.INFO, "stopping", this);
	}

	/**
	 * Stop all log and report activities
	 */
	private synchronized void startLogging(long interval) {
		if (logThread != null && !logThread.isInterrupted()) {
			logThread.interrupt();
		}
		logThread = new MemoryLogThread(interval);
		logThread.start();
		monitorMessage = new MonitorMessage(MonitorMessage.Status.INFO, "started", this);
	}

	@Override
	public synchronized boolean reload() {
		try {
			this.stopLogging();

			fetchPoolNames();
			fetchThresholds();

			startLogging(getConfig().getLoggingInterval());

			return true;
		} catch (Exception ex) {
			Logger.error("Failed to reload, check configuration", ex, MemoryObserver.class);
		}
		return false;
	}

	private final Set<String> getSet(String name, List<String> configuredPools, Set<String> knownPools) {
		Set<String> usedPools = new HashSet<>(configuredPools.size());
		for (String configuredPool : configuredPools) {
			if (nameMatchesSet(configuredPool, knownPools)) {
				usedPools.add(configuredPool);
			}
		}
		if (usedPools.isEmpty()) {
			Logger.warn("None of " + configuredPools + " for '" + name + " is in " + knownPools, MemoryObserver.class);
		}

		return Collections.<String>unmodifiableSet(usedPools);
	}

	private void fetchPoolNames() {
		Set<String> knownPools = fetchKnownPools();
		edenNames = getSet(Pools.EDEN, getEdenPoolNames(), knownPools);
		survivorNames = getSet(Pools.SURVIVOR, getSurvivorPoolNames(), knownPools);
		oldNames = getSet(Pools.OLD, getOldPoolNames(), knownPools);
		metaspaceNames = getSet(Pools.METASPACE, getMetaspacePoolNames(), knownPools);
		ignoredNames = getSet(Pools.IGNORED, getIgnoredPoolNames(), knownPools);
	}

	protected Set<String> fetchKnownPools() {
		List<MemoryManagerMXBean> memoryManagers =  ManagementFactory.getMemoryManagerMXBeans();
		Set<String> knownPools;
		if (memoryManagers.isEmpty()) {
			Logger.warn("System has no memoryManagers?", MemoryObserver.class);
			knownPools = Collections.emptySet();
		} else {
			knownPools = new HashSet<>(memoryManagers.get(0).getMemoryPoolNames().length);
			for (MemoryManagerMXBean memoryManager : memoryManagers) {
				knownPools.addAll(Arrays.asList(memoryManager.getMemoryPoolNames()));
			}
		}
		return knownPools;
	}

	/**
	 * Fetch thresholds from configuration and calculate absolute values form percentage.
	 */
	private void fetchThresholds() throws ConfigurationException {
		long edenMax     = 0;
		long survivorMax = 0;
		long oldMax      = 0;
		long metaspaceMax     = 0;

		for (MemoryPoolMXBean poolBean : ManagementFactory.getMemoryPoolMXBeans()) {
			String name = poolBean.getName();
			if (nameMatchesSet(name, ignoredNames)) {
				continue;
			}
			long max = poolBean.getPeakUsage().getMax();
			if (nameMatchesSet(name, edenNames)) {
				edenMax += max;
			} else if (nameMatchesSet(name, survivorNames)) {
				survivorMax += max;
			} else if (nameMatchesSet(name, oldNames)) {
				oldMax += max;
			} else if (nameMatchesSet(name, metaspaceNames)) {
				metaspaceMax += max;
			} else {
				Logger.warn("Unknown Memory Pool '" + name+ "' ignored", MemoryObserver.class);
			}
		}
		// What about 0 values ? when no pools of some type exist?
		long heapMax = edenMax + oldMax + metaspaceMax;

		// Duplicate usage of youngGenThreshold is as specified.
		fetchThreshold(edenMax, getYoungGenThreshold(), edenThreshold);

		// makes no sense as survivor space is allocated dynamically on demand and therefore would
		// always show red
		// fetchThreshold(config, survivorMax, "youngGenThreshold", survivorThreshold);
		survivorThreshold[MAX_INDEX] = survivorMax;

		fetchThreshold(oldMax, getOldGenThreshold(), tenuredThreshold);
		fetchThreshold(metaspaceMax, getMetaspaceThreshold(), metaspaceThreshold);
		fetchThreshold(heapMax, getHeapThreshold(), heapThreshold);
	}

	/**
	 * Check for valid entries, calculate actual threshold.
	 */
	protected void fetchThreshold(long maxValue, Threshold threshold, long[] into) throws ConfigurationException {
		long percentYellow = threshold.getYellow();
		checkPercent(percentYellow);
		long percentRed = threshold.getRed();
		checkPercent(percentRed);
		if (percentRed <= percentYellow) {
			throw new ConfigurationException("Percent Red smaller than percentYellow " + percentRed + "<=" + percentYellow);
		}
		into[YELLOW_INDEX] = maxValue / 100 * percentYellow;
		into[RED_INDEX] = maxValue / 100 * percentRed;
		into[MAX_INDEX] = maxValue;
	}

	private void checkPercent(long percent) throws ConfigurationException {
		if (percent < 0) {
			throw new ConfigurationException("Percent value < 0");
		}
		if (percent > 100) {
			throw new ConfigurationException("Percent value > 100");
		}
	}

	@Override
	public void checkState(MonitorResult result) {
		result.addMessage(monitorMessage);
	}

	@Override
	public String getName() {
		return "MemoryObserver";
	}

	@Override
	public String getDescription() {
		return "Log detailed memory usage.";
	}

	@Override
	public boolean usesXMLProperties() {
		return true;
	}

	/**
	 * true if the given name is part of one of the given namesToMatch (case insensitive)
	 */
	private static boolean nameMatchesSet(String name, Set<String> namesToMatch) {
		for (String nameToMatch : namesToMatch) {
			if (nameToMatch.toLowerCase().contains(name.toLowerCase()))
				return true;
		}
		return false;
	}

	public String fetchMemoryUsage() {
		try {
			boolean debug = Logger.isDebugEnabled(MemoryObserver.class);

			if (debug) {
				MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
				if (memoryBean != null) {
					Logger.debug(String.valueOf(memoryBean.getHeapMemoryUsage()), MemoryObserver.class);
				}
			}

			long edenMax, survivorMax, tenuredMax, permMax;
			long eden, survivor, tenured, perm;

			edenMax = survivorMax = tenuredMax = permMax =
				eden = survivor = tenured = perm = 0;

			// long heapMax = Runtime.getRuntime().maxMemory();

			for (MemoryPoolMXBean poolBean : ManagementFactory.getMemoryPoolMXBeans()) {
				String name = poolBean.getName();
				if (nameMatchesSet(name, ignoredNames)) {
					continue;
				}

				MemoryUsage peakUsage = poolBean.getPeakUsage();
				long used = peakUsage.getUsed();
				long max = peakUsage.getMax();
				poolBean.resetPeakUsage();

				if (nameMatchesSet(name, edenNames)) {
					eden += used;
					edenMax += max;
				} else if (nameMatchesSet(name, survivorNames)) {
					survivor += used;
					survivorMax += max;
				} else if (nameMatchesSet(name, oldNames)) {
					tenured += used;
					tenuredMax += max;
				} else if (nameMatchesSet(name, metaspaceNames)) {
					perm += used;
					permMax += max;
				} else if (debug) { // was done once at startup, thats enough
									// Can new memory pools appear during runtime?
					Logger.debug("Unknown Memory Pool '" + name + "' ignored", MemoryObserver.class);
				}
				if (debug) {
					Logger.debug(String.valueOf(poolBean.getUsage()), MemoryObserver.class);
					Logger.debug(String.valueOf(peakUsage), MemoryObserver.class);
					Logger.debug(String.valueOf(poolBean.getCollectionUsage()), MemoryObserver.class);
				}
			}

			Runtime theSystem = Runtime.getRuntime();
			long totalMemory = (theSystem.totalMemory() / 1024) / 1024;
			long freeMemory = (theSystem.freeMemory() / 1024) / 1024;
			long usedMemory = totalMemory - freeMemory;
			String memUsage =
				"Used Memory: " + usedMemory + "MB (" + totalMemory + "MB VM size, " + freeMemory + "MB free in VM): ";

			long maxHeap = edenMax + tenuredMax + permMax;
			StringBuilder message = new StringBuilder(1024);
			message.append(memUsage);

			long usedHeap = eden + tenured + perm;

			appendMemUsage(message, "Sum Heap Space (MB)", usedHeap, maxHeap, heapThreshold);
			message.append(' ');
			appendMemUsage(message, "Eden Space (MB)", eden, edenMax, edenThreshold);
			message.append(' ');
			appendMemUsage(message, "Survivor Space (MB)", survivor, survivorMax, survivorThreshold);
			message.append(' ');
			appendMemUsage(message, "Tenured Gen (MB)", tenured, tenuredMax, tenuredThreshold);
			message.append(' ');
			appendMemUsage(message, "Perm Gen (MB)", perm, permMax, metaspaceThreshold);

			if (tenured > tenuredThreshold[RED_INDEX] || perm > metaspaceThreshold[RED_INDEX]) {
				monitorMessage = new MonitorMessage(MonitorMessage.Status.ERROR, "OldGen and/or PermSpace reached threshold RED", this);
			} else if (tenured > tenuredThreshold[YELLOW_INDEX] || perm > metaspaceThreshold[YELLOW_INDEX]) {
				monitorMessage = new MonitorMessage(MonitorMessage.Status.ERROR, "OldGen and/or PermSpace reached threshold YELLOW", this);
			} else {
				monitorMessage = runningOK;
			}

			return message.toString();
		} catch (Exception e) { // dont let this cause an application error
			String msg = "Memory fetch failed.";
			Logger.error(msg, e, this);
			return msg;
		}
	}

	/**
	 * Append {@link #appendLoad(StringBuilder, String, long, long[]) appendLoad} and USED / MAX
	 */
	protected void appendMemUsage(StringBuilder message, String name, long usage, long max, long[] threshold) {
		Formatter formatter = HTMLFormatter.getInstance();
		appendLoad(message, name, usage, threshold)
			.append(formatter.formatDouble(usage / 1024d / 1024d))
			.append(" / ")
			.append(formatter.formatDouble(threshold[MAX_INDEX] / 1024d / 1024d));
		if (max != threshold[MAX_INDEX]) {
			Logger.info("'" + name + "' has changed from "
				+ formatter.formatDouble(threshold[MAX_INDEX] / 1024d / 1024d) + " to "
				+ formatter.formatDouble(max / 1024d / 1024d), MemoryLogThread.class);
			needReload = true;
		}
	}

	/**
	 * Append name, LOAD and care for the monitor in case of YELLOW/READ condition.,
	 */
	private StringBuilder appendLoad(StringBuilder bob, String name, long value, long[] threshold) {
		bob.append(name).append(" ( ");
		if (value < threshold[YELLOW_INDEX]) {
			bob.append(LOAD_GREEN);
		} else if (value < threshold[RED_INDEX]) {
			bob.append(LOAD_YELLOW);
		} else {
			bob.append(LOAD_RED);
		}
		return bob.append(" ) ");
	}

	@Override
	protected void startUp() {
		super.startUp();

		ReloadableManager.getInstance().addReloadable(this);
		ApplicationMonitor.getInstance().registerMonitor(getName(), this);
	}

	/**
	 * {@link #stopLogging()} and unregister me from {@link ReloadableManager} and {@link ApplicationMonitor}.
	 */
	@Override
	protected void shutDown() {
		stopLogging();
		ReloadableManager.getInstance().removeReloadable(this);
		ApplicationMonitor.getInstance().unregisterMonitor(getName());
	}

	/**
	 * A Thread that will log in the configured Intervals.
	 */
	private final class MemoryLogThread extends Thread {

		/** log every x milliseconds */
		private long loggingInterval;

		MemoryLogThread(long  loggingInterval) {
			super("MemoryLogThread");
			setDaemon(true);
			this.loggingInterval = loggingInterval;
		}

		@Override
		public void run() {
			try {
				while (true) {
					sleep(loggingInterval);
					Logger.info(fetchMemoryUsage(), MemoryObserver.class);
					if (needReload) {
						reload();
						break;
					}
				}
			}
			catch (InterruptedException iex) {
				Logger.info("Interrupted, will stop", this);
			}
		}
	}


	/**
	 * Getter for {@link Config#POOLS}.
	 */
	public Pools getPools() {
		return getConfig().getPools();
	}

	/**
	 * Getter for {@link Pools#METASPACE}.
	 */
	public List<String> getMetaspacePoolNames() {
		Pools pools = getPools();
		if (pools == null) {
			return getValueNonEmpty(Pools.METASPACE_DEFAULT);
		}
		return pools.getMetaspace();
	}

	/**
	 * Getter for {@link Pools#OLD}.
	 */
	public List<String> getOldPoolNames() {
		Pools pools = getPools();
		if (pools == null) {
			return getValueNonEmpty(Pools.OLD_DEFAULT);
		}
		return pools.getOld();
	}

	/**
	 * Getter for {@link Pools#EDEN}.
	 */
	public List<String> getEdenPoolNames() {
		Pools pools = getPools();
		if (pools == null) {
			return getValueNonEmpty(Pools.EDEN_DEFAULT);
		}
		return pools.getEden();
	}

	/**
	 * Getter for {@link Pools#SURVIVOR}.
	 */
	public List<String> getSurvivorPoolNames() {
		Pools pools = getPools();
		if (pools == null) {
			return getValueNonEmpty(Pools.SURVIVOR_DEFAULT);
		}
		return pools.getSurvivor();
	}

	/**
	 * Getter for {@link Pools#IGNORED}.
	 */
	public List<String> getIgnoredPoolNames() {
		Pools pools = getPools();
		if (pools == null) {
			return getValueNonEmpty(Pools.IGNORED_DEFAULT);
		}
		return pools.getIgnored();
	}

	/**
	 * @see CommaSeparatedStrings#getValueNonEmpty(String, CharSequence)
	 */
	public List<String> getValueNonEmpty(CharSequence propertyValue) {
		List<String> result = StringServices.toList(propertyValue, ',');
		return result == null ? new ArrayList<>() : result;
	}

	/**
	 * Getter for {@link Config#THRESHOLDS}.
	 */
	public Thresholds getThresholds() {
		return getConfig().getThresholds();
	}

	/**
	 * Getter for {@link Thresholds#METASPACE}.
	 */
	public Threshold getMetaspaceThreshold() {
		return getThresholds().getMetaspace();
	}

	/**
	 * Getter for {@link Thresholds#OLD_GEN}.
	 */
	public Threshold getOldGenThreshold() {
		return getThresholds().getOldGen();
	}

	/**
	 * Getter for {@link Thresholds#YOUNG_GEN}.
	 */
	public Threshold getYoungGenThreshold() {
		return getThresholds().getYoungGen();
	}

	/**
	 * Getter for {@link Thresholds#HEAP}.
	 */
	public Threshold getHeapThreshold() {
		return getThresholds().getHeap();
	}

	/**
	 * Module for instantiation of the {@link MemoryObserver}.
	 */
	public static final class Module extends TypedRuntimeModule<MemoryObserver> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<MemoryObserver> getImplementation() {
			return MemoryObserver.class;
		}
	}

}
