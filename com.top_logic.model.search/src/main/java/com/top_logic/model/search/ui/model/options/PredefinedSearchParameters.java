/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.model.search.ui.model.SearchPartFactory.*;

import java.util.List;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.expr.supplier.SupplierSearchExpressionBuilder;
import com.top_logic.model.search.ui.model.parameters.PredefinedSearchParameter;

/**
 * Returns the predefined parameters.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class PredefinedSearchParameters extends Function1<List<? extends PredefinedSearchParameter>, String> {

	@Override
	public List<PredefinedSearchParameter> apply(String searchName) {
		if (searchName == null) {
			return null;
		}
		return createParameters(getPredefinedParameterBuilders(searchName));
	}

	private List<SupplierSearchExpressionBuilder> getPredefinedParameterBuilders(String searchName) {
		return ModelBasedSearch.getInstance().getPredefinedParameters(searchName);
	}

	private List<PredefinedSearchParameter> createParameters(List<SupplierSearchExpressionBuilder> expressionBuilders) {
		List<PredefinedSearchParameter> parameters = list();
		for (SupplierSearchExpressionBuilder expressionBuilder : expressionBuilders) {
			parameters.add(createParameter(expressionBuilder));
		}
		return parameters;
	}

	private PredefinedSearchParameter createParameter(SupplierSearchExpressionBuilder supplier) {
		ResKey label = ResKey.forClass(supplier.getClass());
		Object value = supplier.getValue();
		TLType type = supplier.getType();
		boolean isMultiple = supplier.isMultiple();
		return create(label, value, type, isMultiple);
	}

}
