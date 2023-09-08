/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.entry.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.util.sched.entry.SchedulerEntry;

/**
 * A {@link Filter} that accepts only {@link SchedulerEntry}s that have been manually started.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ManuallyStartedSchedulerEntryFilter implements Filter<SchedulerEntry> {

	/** The {@link ManuallyStartedSchedulerEntryFilter} instance. */
	public static final ManuallyStartedSchedulerEntryFilter INSTANCE = new ManuallyStartedSchedulerEntryFilter();

	@Override
	public boolean accept(SchedulerEntry entry) {
		return entry.getTask().isForcedRun();
	}

}
