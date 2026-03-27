/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link InputAdapter} that evaluates a TL-Script expression to produce input.
 *
 * <p>
 * The script is obtained from a {@link ScriptInput} configuration and compiled eagerly in the
 * constructor. The compiled {@link QueryExecutor} is then reused for each
 * {@link #gatherInput(BackgroundContext)} call.
 * </p>
 */
public class ScriptInputAdapter implements InputAdapter {

	private final QueryExecutor _executor;

	/**
	 * Creates a new {@link ScriptInputAdapter} from the given configuration.
	 *
	 * <p>
	 * Compiles the TL-Script expression into a {@link QueryExecutor} for efficient repeated
	 * evaluation.
	 * </p>
	 *
	 * @param config
	 *        The script input configuration.
	 */
	public ScriptInputAdapter(ScriptInput config) {
		TLModel model = ModelService.getApplicationModel();
		SearchExpression searchExpr = SearchBuilder.toSearchExpression(model, config.getScript());

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		_executor = QueryExecutor.compile(kb, model, searchExpr);
	}

	@Override
	public String gatherInput(BackgroundContext context) {
		try {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

			Object result = kb.withoutModifications(() ->
				_executor.executeWith(
					_executor.context(true, null, null),
					Args.none()));

			return result != null ? result.toString() : "";
		} catch (Exception ex) {
			Logger.error("Script input evaluation failed for agent '" + context.getAgentName() + "'",
				ex, ScriptInputAdapter.class);
			return "";
		}
	}
}
