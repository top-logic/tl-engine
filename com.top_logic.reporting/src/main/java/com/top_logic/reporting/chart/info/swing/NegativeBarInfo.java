/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.info.swing;

import java.awt.Color;

/**
 * The NegativeBarInfo is displayed in green and is related to the previous bar 
 * through a line. 
 * 
 * KHA: Could we create a "SignAwareBarInfo"
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class NegativeBarInfo extends SpecialBarInfo {
    
    /** 
     * Creates a {@link NegativeBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     */
    public NegativeBarInfo(double aTooltipValue) {
        super(aTooltipValue, 0, Color.GREEN);
    }
    
    /** 
     * Creates a {@link NegativeBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param anAxis        A related axis.
     */
    public NegativeBarInfo(double aTooltipValue, int anAxis) {
        super(aTooltipValue, anAxis, Color.GREEN);
    }
    
}

