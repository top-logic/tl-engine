/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui.handler;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.graphic.flow.data.ClickTarget;
import com.top_logic.graphic.flow.data.DropRegion;
import com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder.Config.HandlerDefinition;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.WithTransaction;

/**
 * {@link ServerDropHandler} that can be implemented in TL-Script.
 */
public class ScriptedDropHandler extends AbstractConfiguredInstance<ScriptedDropHandler.Config<?>>
		implements ServerDropHandler, WithPostCreateActions, WithTransaction<ScriptedDropHandler.Config<?>> {

	/**
	 * Configuration options for {@link ScriptedDropHandler}.
	 */
	public interface Config<I extends ScriptedDropHandler>
			extends HandlerDefinition<I>, WithPostCreateActions.Config, WithTransaction.Config {

		/**
		 * Function that retrieves the {@link DropRegion} diagram element, the dragged objects and
		 * the model of the drag source from which the drag operation was initiated.
		 * 
		 * <pre>
		 * <code>node -> dragged -> source -> ...</code>
		 * </pre>
		 * 
		 * <p>
		 * The function's result is passed to configured {@link #getPostCreateActions() UI actions}.
		 * </p>
		 * 
		 * <p>
		 * If no script is provided, a list with the {@link ClickTarget#getUserObject() user object}
		 * of the clicked diagram element as first element and the list of dragged objects as second
		 * element is used as input for {@link #getPostCreateActions() further UI actions}.
		 * </p>
		 */
		Expr getScript();

	}

	private final QueryExecutor _script;

	private final List<PostCreateAction> _actions;

	private LayoutComponent _component;

	/**
	 * Creates a {@link ScriptedDropHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScriptedDropHandler(InstantiationContext context, Config<?> config) {
		super(context, config);
		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
		_script = QueryExecutor.compileOptional(config.getScript());
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
	}

	@Override
	public void onDrop(DropRegion target, DndData data) {
		Object result;
		Object userObject = target.getUserObject();
		if (_script == null) {
			result = Arrays.asList(userObject, data.getDragData(), data.getSource().getDragSourceModel());
		} else {
			Transaction tx = beginTransaction(
				I18NConstants.DIAGRAM_DROP_ACTION__TARGET.fill(MetaLabelProvider.INSTANCE.getLabel(userObject)),
				getConfig().isInTransaction());
			try {
				result = _script.execute(target, data.getDragData(), data.getSource().getDragSourceModel());
			} finally {
				tx.commit();
			}
		}

		WithPostCreateActions.processCreateActions(_actions, _component, result);
	}

}
