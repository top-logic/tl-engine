/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.flex.control;

import com.top_logic.base.chart.flex.exception.FilterRefreshException;

/**
 * Interface for filter components which are refreshable. If the the filter component 
 * is not up to date an refresh can be forced over the {@link #refresh()} method. 
 * 
 * TODO TDI this is the same pattern as found with UpdateCommandHandler/Updatable
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public interface FilterRefreshable {

    /**
     * This method refreshs the the filters of the component.
     * 
     * @throws FilterRefreshException
     *         Thrown if during the refreshing an error occurs.
     */
    public void refresh() throws FilterRefreshException;
    
}

