/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.renderer;

import com.top_logic.reporting.chart.info.OverviewInfo;

/**
 * The OverviewRenderer draws a overview chart.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class OverviewRenderer extends TemplateRenderer {

    /** 
     * Creates a {@link OverviewRenderer}.
     */
    public OverviewRenderer() {
        super(new OverviewInfo());
    }
    
}

