/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.info.swing;

import java.awt.Color;

/**
 * The SpecialBarInfo is related to an other special or normal bar. 
 * 
 * But somewhere before the special bar must be a normal bar.
 * Theses are in fact rendering hints for the 
 * {@link com.top_logic.reporting.chart.renderer.SwingRenderer}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class SpecialBarInfo extends SwingRenderingInfo {

    /** 
     * Creates a {@link SpecialBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param aColor        A color.
     */
    public SpecialBarInfo(double aTooltipValue, Color aColor) {
        this(aTooltipValue, 0, aColor);
    }

    /** 
     * Creates a {@link SpecialBarInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param anAxis        A related axis.
     * @param aColor        A color.
     */
    public SpecialBarInfo(double aTooltipValue, int anAxis, Color aColor) {
        super();
        setColor(aColor);
        setDrawLineToPrev(true);
        setNormalBar(false);
        setShowAsIcon(false);
        setValueAxisIndex(anAxis);
        setValue(aTooltipValue);
    }
    
}

