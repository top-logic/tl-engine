/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.ui.ScriptComponent;
import com.top_logic.model.search.ui.SearchExpressionEditor;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} for the TL-Script console, that executes an entered script expression.
 * 
 * <p>
 * If the handler is configured with {@link Config#isWithCommit()} the script expression may modify
 * persistent data, such as create or delete {@link TLObject}s.
 * </p>
 * 
 * @author <a href="daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ScriptConsoleHandler extends AbstractCommandHandler {

	/**
	 * Configuration of a {@link ScriptConsoleHandler}.
	 *
	 * @author <a href="daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {
		
		/**
		 * Name of the {@link ScriptComponent} that actually executes the script.
		 */
		@Mandatory
		ComponentName getScriptComponent();


		/**
		 * Whether the script is executed in a {@link Transaction} context.
		 */
		boolean isWithCommit();

	}

	private ScriptComponent _scriptComponent;

	/**
	 * Creates a {@link ScriptConsoleHandler}.
	 */
	public ScriptConsoleHandler(InstantiationContext context, Config config) {
		super(context, config);
		context.resolveReference(config.getScriptComponent(), LayoutComponent.class,
			scriptComponent -> _scriptComponent = (ScriptComponent) scriptComponent);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		return search(aComponent);
	}

	private HandlerResult search(LayoutComponent searchEditor) {
		return ((SearchExpressionEditor) searchEditor).search(expr -> {
			if (expr != null) {
				return _scriptComponent.execute(expr, config().isWithCommit());
			}
			return HandlerResult.DEFAULT_RESULT;
		});
	}

}
