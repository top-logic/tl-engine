/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.table;

import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.reporting.report.model.FilterVO;

/**
 * @author     <a href="mailto:olb@top-logic.com">olb</a>
 */
public interface FilterAwareModelBuilder {
	public void prepareModel(FilterVO filter, DefaultProgressInfo progressInfo);
	
	public FilterVO getFilterVO();
}
