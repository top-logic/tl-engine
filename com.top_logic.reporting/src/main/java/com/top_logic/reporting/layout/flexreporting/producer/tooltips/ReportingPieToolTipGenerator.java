/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.producer.tooltips;

import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.data.general.PieDataset;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ReportingPieToolTipGenerator extends ReportingTooltipGenerator implements PieToolTipGenerator {

    @Override
	public String generateToolTip(PieDataset aDataset, Comparable aKey) {
        Number theCount = aDataset.getValue(aKey);

        return aKey.toString() + " (Anzahl: " + theCount + ")";
    }
}
