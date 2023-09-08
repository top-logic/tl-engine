/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.control.producer;

import com.top_logic.layout.form.model.FormContext;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ChartContextUtil {
    
    public static void copy(FormContext aContext, ChartContext aChartContext, String aKey) {
        aChartContext.setValue(aKey, aContext.getField(aKey).getValue());
    }

}

