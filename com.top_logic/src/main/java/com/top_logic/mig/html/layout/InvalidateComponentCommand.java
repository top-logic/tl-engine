/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command that invalidates a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class InvalidateComponentCommand extends AbstractCommandHandler {

	/**
	 * Configuration of {@link InvalidateComponentCommand}.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Name of the component to validate.
		 */
		ComponentName getComponentName();
		
	}
	
	/**
	 * Creates an {@link InvalidateComponentCommand}.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public InvalidateComponentCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		component.getComponentByName(((Config) getConfig()).getComponentName()).invalidate();

		return HandlerResult.DEFAULT_RESULT;
	}

}
