/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.info.swing;

import java.awt.Color;

/**
 * The NormalBarInfo displays a normal bar in light gray.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class NormalBarInfo extends SwingRenderingInfo {

    /** 
     * Creates a {@link NormalBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     */
    public NormalBarInfo(double aTooltipValue) {
        this(aTooltipValue, 0);
    }

    
    /** 
     * Creates a {@link NormalBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param isDrawLine    Indicates whether a line is drawn to the previous bar.
     */
    public NormalBarInfo(double aTooltipValue, boolean isDrawLine) {
        this(aTooltipValue, 0);
        setDrawLineToPrev(isDrawLine);
    }
    
    /** 
     * Creates a {@link NormalBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param anAxis        A related axis.
     */
    public NormalBarInfo(double aTooltipValue, int anAxis) {
        this(aTooltipValue, anAxis, Color.LIGHT_GRAY, null);
    }

    /** 
     * Creates a {@link NormalBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param anAxis        A related axis.
     * @param aColor        A color.
     */
    public NormalBarInfo(double aTooltipValue, int anAxis, Color aColor) {
        this(aTooltipValue, anAxis, aColor, null);
    }

    public NormalBarInfo(double aTooltipValue, int anAxis, Color aColor, Object anInformation) {
        super();

        setDrawLineToPrev(true);
        setNormalBar(true);
        setShowAsIcon(false);
        setValueAxisIndex(anAxis);
        setValue(aTooltipValue);
        setColor(aColor);
        setInformation(anInformation);
    }

}
