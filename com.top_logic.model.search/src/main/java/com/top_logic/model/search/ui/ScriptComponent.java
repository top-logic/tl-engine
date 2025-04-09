/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.TypedChannelSPI;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.model.ModelService;

/**
 * {@link Layout} that can execute a {@link SearchExpression} and delivers the result in a result
 * channel.
 *
 * @author <a href="daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ScriptComponent extends BoundLayout {

	private static final String RESULT_CHANNEL_NAME = "result";

	private static final Map<String, ChannelSPI> MODEL_AND_RESULT_CHANNEL = channels(MODEL_CHANNEL,
		new TypedChannelSPI<>(RESULT_CHANNEL_NAME, AttributedSearchResultSet.class, null));

	/**
	 * Creates a {@link ScriptComponent}.
	 */
	public ScriptComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	protected Map<String, ChannelSPI> channels() {
		return MODEL_AND_RESULT_CHANNEL;
	}

	/**
	 * Channel containing the results of a model based search.
	 */
	public ComponentChannel getResultChannel() {
		return getChannel(RESULT_CHANNEL_NAME);
	}

	/**
	 * Executes the given {@link SearchExpression} and propagates the result on the
	 * {@link #getResultChannel() result} channel.
	 * 
	 * @param expression
	 *        Search model.
	 * @param withCommit
	 *        Whether data changes made by the expression are allowed. If changes are made, they are
	 *        stored. Otherwise executing the expression fails.
	 */
	public HandlerResult execute(SearchExpression expression, boolean withCommit) {
		Collection<?> results;
		try {
			if (withCommit) {
				try (Transaction tx = kb().beginTransaction()) {
					results = getResults(expression);
					tx.commit();
				}
			} else {
				results = kb().withoutModifications(() -> getResults(expression));
			}
		} catch (RuntimeException ex) {
			HandlerResult error = HandlerResult.error(I18NConstants.ERROR_EXECUTING_SEARCH__EXPR.fill(expression), ex);
			error.setErrorSeverity(ErrorSeverity.WARNING);
			return error;
		}

		Set<TLClass> searchedTypes = SearchUtil.getSearchedTypes(expression);
		if (searchedTypes.isEmpty() && !results.isEmpty()) {
			// Cannot determine static type of query, use typing by example.
			searchedTypes = new HashSet<>();
			TLClass resultType = null;
			TLClassProperty resultPart = null;
			Collection<Object> resultObjects = new ArrayList<>(results.size());
			for (Object result : results) {
				if (result instanceof TLObject) {
					TLStructuredType type = ((TLObject) result).tType();
					if (type instanceof TLClass) {
						searchedTypes.add((TLClass) type);
					}
					resultObjects.add(result);
				} else {
					if (resultType == null) {
						resultType = TransientModelFactory.createTransientClass(ModelService.getApplicationModel(),
							"SearchResult");
						resultPart = TransientModelFactory.addClassProperty(resultType, "result",
							TLModelUtil.findType(TypeSpec.OBJECT_TYPE));
						searchedTypes.add(resultType);
					}

					TLObject obj = TransientModelFactory.createTransientObject(resultType);
					obj.tUpdate(resultPart, result);
					resultObjects.add(obj);
				}
			}
			results = resultObjects;
		}
		Object resultSet = new AttributedSearchResultSet((Collection<TLObject>) results, searchedTypes, null, null);
		getResultChannel().set(resultSet);
		return HandlerResult.DEFAULT_RESULT;
	}

	private Collection<?> getResults(SearchExpression expression) {
		KnowledgeBase defaultKnowledgeBase = kb();
		TLModel defaultTLModel = ModelService.getApplicationModel();
		QueryExecutor executor = QueryExecutor.compile(defaultKnowledgeBase, defaultTLModel, expression);
		Object result = executor.executeWith(executor.context(true, null, null), Args.none());

		return SearchExpression.asCollection(result);
	}

	private KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

}
