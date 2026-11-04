/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.table;

import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.reporting.report.model.FilterVO;

/**
 * Model builder computing its model from filter settings.
 *
 * @author     <a href="mailto:olb@top-logic.com">olb</a>
 */
public interface FilterAwareModelBuilder {
	/**
	 * Computes the model for the given filter settings.
	 *
	 * @param progressInfo
	 *        Progress information to update during the computation.
	 */
	public void prepareModel(FilterVO filter, DefaultProgressInfo progressInfo);
	
	/** The filter settings the model was computed for. */
	public FilterVO getFilterVO();
}
