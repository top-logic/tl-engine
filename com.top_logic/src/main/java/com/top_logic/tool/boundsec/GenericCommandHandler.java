/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link CommandHandler} that is composed of a configurable chain of actions.
 */
@Label("Generic command")
@InApp
public class GenericCommandHandler extends AbstractCommandHandler implements WithPostCreateActions {

	/**
	 * Configuration options for {@link GenericCommandHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config, WithPostCreateActions.Config {
		// Sum interface.
	}

	private final List<PostCreateAction> _actions;

	/**
	 * Creates a {@link GenericCommandHandler}.
	 */
	public GenericCommandHandler(InstantiationContext context, Config config) {
		super(context, config);

		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		WithPostCreateActions.processCreateActions(_actions, aComponent, model);

		return HandlerResult.DEFAULT_RESULT;
	}

}
