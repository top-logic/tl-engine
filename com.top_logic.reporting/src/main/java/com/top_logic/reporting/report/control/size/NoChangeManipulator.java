/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.size;

import java.awt.Dimension;

import com.top_logic.reporting.report.control.producer.ChartContext;

/**
 * This manipulator does nothing.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class NoChangeManipulator implements ChartSizeManipulator {

	/** Single instance of this class. */
	public static final NoChangeManipulator INSTANCE = new NoChangeManipulator();

	@Override
	public void processChartSize(ChartContext aChartContext, Dimension aDimension) {
		//  No op.
	}

}
