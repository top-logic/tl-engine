/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
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
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
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
	protected Map<String, ChannelSPI> programmaticChannels() {
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
	 * @param src
	 *        The script source code.
	 */
	public HandlerResult execute(SearchExpression expression, boolean withCommit, Provider<String> src) {
		Collection<?> results;
		try {
			if (withCommit) {
				try (Transaction tx =
					kb().beginTransaction(I18NConstants.EXECUTED_CUSTOM_SCRIPT__SCRIPT.fill(src.get()))) {
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

		Predicate<TLObject> securityFilter = securityFilter();
		Set<TLClass> searchedTypes = SearchUtil.getSearchedTypes(expression);
		if (searchedTypes.isEmpty() && !results.isEmpty()) {
			// Cannot determine static type of query, use typing by example.
			searchedTypes = new HashSet<>();
			TLClass resultType = null;
			TLClass multiResultType = null;
			TLClassProperty resultPart = null;
			TLClassProperty resultsPart = null;
			Collection<Object> resultObjects = new ArrayList<>(results.size());
			for (Object result : results) {
				if (result instanceof TLObject item) {
					if (!securityFilter.test(item)) {
						// Not allowed
						continue;
					}
					TLStructuredType type = item.tType();
					if (type instanceof TLClass) {
						searchedTypes.add((TLClass) type);
					}
					resultObjects.add(result);
				} else {
					boolean multipleResult = false;
					if (result != null) {
						if (result.getClass().isArray()) {
							result = Arrays.asList((Object[]) result);
						}
						if (result instanceof Collection<?> colResult) {
							List<Object> filtered = new ArrayList<>();
							for (Object singleResult : colResult) {
								if (singleResult instanceof TLObject item && !securityFilter.test(item)) {
									// Not allowed
									continue;
								}
								filtered.add(singleResult);
							}
							result = filtered;
							multipleResult = true;
						}
					}
					if (multipleResult) {
						if (multiResultType == null) {
							multiResultType =
								TransientModelFactory.createTransientClass(ModelService.getApplicationModel(),
									"MultiSearchResult");
							resultsPart = TransientModelFactory.addClassProperty(multiResultType, "results",
								TLModelUtil.findType(TypeSpec.OBJECT_TYPE));
							resultsPart.setMultiple(true);
							searchedTypes.add(multiResultType);
						}
						TLObject obj = TransientModelFactory.createTransientObject(multiResultType);
						obj.tUpdate(resultsPart, result);
						resultObjects.add(obj);
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
			}
			results = resultObjects;
		} else {
			results = results.stream()
				.map(TLObject.class::cast)
				.filter(securityFilter)
				.toList();
		}
		Object resultSet = new AttributedSearchResultSet((Collection<TLObject>) results, searchedTypes, null, null);
		getResultChannel().set(resultSet);
		return HandlerResult.DEFAULT_RESULT;
	}

	private static Predicate<TLObject> securityFilter() {
		ModelAccessRights accessRights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		BoundCommandGroup cmdGroup = SimpleBoundCommandGroup.READ;
		return result -> accessRights.isAllowed(user, result, cmdGroup);
	}

	private Collection<?> getResults(SearchExpression expression) {
		KnowledgeBase defaultKnowledgeBase = kb();
		TLModel defaultTLModel = ModelService.getApplicationModel();
		QueryExecutor executor = QueryExecutor.compile(defaultKnowledgeBase, defaultTLModel, expression);
		Object result = executor.executeWith(executor.context(true, null, null), Args.none());

		// Note: Do not use SearchExpression.asCollection(result), since this decomposes maps into
		// entry sets, which makes results hard to interpret.
		if (result instanceof Collection<?> collection) {
			return collection;
		} else if (result == null) {
			return Collections.emptyList();
		} else if (result.getClass().isArray()) {
			return Arrays.asList((Object[]) result);
		} else {
			return Collections.singleton(result);
		}
	}

	private KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

}
