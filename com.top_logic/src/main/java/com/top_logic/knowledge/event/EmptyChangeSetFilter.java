/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import com.top_logic.basic.col.Filter;

/**
 * {@link Filter} accepting empty {@link ChangeSet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EmptyChangeSetFilter implements Filter<ChangeSet> {

	/** Singleton {@link EmptyChangeSetFilter} instance. */
	public static final EmptyChangeSetFilter INSTANCE = new EmptyChangeSetFilter();

	private EmptyChangeSetFilter() {
		// singleton instance
	}

	@Override
	public boolean accept(ChangeSet anObject) {
		return anObject.getBranchEvents().isEmpty()
			&& anObject.getCreations().isEmpty()
			&& anObject.getUpdates().isEmpty()
			&& anObject.getDeletions().isEmpty();
	}

}

