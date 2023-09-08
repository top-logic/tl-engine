/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.knowledge.service.Revision;

/**
 * Specification of the {@link Revision}s to search in a {@link HistoryQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum RevisionParam {

	/**
	 * Limit the search to a range of {@link Revision}s.
	 * 
	 * @see HistoryQueryArguments#getStartRevision()
	 * @see HistoryQueryArguments#getStopRevision()
	 */
	range, 
	
	/**
	 * Search through the complete history.
	 */
	all
	
}
