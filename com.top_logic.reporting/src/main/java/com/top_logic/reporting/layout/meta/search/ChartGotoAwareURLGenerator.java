/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ChartGotoAwareURLGenerator implements XYURLGenerator {

    /** 
     * @see org.jfree.chart.urls.XYURLGenerator#generateURL(org.jfree.data.xy.XYDataset, int, int)
     */
    @Override
	public String generateURL(XYDataset aDataset, int aSeries, int anItem) {
        StringBuffer theBuffer = new StringBuffer("javascript:");

        theBuffer.append(ChartGotoAwareGotoHandler.COMMAND_ID + "(");
        theBuffer.append("\'" + aSeries + "\',");
        theBuffer.append("\'" + anItem + "\'");
        theBuffer.append(")");

        return theBuffer.toString();
    }
}

