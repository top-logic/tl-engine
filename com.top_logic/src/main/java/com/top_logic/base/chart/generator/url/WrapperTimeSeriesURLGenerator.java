/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.url;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * The WrapperTimeSeriesURLGenerator generates tooltips for {@link Wrapper}s.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class WrapperTimeSeriesURLGenerator extends ExtendedTimeSeriesCollectionURLGenerator {

	private ComponentName destinationName;

    /**
     * Creates a {@link WrapperTimeSeriesURLGenerator}.
     * 
     * @param aDestinationName
     *        The name of the destination layout component. Maybe null.
     */
    public WrapperTimeSeriesURLGenerator(ComponentName aDestinationName) {
        this.destinationName = aDestinationName;
    }

    @Override
	public String getURLFor(Object aWrapper) {
		return "javascript:" + GotoHandler.getJSCallStatement(aWrapper, this.destinationName);
    }

}

