/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.TLModel;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.search.expr.AnnotatedSearchExpression;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link StorageMapping} for Storing {@link SearchExpression}s in source code.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractExprMapping extends AbstractConfigurationValueProvider<SearchExpression>
		implements StorageMapping<SearchExpression> {

	/**
	 * Creates a {@link AbstractExprMapping}.
	 */
	public AbstractExprMapping() {
		super(SearchExpression.class);
	}

	@Override
	public Class<SearchExpression> getApplicationType() {
		return SearchExpression.class;
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof AnnotatedSearchExpression;
	}

	@Override
	protected SearchExpression getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		return compile(propertyValue.toString());
	}

	@Override
	public SearchExpression getBusinessObject(Object aStorageObject) {
		if (StringServices.isEmpty(aStorageObject)) {
			return null;
		}
		try {
			return compile((String) aStorageObject);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	@Override
	protected String getSpecificationNonNull(SearchExpression configValue) {
		return serialize(configValue);
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject == null) {
			return null;
		}
		return serialize(aBusinessObject);
	}

	/**
	 * Implementation of {@link #getBusinessObject(Object)} and
	 * {@link #getValueNonEmpty(String, CharSequence)};
	 */
	protected abstract SearchExpression compile(String source) throws ConfigurationException;

	/**
	 * Implementation of {@link #getStorageObject(Object)} and
	 * {@link #getSpecificationNonNull(SearchExpression)}.
	 */
	protected String serialize(Object aBusinessObject) {
		AnnotatedSearchExpression expr = (AnnotatedSearchExpression) aBusinessObject;
		return expr.getSource();
	}

	/**
	 * Utility for translating the parse tree in an executable expression.
	 */
	protected final SearchExpression compile(String source, Expr parseTree) {
		TLModel model = ModelService.getApplicationModel();
		SearchExpression code = QueryExecutor.resolve(model, SearchBuilder.toSearchExpression(model, parseTree));
		return new AnnotatedSearchExpression(source, code);
	}

}
