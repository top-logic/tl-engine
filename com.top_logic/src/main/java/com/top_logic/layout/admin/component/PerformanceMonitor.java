/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.ProcessingKind;

/**
 * Holds and aggregates information about the time consumed for executing commands
 * in this VM.
 * Might be extended to measure rendering time etc.
 * Extensions to measure time in a cluster would need a database synchronization mechanism.
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class PerformanceMonitor extends ManagedClass implements Reloadable {

	private Map<Long, Set<PerformanceDataEntryAggregated>> perfData;
	
	private static long MAX_CACHE_INTERVALS;
	private static long INTERVAL_MILLIS;
	private static boolean CACHE_DETAILS;

	private Config _config;

	/**
	 * Configuration for {@link PerformanceMonitor}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<PerformanceMonitor> {
		/**
		 * False when no cache details should shown.
		 */
		@BooleanDefault(false)
		boolean hasCacheDetails();

		/**
		 * Maximal interval time for caching.
		 */
		@LongDefault(24 * 60)
		long getMaxCacheIntervals();

		/**
		 * Interval milliseconds.
		 */
		@LongDefault(60 * 1000)
		long getIntervalMillis();
	}

	/**
	 * Empty the cache
	 */
	public synchronized void clearCache() {
		this.perfData = new HashMap<>();
	}
	
	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link PerformanceMonitor}.
	 */
	public PerformanceMonitor(InstantiationContext context, Config config) {
		super(context, config);

		_config = config;

		init(config);
		ReloadableManager.getInstance().addReloadable(this);
	}

	private void init(Config config) {
		CACHE_DETAILS = config.hasCacheDetails();
		MAX_CACHE_INTERVALS = config.getMaxCacheIntervals();
		INTERVAL_MILLIS = config.getIntervalMillis();

		if (INTERVAL_MILLIS < 1) {
			INTERVAL_MILLIS = 1;
		}

		this.clearCache();
	}

	/**
	 * sole instance according to singleton pattern
	 */
	public static synchronized PerformanceMonitor getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}
	
	/**
	 * true if performance monitoring is enabled
	 */
	public static boolean isEnabled() {
		return Module.INSTANCE.isActive();
	}
	
	/**
	 * the max number of intervals to cache
	 */
	public static long getMaxCacheIntervals() {
		return MAX_CACHE_INTERVALS;
	}
	
	/** 
	 * true if details are cached
	 */
	public static boolean cacheDetails() {
		return CACHE_DETAILS;
	}

	/**
	 * the max cache time in msec
	 */
	public static long getIntervalMillis() {
		return INTERVAL_MILLIS;
	}
	
	/**
	 * Time entry with interval and consumed time information
	 * 
	 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public static class TimeEntry implements Comparable<TimeEntry> {
		/**
		 * Create a new TimeEntry
		 * 
		 * @param interval
		 *        the interval, i.e. time in msec / interval size
		 * @param consumed
		 *        the consumed time in msec
		 * @param profile
		 *        a profile id, e.g. user or session info
		 */
		public TimeEntry(long interval, long consumed, String profile) {
			this.intervalStart = interval;
			this.timeConsumed  = consumed;
			this.profileID = profile;
		}
		
		/**
		 * get the profile id
		 */
		public String getProfileID() {
			return (profileID);
		}

		/** 
		 * the start interval
		 */
		public long getIntervalStart() {
			return (intervalStart);
		}

		/** 
		 * the consumed time
		 */
		public long getTimeConsumed() {
			return (timeConsumed);
		}

		private long intervalStart; // Start time in msec as in System.currentTimeMillis()
		private long timeConsumed; 	// Consumed time in msec

		private String profileID; // used to store some kind of profile id, e.g. session or user or
									// role info
		
		/** 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(TimeEntry o) {
			int theDiff = (int) (intervalStart - o.intervalStart);
			if (theDiff != 0) {
				return theDiff;
			}
			
			theDiff = (int) (timeConsumed - o.timeConsumed);
			if (theDiff != 0) {
				return theDiff;
			}
			
			return Integer.compare(this.hashCode(), o.hashCode());
		}
	}
	
	/** 
	 * the current interval
	 */
	public Long getCurrentInterval() {
		return getIntervalFor(System.currentTimeMillis());
	}
	
	/** 
	 * the current interval
	 */
	protected Long getIntervalFor(long aTime) {
		return aTime / getIntervalMillis();
	}

	/**
	 * Aggregate entries according to common attribute value(s). Note: if all flags are set to
	 * false, a list with one {@link PerformanceDataEntry} will be returned that holds the total
	 * performance information
	 * 
	 * @param byContext
	 *        if true each context name gets an individual entry
	 * @param byTrigger
	 *        if true each trigger name gets an individual entry
	 * @param byVmName
	 *        if true each VM name gets an individual entry
	 * @param byPerfKind
	 *        if true each perf kind gets an individual entry
	 * @return the collection of resulting {@link PerformanceDataEntry}s
	 */
	public Map<Long, Set<PerformanceDataEntryAggregated>> getPerformanceDataGroupedBy(boolean byContext,
			boolean byTrigger,
			boolean byVmName, boolean byPerfKind) {
		return getPerformanceDataGroupedBy(this.perfData, byContext, byTrigger, byVmName, byPerfKind);
	}
	
	/**
	 * Aggregate entries according to common attribute value(s). Note: if all flags are set to
	 * false, a list with one {@link PerformanceDataEntry} will be returned that holds the total
	 * performance information
	 * 
	 * @param aPerfData the performance data. Must not be <code>null</code>.
	 * @param byContext
	 *        if true each context name gets an individual entry
	 * @param byTrigger
	 *        if true each trigger name gets an individual entry
	 * @param byVmName
	 *        if true each VM name gets an individual entry
	 * @param byPerfKind
	 *        if true each perf kind gets an individual entry
	 * @return the collection of resulting {@link PerformanceDataEntry}s
	 */
	public synchronized Map<Long, Set<PerformanceDataEntryAggregated>> getPerformanceDataGroupedBy(
			Map<Long, Set<PerformanceDataEntryAggregated>> aPerfData, boolean byContext, boolean byTrigger,
			boolean byVmName, boolean byPerfKind) {
		Map<Long, Set<PerformanceDataEntryAggregated>> theResult =
			new HashMap<>();
		for (Long theInterval : aPerfData.keySet()) {
			Set<PerformanceDataEntryAggregated> theResultEntries = new HashSet<>();
			theResult.put(theInterval, theResultEntries);
			Collection<PerformanceDataEntryAggregated> theEntries = aPerfData.get(theInterval);
			for (PerformanceDataEntryAggregated performanceDataEntry : theEntries) {
				String context = null, vm = null;
				ResKey trigger = null;
				ProcessingKind perf = null;
				if (!byContext) {
					String contextName = performanceDataEntry.getContextName();
					context = contextName;
				}
				if (!byTrigger) {
					trigger = performanceDataEntry.getTriggerName();
				}
				if (!byVmName) {
					String vmName = performanceDataEntry.getVmName();
					vm = vmName;
				}
				if (!byPerfKind) {
					ProcessingKind perfKind = performanceDataEntry.getPerfKind();
					perf = perfKind;
				}
				
				PerformanceDataEntryAggregated performanceData =
					(PerformanceDataEntryAggregated) searchOrEnterEntry(theResultEntries,
						new PerformanceDataEntryAggregated(perf, context, trigger, vm));
				performanceData.mergeWith(performanceDataEntry);
			}
		}

		return theResult;
	}

	/**
	 * Search a {@link PerformanceDataEntry} with the given properties in the given entries or
	 * create one with the given properties and enter it into the given entries
	 * 
	 * @param someEntries
	 *        some entries. Must not be <code>null</code>
	 * @return the entry. Never <code>null</code>.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected PerformanceDataEntry searchOrEnterEntry(Collection someEntries,
			PerformanceDataEntry aTemplate) {
		PerformanceDataEntry perfDE = aTemplate;
		boolean found = false;
		for (Iterator iterator = someEntries.iterator(); iterator.hasNext();) {
			PerformanceDataEntry theEntry = (PerformanceDataEntry) iterator.next();
			if (theEntry.equals(perfDE)) {
				found = true;
				perfDE = theEntry;
				break;
			}
		}
		if (!found) {
			someEntries.add(perfDE);
		}

		return perfDE;
	}

	/** 
	 * Get a filtered view on the {@link PerformanceDataEntry}s
	 * 
	 * @param startIntervalFrom		optional start time beginning
	 * @param startIntervalTo		optional start time end
	 * @param consumedTimeFrom	optional consumed time minimum
	 * @param consumedTimeTo	optional maximum consumed time
	 * @param perfKinds			optional collection of perfKinds
	 * @param vmNames			optional collection of vmNames
	 * @param triggerNames		optional collection of triggerNames
	 * @param contextNames		optional collection of contextNames
	 * @return the filtered {@link PerformanceDataEntry}s
	 */
	public synchronized Map<Long, Set<PerformanceDataEntryAggregated>> getPerformanceData(Long startIntervalFrom,
			Long startIntervalTo, Long consumedTimeFrom, Long consumedTimeTo, Collection<ProcessingKind> perfKinds,
			Collection<String> vmNames, Collection<ResKey> triggerNames, Collection<String> contextNames) {
		Map<Long, Set<PerformanceDataEntryAggregated>> theResult =
			new HashMap<>();
		Collection<Long> removeThem = new ArrayList<>();
		long minInterval = this.getCurrentInterval() - getMaxCacheIntervals();
		for (Long theInterval : this.perfData.keySet()) {
			if (theInterval < minInterval) {
				removeThem.add(theInterval);
			}
			else {
				Set<PerformanceDataEntryAggregated> theResultEntries = new HashSet<>();
				theResult.put(theInterval, theResultEntries);
				if (startIntervalFrom == null || startIntervalFrom <= theInterval) {
					if (startIntervalTo == null || startIntervalTo >= theInterval) {
						for (PerformanceDataEntryAggregated performanceDataEntry : this.perfData.get(theInterval)) {
							if (perfKinds == null || perfKinds.contains(performanceDataEntry.getPerfKind())) {
								if (vmNames == null || vmNames.contains(performanceDataEntry.getVmName())) {
									if (triggerNames == null || triggerNames.contains(performanceDataEntry.getTriggerName())) {
										if (contextNames == null || contextNames.contains(performanceDataEntry.getContextName())) {
											if (!cacheDetails() || consumedTimeFrom == null && consumedTimeTo == null) {
												theResultEntries.add(performanceDataEntry);
											}
											else {
												PerformanceDataEntry searchOrEnterEntry =
													searchOrEnterEntry(theResultEntries,
														new PerformanceDataEntryAggregated(
														performanceDataEntry));
												for (TimeEntry timeEntry : performanceDataEntry.getEntries()) {
													if (consumedTimeFrom == null || consumedTimeFrom <= timeEntry.getTimeConsumed()) {
														if (consumedTimeTo == null || consumedTimeTo >= timeEntry.getTimeConsumed()) {
															searchOrEnterEntry.addTimeEntry(timeEntry);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		for (Long toRemove : removeThem) {
			this.perfData.remove(toRemove);
		}
		
		return theResult;
	}
	
	/**
	 * the current time of the start of the measurement
	 */
	public long startMeasurement() {
		return System.currentTimeMillis();
	}

	/**
	 * Stop measuring and add a {@link PerformanceDataEntry} with the given attributes and return it
	 * 
	 * @param timeStart
	 *        the start time in msec as in System.currentTimeMillis();
	 * @param perfKind
	 *        the performance kind description
	 * @param contextName
	 *        the context (Component) name
	 * @param triggerName
	 *        the trigger (Command) name
	 * @param vmName
	 *        the name of the Java VM in which the entry was generated
	 * @return the created {@link PerformanceDataEntry}
	 */
	public PerformanceDataEntry stopMeasurement(long timeStart, ProcessingKind perfKind, String contextName,
			ResKey triggerName, String vmName) {
		return addPerformanceDataEntry(timeStart, System.currentTimeMillis() - timeStart,
			perfKind, contextName, triggerName, vmName);
	}

	/**
	 * Add a {@link PerformanceDataEntry} with the given attributes and return it
	 * 
	 * @param timeStart
	 *        the start time in msec as in System.currentTimeMillis();
	 * @param timeConsumed
	 *        the consumed time in msec
	 * @param perfKind
	 *        the performance kind description
	 * @param contextName
	 *        the context (Component) name
	 * @param triggerName
	 *        the trigger (Command) name
	 * @param vmName
	 *        the name of the Java VM in which the entry was generated
	 * @return the created {@link PerformanceDataEntry}
	 */
	public synchronized PerformanceDataEntry addPerformanceDataEntry(long timeStart, long timeConsumed,
			ProcessingKind perfKind, String contextName, ResKey triggerName, String vmName) {
		if (isEnabled()) {
			if (!StringServices.isEmpty(perfKind) || !StringServices.isEmpty(contextName) || triggerName != null) {
				Long intervalFor = getIntervalFor(timeStart);
				Set<PerformanceDataEntryAggregated> thePerfData = this.perfData.get(intervalFor);
				if (thePerfData == null) {
					thePerfData = new HashSet<>();
					this.perfData.put(intervalFor, thePerfData);
				}

				PerformanceDataEntry perfDE =
					searchOrEnterEntry(thePerfData,
						new PerformanceDataEntryAggregated(perfKind, contextName, triggerName, vmName));
				
				perfDE.addTimeEntry(new TimeEntry(timeStart, timeConsumed, null));
				
				return perfDE;
			}
		}

		return null;
	}
	
	/**
	 * {@link PerformanceDataEntry} that holds interval data internally
	 * 
	 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public static class PerformanceDataEntryIntervals extends PerformanceDataEntry {
		private Map<Long, PerformanceAggregation> perfAggs;

		/**
		 * Create a new PerformanceDataEntryIntervals from a template taking over the basic
		 * attributes
		 * 
		 * @param aTemplate
		 *        a template
		 */
		public PerformanceDataEntryIntervals(PerformanceDataEntry aTemplate) {
			super(aTemplate.getPerfKind(), aTemplate.getContextName(), aTemplate.getTriggerName(), aTemplate
				.getVmName());
			this.perfAggs = new HashMap<>();
		}

		/**
		 * Get the {@link PerformanceAggregation} for an interval.
		 * 
		 * @param interval
		 *        the interval
		 * @return the {@link PerformanceAggregation} for an interval. Maybe <code>null</code>
		 */
		public PerformanceAggregation getIntervalData(Long interval) {
			return this.perfAggs.get(interval);
		}

		/**
		 * Set the interval data
		 * 
		 * @param interval
		 *        the interval
		 * @param data
		 *        the data
		 * @return the old data for the interval if available
		 */
		public PerformanceAggregation setIntervalData(Long interval, PerformanceAggregation data) {
			return this.perfAggs.put(interval, data);
		}
	}

	/**
	 * Performance aggragation data
	 * 
	 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public static class PerformanceAggregation {
		/** min consumed time of all entries */
		protected long minTime;

		/** max consumed time of all entries */
		private long maxTime;

		/** average consumed time over all entries */
		protected long avgTime;

		/** total consumed time of all entries */
		protected long totalTime;

		/** number of entries */
		protected long numEntries;

		/**
		 * Create a new PerformanceAggregation
		 * 
		 */
		public PerformanceAggregation() {
			// Defaults are fine for fields
		}

		/**
		 * Create a new PerformanceAggregation from a template taking over the classifying data
		 * 
		 * @param aTemplate
		 *        the template
		 */
		public PerformanceAggregation(PerformanceAggregation aTemplate) {
			this.avgTime = aTemplate.avgTime;
			this.minTime = aTemplate.minTime;
			this.maxTime = aTemplate.maxTime;
			this.totalTime = aTemplate.totalTime;
			this.numEntries = aTemplate.numEntries;
		}

		/**
		 * the average consumed time of all entries
		 */
		public long getAvgTime() {
			return (avgTime);
		}

		/**
		 * the maximal consumed time of all entries
		 */
		public long getMaxTime() {
			return (maxTime);
		}

		/**
		 * the minimal consumed time of all entries
		 */
		public long getMinTime() {
			return (minTime);
		}

		/**
		 * the number of entries
		 */
		public long getNumEntries() {
			return (numEntries);
		}

		/**
		 * the aggregated consumed time of all entries
		 */
		public long getTotalTime() {
			return (totalTime);
		}
	}

	/**
	 * Transform the aggregation data that comes indexed by interval into a set of
	 * {@link PerformanceDataEntryAggregated} that contains the interval data in each entry
	 * 
	 * @param someData
	 *        the data
	 * @return the transformed data as defined above
	 */
	public Set<PerformanceDataEntryIntervals> getIntervalPerformanceData(
			Map<Long, Set<PerformanceDataEntryAggregated>> someData) {
		Set<PerformanceDataEntryIntervals> allSeries = new HashSet<>();
		for (Long theInterval : someData.keySet()) {
			for (PerformanceDataEntryAggregated thePDE : someData.get(theInterval)) {
					PerformanceDataEntryIntervals series =
						(PerformanceDataEntryIntervals) this.searchOrEnterEntry(allSeries,
							new PerformanceDataEntryIntervals(thePDE));
				series.setIntervalData(theInterval, new PerformanceAggregation(thePDE.getPerformanceAggregation()));
				}
		}

		return allSeries;
	}

	/**
	 * An entry of the performance measuring
	 * 
	 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public static class PerformanceDataEntryAggregated extends PerformanceDataEntry {
		// Data
		private PerformanceAggregation perfAggr; // aggregated performance data

		/**
		 * Create a new entry
		 * 
		 * @param perfKind
		 *        the performance kind description
		 * @param contextName
		 *        the context (Component) name
		 * @param triggerName
		 *        the trigger (Command) name
		 * @param vmName
		 *        the name of the Java VM in which the entry was generated
		 */
		public PerformanceDataEntryAggregated(ProcessingKind perfKind, String contextName, ResKey triggerName,
				String vmName) {
			super(perfKind, contextName, triggerName, vmName);
			this.perfAggr = new PerformanceAggregation();
		}

		/**
		 * Create a new PerformanceDataEntryAggregated from a template
		 * 
		 * @param aTemplate
		 *        a template
		 */
		public PerformanceDataEntryAggregated(PerformanceDataEntry aTemplate) {
			super(aTemplate.getPerfKind(), aTemplate.getContextName(), aTemplate.getTriggerName(), aTemplate
				.getVmName());
			this.perfAggr = new PerformanceAggregation();
		}

		/**
		 * Merge this with another entry
		 * 
		 * @param performanceDataEntry
		 *        the other entry. Must not be <code>null</code>
		 */
		public void mergeWith(PerformanceDataEntryAggregated performanceDataEntry) {
			if (performanceDataEntry.perfAggr.maxTime > this.perfAggr.maxTime) {
				this.perfAggr.maxTime = performanceDataEntry.perfAggr.maxTime;
			}
			if (this.perfAggr.minTime == 0 || this.perfAggr.numEntries > 0
				&& performanceDataEntry.perfAggr.minTime < this.perfAggr.minTime) {
				this.perfAggr.minTime = performanceDataEntry.perfAggr.minTime;
			}
			this.perfAggr.avgTime =
				(performanceDataEntry.perfAggr.totalTime + this.perfAggr.totalTime)
					/ (performanceDataEntry.perfAggr.numEntries + this.perfAggr.numEntries);
			this.perfAggr.totalTime += performanceDataEntry.perfAggr.totalTime;
			this.perfAggr.numEntries += performanceDataEntry.perfAggr.numEntries;
			this.entries.addAll(performanceDataEntry.entries);
		}

		/**
		 * Add a new entry to the cache
		 * 
		 * @param aPDE
		 *        Must not be <code>null</code>
		 */
		@Override
		public synchronized void addTimeEntry(TimeEntry aPDE) {
			super.addTimeEntry(aPDE);

			if (this.perfAggr.numEntries == 0) {
				this.perfAggr.minTime = aPDE.getTimeConsumed();
			}

			if (aPDE.getTimeConsumed() < this.perfAggr.minTime) {
				this.perfAggr.minTime = aPDE.getTimeConsumed();
			}
			if (aPDE.getTimeConsumed() > this.perfAggr.maxTime) {
				this.perfAggr.maxTime = aPDE.getTimeConsumed();
			}

			this.perfAggr.totalTime += aPDE.getTimeConsumed();

			this.perfAggr.numEntries++;

			this.perfAggr.avgTime = this.perfAggr.totalTime / this.perfAggr.numEntries;
		}

		// Methods
		/**
		 * the aggregated performance data
		 */
		public PerformanceAggregation getPerformanceAggregation() {
			return this.perfAggr;
		}

		/**
		 * the maximal consumed time of all entries
		 */
		public long getMaxTime() {
			return (perfAggr.maxTime);
		}

		/**
		 * the average consumed time of all entries
		 */
		public long getAvgTime() {
			return (perfAggr.avgTime);
		}

		/**
		 * the minimal consumed time of all entries
		 */
		public long getMinTime() {
			return (perfAggr.minTime);
		}

		/**
		 * the number of entries
		 */
		public long getNumEntries() {
			return (perfAggr.numEntries);
		}

		/**
		 * the aggregated consumed time of all entries
		 */
		public long getTotalTime() {
			return (perfAggr.totalTime);
		}
	}

	/**
	 * An entry of the performance measuring
	 * 
	 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public static class PerformanceDataEntry {
		// Data
		/** e.g. Command, Rendering etc. */
		protected ProcessingKind perfKind;

		/** e.g. name of component context (optional) */
		protected String contextName;

		/** e.g. name of command (optional) */
		protected ResKey triggerName;

		/** name of the cluster node / Java VM */
		protected String vmName;

		/** the time entries. Only used if detail cacheing is enabled */
		protected SortedSet<TimeEntry> entries;
		
		/**
		 * Create a new PerformanceDataEntry from a template
		 * 
		 * @param aTemplate
		 *        a template
		 */
		public PerformanceDataEntry(PerformanceDataEntry aTemplate) {
			this(aTemplate.getPerfKind(), aTemplate.getContextName(), aTemplate.getTriggerName(), aTemplate.getVmName());
		}

		/**
		 * Create a new entry
		 * 
		 * @param perfKind
		 *        the performance kind description
		 * @param contextName
		 *        the context (Component) name
		 * @param triggerName
		 *        the trigger (Command) name
		 * @param vmName
		 *        the name of the Java VM in which the entry was generated
		 */
		public PerformanceDataEntry(ProcessingKind perfKind, String contextName, ResKey triggerName, String vmName) {
			super();
			this.perfKind = perfKind;
			this.contextName = contextName;
			this.triggerName = triggerName;
			this.vmName = vmName;
			this.entries = new TreeSet<>();
		}

		/**
		 * the performance kind
		 */
		public ProcessingKind getPerfKind() {
			return (perfKind);
		}

		/**
		 * the context (e.g. Component) name
		 */
		public String getContextName() {
			return (contextName);
		}

		/**
		 * the trigger (e.g. Command) name
		 */
		public ResKey getTriggerName() {
			return (triggerName);
		}

		/**
		 * the Java VM / cluster node name
		 */
		public String getVmName() {
			return (vmName);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PerformanceDataEntry)) {
				return false;
			}
			
			PerformanceDataEntry o = (PerformanceDataEntry) obj;
			
			if (!Utils.equals(contextName, o.contextName)) {
				return false;
			}
			if (!Utils.equals(triggerName, o.triggerName)) {
				return false;
			}
			if (!Utils.equals(vmName, o.vmName)) {
				return false;
			}
			if (!Utils.equals(perfKind, o.perfKind)) {
				return false;
			}

			return true;
		}
		
		@Override
		public int hashCode() {
			int hash = 0;
			
			if (this.contextName != null) {
				hash += this.contextName.hashCode();
			}

			if (this.triggerName != null) {
				hash += this.triggerName.hashCode();
			}

			if (this.vmName != null) {
				hash += this.vmName.hashCode();
			}

			if (this.perfKind != null) {
				hash += this.perfKind.hashCode();
			}

			return hash;
		}

		/**
		 * Add a new entry to the cache
		 * 
		 * @param aPDE Must not be <code>null</code>
		 */
		public synchronized void addTimeEntry(TimeEntry aPDE) {
			if (cacheDetails()) {
				this.entries.add(aPDE);
			}
		}

		// Methods
		/**
		 * all entries
		 */
		public SortedSet<TimeEntry> getEntries() {
			return Collections.unmodifiableSortedSet(entries);
		}
	}

	/**
	 * @see com.top_logic.basic.Reloadable#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Collect performance data";
	}

	/**
	 * @see com.top_logic.basic.Reloadable#getName()
	 */
	@Override
	public String getName() {
		return "PerformanceMonitor";
	}

	/**
	 * @see com.top_logic.basic.Reloadable#reload()
	 */
	@Override
	public boolean reload() {
		try {
			_config = (Config) ApplicationConfig.getInstance().getServiceConfiguration(PerformanceMonitor.class);
		} catch (ConfigurationException ex) {
			ex.printStackTrace();
		}

		init(_config);

		return true;
	}

	
	@Override
	public boolean usesXMLProperties() {
		return true;
	}

	/**
	 * Module for {@link PerformanceMonitor}
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<PerformanceMonitor> {

		/**
		 * Module instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<PerformanceMonitor> getImplementation() {
			return PerformanceMonitor.class;
		}

	}
}
