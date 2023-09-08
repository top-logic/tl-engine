/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import com.top_logic.reporting.report.control.producer.ChartContext;

/**
 * A click information to be used in {@link AbstractMatrixChartProducer}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MatrixChartClickVO {
    
    public static final MatrixChartClickVO EMPTY = new MatrixChartClickVO();
    
    private ChartContext chartContext;
    
    private int clickX;
    private int clickY;

    public ChartContext getChartContext() {
        return (this.chartContext);
    }

    public int getClickX() {
        return (this.clickX);
    }

    public int getClickY() {
        return (this.clickY);
    }
    
    private MatrixChartClickVO() {
    }

    /**
	 * Creates a {@link MatrixChartClickVO}.
	 */
    public MatrixChartClickVO(ChartContext aContext, int aClickX, int aClickY) {
        super();
        this.chartContext = aContext;
        this.clickX = aClickX;
        this.clickY = aClickY;
    }
}

