/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.persistency.expressions.SearchExpressionImpl;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link Command} that resets the search to the selected {@link SearchExpressionImpl}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ResetSearchCommand extends AJAXCommandHandler {

	/**
	 * The id under which this {@link CommandHandler} is registered at the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "resetModelSearch";

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ResetSearchCommand}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ResetSearchCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent untypedComponent,
			Object model, Map<String, Object> arguments) {
		SearchSelectorComponent component = (SearchSelectorComponent) untypedComponent;
		SearchExpressionImpl searchExpression = component.getSelected();
		try {
			component.loadExpression(searchExpression);
		} catch (ConfigurationException ex) {
			return HandlerResult.error(com.top_logic.element.layout.meta.expression.I18NConstants.ERROR_LOADING_QUERY,
				ex);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
