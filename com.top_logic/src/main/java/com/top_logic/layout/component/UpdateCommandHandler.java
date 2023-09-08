/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Map;

import com.top_logic.base.chart.flex.control.FilterRefreshCommandHandler;
import com.top_logic.base.chart.flex.control.FilterRefreshable;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link UpdateCommandHandler} updates an {@link Updatable} with special care for
 * {@link FormComponent}.
 * 
 * Compare with {@link FilterRefreshable} / {@link FilterRefreshCommandHandler}
 */
public class UpdateCommandHandler extends AbstractCommandHandler {
    
    /** The unique identifier of this command handler. */
    public static final String COMMAND_ID = "update";
    
	/**
	 * Configuration options for {@link UpdateCommandHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config {
		@Override
		@FormattedDefault(TARGET_NULL)
		ModelSpec getTarget();
	}

	/**
	 * Creates a new {@link UpdateCommandHandler}.
	 */
    public UpdateCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        Updatable component = (Updatable) aComponent;

		if (component instanceof FormComponent) {
			FormComponent form = (FormComponent) component;
			if (form.hasFormContext() && !form.getFormContext().checkAll()) {
				// Do not update forms with invalid context
				return HandlerResult.DEFAULT_RESULT;
			}
        }
        component.update();
        
        return HandlerResult.DEFAULT_RESULT;
    }
    
}