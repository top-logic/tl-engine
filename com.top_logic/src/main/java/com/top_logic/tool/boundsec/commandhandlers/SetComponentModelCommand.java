/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Sets the given model as the model of the {@link LayoutComponent}.
 * <p>
 * This can be used as a component local "goto" command. See {@link OpenViewForModelCommand} for
 * more information.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SetComponentModelCommand extends AbstractCommandHandler {

	/** {@link ConfigurationItem} for the {@link SetComponentModelCommand}. */
	public interface Config extends AbstractCommandHandler.Config {

		/** The default value for {@link #getId()}. */
		String ID = "setComponentModel";

		@Override
		@StringDefault(ID)
		String getId();

		@Override
		@StringDefault(CommandHandlerFactory.INTERNAL_GROUP)
		String getClique();

	}

	/** {@link TypedConfiguration} constructor for {@link SetComponentModelCommand}. */
	public SetComponentModelCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		component.setModel(model);
		return HandlerResult.DEFAULT_RESULT;
	}

}
