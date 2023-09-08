/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.handler;

import java.awt.Dimension;
import java.util.Map;

import com.top_logic.layout.DisplayContext;
import com.top_logic.reporting.flex.chart.component.AbstractChartComponent;

/**
 * Default-implementation of {@link ImagePostHandler} that does nothing.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class NoPostHandler implements ImagePostHandler {

	/**
	 * Singleton <code>INSTANCE</code>
	 */
	public static NoPostHandler INSTANCE = new NoPostHandler();

	private NoPostHandler() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepareImage(AbstractChartComponent comp, DisplayContext aContext, String anImageId,
			Dimension aDimension, Map dimensions, String aKey) {
		// do nothing
	}

	@Override
	public void postSetConfig(AbstractChartComponent configuredChartComponent) {
		// do nothing
	}

}