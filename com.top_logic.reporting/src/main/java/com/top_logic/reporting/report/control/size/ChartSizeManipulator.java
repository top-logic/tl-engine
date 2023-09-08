/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.size;

import java.awt.Dimension;

import com.top_logic.reporting.report.control.producer.ChartContext;

/**
 * Changes the chart size if it is wished.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface ChartSizeManipulator {

	/**
	 * Please manipulate the chart size of the given dimension.
	 * 
	 * See {@link Dimension#setSize(int, int)}.
	 * 
	 * @param aChartContext
	 *            A chart context. Must NOT be <code>null</code>.
	 * @param aDimension
	 *            A chart dimension. Must NOT be <code>null</code>.
	 */
	public void processChartSize(ChartContext aChartContext, Dimension aDimension);
	
}
