/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import java.util.List;

import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.ImageField;

/**
 * Algorithm retrieving table information from a chart.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface ChartGotoAware {

    public List getSearchResultColumns();

    /** 
     * @param aChartName name of the  (A {@link FormGroup} or {@link ImageField} to) retrieve the values from.
     * @param aSeries may be -1 indicating "all"
     * @param anItem  may be -1 indicating "all"
     * 
     * @return A List of Items to be shown after the Goto.
     */
    public List getGotoItems(String aChartName, int aSeries, int anItem);
}

