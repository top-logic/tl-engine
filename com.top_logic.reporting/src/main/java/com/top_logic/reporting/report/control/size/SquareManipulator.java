/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.size;

import java.awt.Dimension;

import com.top_logic.reporting.report.control.producer.ChartContext;

/**
 * This manipulater set the width and height to the same
 * value. 
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class SquareManipulator implements ChartSizeManipulator {

	@Override
	public void processChartSize(ChartContext aChartContext, Dimension aDimension) {
		double size;
		size = Math.min(aDimension.getWidth(), aDimension.getHeight());
		size = Math.max(size,                  /* Default */ 150);
		
		aDimension.setSize(size, size);
	}

}
