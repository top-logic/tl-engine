/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.knowledge.service.Revision;

/**
 * Arguments to a {@link HistoryQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HistoryQueryArguments extends QueryArguments<HistoryQueryArguments> {
	
	private long startRevision = 1;

	private long stopRevision = Revision.CURRENT_REV;
	
	/**
	 * Creates a {@link HistoryQueryArguments} with all default values.
	 */
	HistoryQueryArguments() {
		// All values are default.
	}

	/**
	 * The first {@link Revision} to search (inclusive).
	 */
	public long getStartRevision() {
		return startRevision;
	}
	
	/**
	 * @see #getStartRevision()
	 * 
	 * @return This instance for call chaining.
	 */
	public HistoryQueryArguments setStartRevision(long startRevision) {
		this.startRevision = startRevision;
		return this;
	}
	
	/**
	 * The last {@link Revision} to search (exclusive).
	 */
	public long getStopRevision() {
		return stopRevision;
	}
	
	/**
	 * @see #getStopRevision()
	 * 
	 * @return This instance for call chaining.
	 */
	public HistoryQueryArguments setStopRevision(long stopRevision) {
		this.stopRevision = stopRevision;
		return this;
	}

	@Override
	protected HistoryQueryArguments chain() {
		return this;
	}
	
}
