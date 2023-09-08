/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.table;

import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.reporting.report.model.FilterVO;

/**
 * Simple implementation of {@link FilterAwareModelBuilder}.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class SimpleFilterAwareModelBuilder implements FilterAwareModelBuilder {

	private FilterVO filter;

	/**
	 * Create a new SimpleFilterAwareModelBuilder ...
	 */
	public SimpleFilterAwareModelBuilder() {
	}

	@Override
	public void prepareModel(FilterVO aFilter, DefaultProgressInfo progressInfo) {
		filter = aFilter;
	}

	@Override
	public FilterVO getFilterVO() {
		return filter;
	}

}
