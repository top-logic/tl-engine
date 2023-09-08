/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.model;

import java.util.Date;

/**
 * {@link GanttObject} displayed at a certain date on a {@link GanttRow}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GanttDate extends GanttObject {

	private Date _date; // mandatory

	public Date getDate() {
		return _date;
	}

	public void setDate(Date date) {
		_date = date;
	}

}
