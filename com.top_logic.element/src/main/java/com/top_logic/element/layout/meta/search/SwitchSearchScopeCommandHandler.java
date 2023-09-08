/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SwitchSearchScopeCommandHandler extends AJAXCommandHandler {
    
    public static final String COMMAND_ID = "switchAttributedSearchScope";

    public SwitchSearchScopeCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> aArguments) {
        AttributedSearchComponent theComp   = (AttributedSearchComponent) aComponent;
        HandlerResult             theResult = HandlerResult.DEFAULT_RESULT; 
        try {
            this.switchSearchScope(theComp);
            theComp.invalidateButtons();
        }
        catch (Exception ex) {
        	theResult = new HandlerResult();
        	theResult.setException(new TopLogicException(this.getClass(), "element.search.query.switch.failed", ex));
            Logger.error("Failed to switch search scope for " + theComp, ex, this);
        }

        return theResult;
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.SWITCH_TO_EXTENDED_MODE;
    }

    public void switchSearchScope(AttributedSearchComponent aComp) {
        String       theScope = aComp.getSearchScope();
        CommandModel theModel = aComp.getButtonComponent().getCommandModel(this);
        String       theNewScope;
		ResKey theLabel;

        if (AttributedSearchComponent.NORMAL_SCOPE.equals(theScope)) {
            theNewScope = AttributedSearchComponent.EXTENDED_SCOPE;
			theLabel = I18NConstants.SWITCH_TO_NORMAL_MODE;
        }
        else {
            theNewScope = AttributedSearchComponent.NORMAL_SCOPE;
			theLabel = I18NConstants.SWITCH_TO_EXTENDED_MODE;
        }
        aComp.setSearchScope(theNewScope);

        if (theModel != null) {
            theModel.setLabel(Resources.getInstance().getString(theLabel));
        }
    }
    
}

