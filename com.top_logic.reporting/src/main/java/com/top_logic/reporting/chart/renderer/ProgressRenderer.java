/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.renderer;

import com.top_logic.reporting.chart.info.ProgressInfo;

/**
 * The ProgressRenderer draws a progress chart.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ProgressRenderer extends TemplateRenderer {

    /** 
     * Creates a {@link ProgressRenderer}.
     */
    public ProgressRenderer() {
        super(new ProgressInfo());
    }
    
}

