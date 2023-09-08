/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

/**
 * An Interval defines a range using a begin and an end.
 * 
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public interface Interval {
	public Object getBegin();
	public Object getEnd();
	@Override
	public String toString();
}

