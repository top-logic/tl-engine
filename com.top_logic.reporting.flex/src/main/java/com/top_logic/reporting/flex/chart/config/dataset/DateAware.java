/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.dataset;

import java.util.Date;

import org.jfree.data.time.TimeSeries;

/**
 * Implementers must provide a date e.g. to display in tooltips of {@link TimeSeries}-Charts.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public interface DateAware {

	/**
	 * the relevant date for this object
	 */
	Date getDate();

}
