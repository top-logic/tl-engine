/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui.handler;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.graphic.flow.callback.ClickHandler;
import com.top_logic.graphic.flow.data.ClickTarget;
import com.top_logic.graphic.flow.data.MouseButton;
import com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder.Config.HandlerDefinition;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ClickHandler} that can be implemented in TL-Script.
 */
public class ScriptedClickHandler extends AbstractConfiguredInstance<ScriptedClickHandler.Config<?>>
		implements ClickHandler, WithPostCreateActions {

	/**
	 * Configuration options for {@link ScriptedClickHandler}.
	 */
	public interface Config<I extends ScriptedClickHandler> extends HandlerDefinition<I>, WithPostCreateActions.Config {

		/**
		 * Function that retrieves the {@link ClickTarget} diagram element.
		 * 
		 * <p>
		 * The function's result is passed to configured {@link #getPostCreateActions() UI actions}.
		 * </p>
		 * 
		 * <p>
		 * If no script is provided, the {@link ClickTarget#getUserObject() user object} of the
		 * clicked diagram element is used as input for {@link #getPostCreateActions() further UI
		 * actions}.
		 * </p>
		 */
		Expr getScript();

	}

	private final QueryExecutor _script;

	private final List<PostCreateAction> _actions;

	private LayoutComponent _component;

	/**
	 * Creates a {@link ScriptedClickHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptedClickHandler(InstantiationContext context, Config<?> config) {
		super(context, config);
		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
		_script = QueryExecutor.compileOptional(config.getScript());
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
	}

	@Override
	public void onClick(ClickTarget target, Set<MouseButton> buttons) {
		Object result;
		if (_script == null) {
			result = target.getUserObject();
		} else {
			result = _script.execute(target);
		}

		WithPostCreateActions.processCreateActions(_actions, _component, result);
	}

}
