/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.exec;

import java.util.List;

import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;

/**
 * {@link LifePeriodComputation} that represents an objects which is always alive.
 * 
 *          com.top_logic.knowledge.service.db2.expr.exec.LifePeriodComputation
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ForeverAlive implements LifePeriodComputation {
	
	/**
	 * Singleton {@link ForeverAlive} instance.
	 */
	public static final ForeverAlive INSTANCE = new ForeverAlive();

	private ForeverAlive() {
		// Singleton constructor.
	}
	
	@Override
	public List<LongRange> computeRanges(Context context) {
		return LongRangeSet.FULL_SET;
	}

	@Override
	public String toString() {
		return "ForeverAlive";
	}
}
