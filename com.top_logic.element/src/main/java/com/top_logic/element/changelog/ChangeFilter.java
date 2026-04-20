/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import com.top_logic.element.changelog.model.Change;
import com.top_logic.knowledge.service.Revision;

/**
 * Filter deciding which {@link Change}s are relevant for a {@link ChangeLogBuilder} result.
 *
 * <p>
 * Implementations may hold arbitrary state (e.g. memoization caches) and are typically not
 * thread-safe. A filter instance is used by at most one {@link ChangeLogBuilder} invocation.
 * </p>
 */
public interface ChangeFilter {

	/**
	 * Whether the given {@link Change} passes this filter.
	 */
	boolean accept(Change change);

	/**
	 * Allows the filter to narrow the {@link ChangeLogBuilder#getStartRev() start revision} before
	 * the log is built.
	 *
	 * @param startRev
	 *        The start revision configured on the {@link ChangeLogBuilder}.
	 * @return The effective start revision to use. Must not be before <code>startRev</code>.
	 */
	default Revision adjustStartRev(Revision startRev) {
		return startRev;
	}

}
