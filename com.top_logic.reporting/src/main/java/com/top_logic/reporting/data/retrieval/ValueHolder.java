/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.retrieval;

import com.top_logic.reporting.data.base.value.Value;

/**
 * A ValueHolder contains one or more values of the data described 
 * by the appropriate description.
 * 
 * The value holder is responsible for a special set of information. 
 * Because this is normally a range of data (eg. costs of 5 years),
 * this holder provides even access to the data and also to the range
 * of it. Inside the processing engine the data from different ranges
 * can be cummulated and result in a result over a longer range, example:
 * <pre>
 * Cost Workpackage 1:
 * 2002   2003   2004          2006   2007   2008
 * 104K   154K   100K           67K    11K     7K
 * 
 * Cost Workpackage 2:
 *        2003   2004   2005          2007
 *         34K    22K    12K           33K
 * </pre>
 * The processing engine is able to sum these costs because of the range
 * from the different value holders:
 * <pre>
 * Total Cost Workpackages 1-2:
 * 2002   2003   2004   2005   2006   2007   2008
 * 104K   188K   122K    12K    67K    44K     7K
 * </pre>
 * 
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public interface ValueHolder {

	/**
	 * The range of the held values as written in the class description.
     * 
	 * @return    The range of values within this ValueHolder.
	 */
	public Range[] getRange();

	/**
     * Return a value for a special point in the holder.
     * 
     * The given {@link Range} is one value out of the
     * range, this instance returns. For every part of the range, this
     * method must be able to return a value.
     * 
	 * @param    aKey    A point from the range.
	 * @return   The value at that point of the range.
	 */
	public Value getValue(Range aKey);

	/**
     * Returns the display name of this value holder.
     * 
     * This display name is needed to differentiate the varios value 
     * holders for the user.
     * 
	 * @return    The display name of this ValueHolder.
	 */
	public String getDisplayName();
}
