/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.info.swing;

/**
 * The NormalBarIconInfo displays a normal bar as icon in light gray.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class NormalBarIconInfo extends NormalBarInfo{

    /**
     * Creates a {@link NormalBarIconInfo} with the given
     * parameters.
     * 
     * @param aTooltipValue A value which will be displayed as tooltip.
     * @param aIconOrientation The icon orientation
     *        {@link com.top_logic.reporting.chart.renderer.SwingRenderer#LEFT} or
     *        {@link com.top_logic.reporting.chart.renderer.SwingRenderer#RIGHT}.
     */
    public NormalBarIconInfo(double aTooltipValue, int aIconOrientation) {
        super(aTooltipValue, 0);
        setShowAsIcon(true);
        setIconOrientation(aIconOrientation);
    }
    
    /** 
     * Creates a {@link NormalBarIconInfo} with the
     * given parameters.
     * 
     * @param aTooltipValue    A value which will be displayed as tooltip.
     * @param aAxis            A related axis.
     * @param aIconOrientation The icon orientation
     *        {@link com.top_logic.reporting.chart.renderer.SwingRenderer#LEFT} or
     *        {@link com.top_logic.reporting.chart.renderer.SwingRenderer#RIGHT}.
     */
    public NormalBarIconInfo(double aTooltipValue, int aAxis, int aIconOrientation) {
        super(aTooltipValue, aAxis);
        setShowAsIcon(true);
        setIconOrientation(aIconOrientation);
    }
    
}

