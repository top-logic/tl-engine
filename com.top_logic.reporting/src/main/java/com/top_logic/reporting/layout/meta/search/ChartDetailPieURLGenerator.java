/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.data.general.PieDataset;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ChartDetailPieURLGenerator implements PieURLGenerator {

    private final String name;

    public ChartDetailPieURLGenerator(String aName) {
        this.name = aName;
    }

    @Override
	public String generateURL(PieDataset aDataset, Comparable aKey, int aPieIndex) {
        StringBuffer theOnClickFragment = new StringBuffer();

        theOnClickFragment.append("javascript:");
        theOnClickFragment.append(this.name + "(");
        theOnClickFragment.append("\'" + "chart" + "\',");
        theOnClickFragment.append("\'" + aPieIndex + "\',");
        theOnClickFragment.append("\'" + aDataset.getIndex(aKey) + "\')");
        
        return theOnClickFragment.toString();
    }
    
}
