/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.info.swing;

import java.awt.Color;

/**
 * The OverwriteBarInfo displays a bar overlapping an other bar or over an part of an other bar.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class OverwriteBarInfo extends SpecialBarInfo {

    /** 
     * Creates a {@link OverwriteBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param aColor        A color.
     */
    public OverwriteBarInfo(double aTooltipValue, Color aColor) {
        this(aTooltipValue, aColor, 0);
    }
    
    /** 
     * Creates a {@link OverwriteBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param aColor        A color.
     */
    public OverwriteBarInfo(double aTooltipValue, Color aColor, int anAxisIndex) {
        this(aTooltipValue, aColor, anAxisIndex, null);
    }

    public OverwriteBarInfo(double aTooltipValue, Color aColor, int anAxisIndex, Object anInformation) {
        super(aTooltipValue, 0, aColor);
        setValueAxisIndex(anAxisIndex);
        this.setInformation(anInformation);
        setOverwritePrev(true);
    }
    
    /** 
     * Creates a {@link OverwriteBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param anAxis        A related axis.
     * @param aColor        A color.
     */
    public OverwriteBarInfo(double aTooltipValue, int anAxis, Color aColor) {
        super(aTooltipValue, anAxis, aColor);
        setOverwritePrev(true);
    }
    
}

