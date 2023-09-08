/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder;


/**
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface ChartContext {

	/**
	 * Singleton instance for <code>NO_CONTEXT</code> - no urls will be rendered
	 */
	public static ChartContext NO_CONTEXT = new ChartContext() {

		@Override
		public String getUrl(String path) {
			return null;
		}
	};

	/**
	 * @param param
	 *        the param to be encoded in the generated URL
	 * @return a URL to that can be processed by the application
	 */
	public String getUrl(String param);

}
