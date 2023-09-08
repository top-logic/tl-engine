/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 *Class for setting the personal homepage
 * 
 * @author    <a href=mailto:fma@top-logic.com>fma</a>
 */
public class SetHomepageHandler extends AbstractCommandHandler {

	public final static String COMMAND = "setHomepage";

    public SetHomepageHandler(InstantiationContext context, Config config) {
		super(context, config);
    }
    
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        HandlerResult   theResult    =  HandlerResult.DEFAULT_RESULT;
        Logger.info("Set homepage. Does nothing, use other Handler (e.g. CompoundSetHomepageHandler) for a real action.",this);        
        return theResult;
    }

}
