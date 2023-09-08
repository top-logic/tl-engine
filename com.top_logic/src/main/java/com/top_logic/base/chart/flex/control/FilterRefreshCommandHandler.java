/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.flex.control;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The FilterRefreshCommandHandler refreshs filter components.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class FilterRefreshCommandHandler extends AJAXCommandHandler {

    /** The unique identifier of this command. */
    public static final String COMMAND_ID = "refreshFilters";

    public FilterRefreshCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        FilterRefreshable theComponent = (FilterRefreshable)aComponent;
        theComponent.refresh();
        
        return HandlerResult.DEFAULT_RESULT;
    }

}

