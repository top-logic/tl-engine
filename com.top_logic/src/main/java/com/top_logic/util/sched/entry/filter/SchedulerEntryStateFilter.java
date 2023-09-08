/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.entry.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.util.Utils;
import com.top_logic.util.sched.entry.SchedulerEntry;
import com.top_logic.util.sched.entry.SchedulerEntry.State;

/**
 * A {@link Filter} for {@link SchedulerEntry}s that filters them by their
 * {@link SchedulerEntry#getState() state}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class SchedulerEntryStateFilter implements Filter<SchedulerEntry> {

	private final SchedulerEntry.State _state;

	/**
	 * Creates a new {@link SchedulerEntryStateFilter} for the given state.
	 * 
	 * @param state
	 *        Is allowed to be null.
	 */
	public SchedulerEntryStateFilter(State state) {
		_state = state;
	}

	@Override
	public boolean accept(SchedulerEntry entry) {
		return Utils.equals(entry.getState(), _state);
	}

}
