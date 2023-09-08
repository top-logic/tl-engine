/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.exec;

import java.util.List;

import com.top_logic.basic.col.LongRange;

/**
 * Computation of the fife range of some object.
 * 
 * @see Context
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public interface LifePeriodComputation {

	/**
	 * Creates the life period of the represented object
	 * 
	 * @param context
	 *        {@link Context} in which computation occurs.
	 * @return Non overlapping {@link LongRange}s in ascending order in which the represented object
	 *         was alive.
	 */
	List<LongRange> computeRanges(Context context);

}
