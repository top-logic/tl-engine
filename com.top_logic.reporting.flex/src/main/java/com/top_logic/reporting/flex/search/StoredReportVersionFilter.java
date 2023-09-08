/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search;

import com.top_logic.basic.col.Filter;
import com.top_logic.reporting.flex.search.model.FlexReport;

/**
 * @author     <a href="mailto:cca@top-logic.com">cca</a>
 */
public class StoredReportVersionFilter implements Filter<FlexReport> {

	private final double _version;

	public StoredReportVersionFilter(double version) {
		_version = version;
	}

	@Override
	public boolean accept(FlexReport anObject) {
		return anObject.getFormatVersion() == _version;
	}

}
