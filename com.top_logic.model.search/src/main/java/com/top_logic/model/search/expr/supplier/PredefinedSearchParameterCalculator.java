/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.supplier;

import static com.top_logic.basic.col.FilterUtil.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.model.search.ModelBasedSearch.SearchConfig;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Calculates the effective {@link SupplierSearchExpressionBuilder} for a given
 * {@link SearchConfig}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class PredefinedSearchParameterCalculator
		implements Function<SearchConfig, List<SupplierSearchExpressionBuilder.Config>> {

	/** The {@link PredefinedSearchParameterCalculator} instance. */
	public static final PredefinedSearchParameterCalculator INSTANCE = new PredefinedSearchParameterCalculator();

	@Override
	public List<SupplierSearchExpressionBuilder.Config> apply(SearchConfig searchConfig) {
		List<SupplierSearchExpressionBuilder.Config> predefinedParameters = list();
		predefinedParameters.addAll(filterPredefinedParameters(getSearchMethods()));
		removeAll(predefinedParameters, searchConfig.getDisabledPredefinedParameters());
		predefinedParameters.addAll(searchConfig.getAdditionalPredefinedParameters());
		return predefinedParameters;
	}

	private void removeAll(List<SupplierSearchExpressionBuilder.Config> activeParameters,
			List<SupplierSearchExpressionBuilder.Config> disabledParameters) {
		for (SupplierSearchExpressionBuilder.Config disabledParameter : disabledParameters) {
			remove(activeParameters, disabledParameter);
		}
	}

	private void remove(List<SupplierSearchExpressionBuilder.Config> activeParameters,
			SupplierSearchExpressionBuilder.Config disabledParameter) {
		int i = 0;
		for (SupplierSearchExpressionBuilder.Config activeParameter : activeParameters) {
			if (ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(disabledParameter, activeParameter)) {
				activeParameters.remove(i);
				return;
			}
			i += 1;
		}
	}

	private List<SupplierSearchExpressionBuilder.Config> filterPredefinedParameters(
			List<MethodBuilder.Config<?>> methods) {
		return filterList(SupplierSearchExpressionBuilder.Config.class, methods);
	}

	private List<MethodBuilder.Config<?>> getSearchMethods() {
		return SearchBuilder.getInstance().getConfig().getMethods();
	}

}
