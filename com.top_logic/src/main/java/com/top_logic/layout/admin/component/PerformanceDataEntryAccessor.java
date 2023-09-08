/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.admin.component.PerformanceMonitor.PerformanceAggregation;
import com.top_logic.layout.admin.component.PerformanceMonitor.PerformanceDataEntry;
import com.top_logic.layout.admin.component.PerformanceMonitor.PerformanceDataEntryAggregated;
import com.top_logic.layout.admin.component.PerformanceMonitor.PerformanceDataEntryIntervals;
import com.top_logic.util.Resources;

/**
 * Accessor for {@link PerformanceDataEntry} objects
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PerformanceDataEntryAccessor implements Accessor<PerformanceDataEntry> {

	/**
	 * Enum to specify which performance data will be returned for intervals
	 * 
	 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public static enum NumberKind {
		/** max value */
		MAX("max"),
		/** min value */
		MIN("min"),
		/** average value */
		AVG("average"),
		/** total value */
		TOT("total");

		private final String kind;

		private NumberKind(String aKind) {
			this.kind = aKind;
		}

		/**
		 * Get the {@link NumberKind} for a given String
		 * 
		 * @param aKind
		 *        the string
		 * @return a {@link NumberKind} or <code>null</code> of no suitable kind is found
		 */
		public static NumberKind getKind(String aKind) {
			if (MAX.kind.equalsIgnoreCase(aKind))
				return MAX;
			if (MIN.kind.equalsIgnoreCase(aKind))
				return MIN;
			if (AVG.kind.equalsIgnoreCase(aKind))
				return AVG;
			if (TOT.kind.equalsIgnoreCase(aKind))
				return TOT;

			return null;
		}

	};

	// Attribute names
	/** start time */
	public static final String START_TIME    = "startTime";

	/** time consumed */
	public static final String CONSUMED_TIME = "consumedTime";

	/** context name */
	public static final String CONTEXT_NAME  = "contextName";

	/** trigger name */
	public static final String TRIGGER_NAME  = "triggerName";

	/** processing kind */
	public static final String PERF_KIND     = "perfKind";

	/** Java VM name */
	public static final String VM_NAME       = "vmName";

	/** total time consumed */
	public static final String TOTAL_TIME    = "totalTime";

	/** minimal consumed time */
	public static final String MIN_TIME      = "minTime";

	/** average consumed time */
	public static final String AVG_TIME      = "avgTime";

	/** maximal consumed time */
	public static final String MAX_TIME      = "maxTime";

	/** number of entries */
	public static final String NUM_ENTRIES   = "numEntries";

	private NumberKind numKind;

	public PerformanceDataEntryAccessor(NumberKind aNumKind) {
		this.numKind = aNumKind;
	}

	/**
	 * @see com.top_logic.layout.Accessor#getValue(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object getValue(PerformanceDataEntry object, String property) {
		if (VM_NAME.equals(property)) {
			return object.getVmName();
		}
		if (CONTEXT_NAME.equals(property)) {
			return object.getContextName();
		}
		if (TRIGGER_NAME.equals(property)) {
			ResKey triggerName = object.getTriggerName();
			return Resources.getInstance().getString(triggerName);
		}
		if (PERF_KIND.equals(property)) {
			return object.getPerfKind();
		}
		if (object instanceof PerformanceDataEntryAggregated) {
			PerformanceDataEntryAggregated pdea = (PerformanceDataEntryAggregated) object;
			if (TOTAL_TIME.equals(property)) {
				return pdea.getTotalTime();
			}
			if (MIN_TIME.equals(property)) {
				return pdea.getMinTime();
			}
			if (MAX_TIME.equals(property)) {
				return pdea.getMaxTime();
			}
			if (PERF_KIND.equals(property)) {
				return pdea.getPerfKind();
			}
		}
		if (object instanceof PerformanceDataEntryIntervals) {
			PerformanceDataEntryIntervals pdei = (PerformanceDataEntryIntervals) object;
			try {
				Long theInterval = Long.parseLong(property) + PerformanceMonitor.getInstance().getCurrentInterval();
				PerformanceAggregation intervalData = pdei.getIntervalData(theInterval);
				if (intervalData != null) {
					switch (this.numKind) {
						case MAX:
							return intervalData.getMaxTime();
						case MIN:
							return intervalData.getMinTime();
						case AVG:
							return intervalData.getAvgTime();
						case TOT:
							return intervalData.getTotalTime();
					}
				}

				return Long.valueOf(0);
			} catch (NumberFormatException e) {
				// Ignore, not a number
			}
		}
		throw new IllegalArgumentException("Attribute " + property + " unknown for PerformanceDataEntry.");
	}

	@Override
	public void setValue(PerformanceDataEntry object, String property, Object value) {
		throw new UnsupportedOperationException("Changing PerformanceDataEntry is not allowed");
	}

}
