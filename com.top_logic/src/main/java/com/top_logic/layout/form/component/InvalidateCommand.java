/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link CommandHandler} that {@link LayoutComponent#invalidate() updates} it's
 * {@link LayoutComponent}. In case of a {@link FormComponent#removeFormContext() resets} it's
 * {@link FormContext} in case of a {@link FormComponent}.
 * 
 * @see LayoutComponent#invalidate()
 * @see FormComponent#removeFormContext()
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
@InApp(classifiers = "commons")
@Label("Update view")
public class InvalidateCommand extends AJAXCommandHandler {

	/**
	 * Configuration options for {@link InvalidateCommand}.
	 */
	public interface Config extends AJAXCommandHandler.Config {

		@Override
		@FormattedDefault("class.com.top_logic.layout.form.component.I18NConstants.INVALIDATE")
		ResKey getResourceKey();

		@Override
		@FormattedDefault("theme:ICONS_BUTTON_REFRESH")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:ICONS_BUTTON_REFRESH_DISABLED")
		ThemeImage getDisabledImage();

		@Override
		@FormattedDefault(CommandHandlerFactory.REFRESH_GROUP)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault(TARGET_NULL)
		ModelSpec getTarget();

	}

    /** The name used to register this command in the CommandHandlerFactory */
    public static final String COMMAND = "invalidate";

    /** 
     * Create a new InvalidateCommand as {@link SimpleBoundCommandGroup#READ} command.
     */
    public InvalidateCommand(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.INVALIDATE;
    }
    
    /**
     * Do my Job.
     */
    @Override
	public HandlerResult handleCommand(DisplayContext ignored0, 
            LayoutComponent aComponent, Object model, Map<String, Object> ignored1) {
        aComponent.invalidate();
        return HandlerResult.DEFAULT_RESULT;
    }

}

