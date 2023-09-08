/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Default DataContext for charts created and displayed in {@link LayoutComponent}s.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface ComponentDataContext extends DataContext {

	/**
	 * Returns the component-context of the chart. E.g. if the model is necessary to provide
	 * raw-data.
	 */
	LayoutComponent getComponent();

}
