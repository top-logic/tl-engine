/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.component;

import java.awt.Dimension;
import java.io.IOException;

import org.jfree.chart.JFreeChart;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.base.chart.ImageData;
import com.top_logic.base.chart.component.AbstractImageComponent;
import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.reporting.report.model.Report;

/**
 * {@link ImageComponent} for displaying {@link Report}s.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class ReportComponent extends AbstractImageComponent {

    /**
     * Creates a {@link ReportComponent}.
     */
    public ReportComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof Report;
    }

    @Override
	public FormContext createFormContext() {
        return null;
    }

    @Override
	public ImageData createImage(DisplayContext context, String imageId, String imageType, Dimension dimension)
			throws IOException {
        JFreeChart theChart = ((Report) getModel()).getChart();
		return ChartUtil.encode(isWriteToTempFile(), dimension, theChart);
    }

    @Override
	public HTMLFragment getImageMap(String imageId, String mapName, Dimension dimension)
			throws IOException {
		return null;
    }

    @Override
	public void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException {
		// nothing to prepare
    }
    
}

