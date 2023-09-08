/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.Provider;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * {@link ArgumentDescriptorBuilder} creating an {@link ArgumentDescriptor} supporting named
 * arguments.
 */
public class ArgumentDescriptorBuilderImpl implements ArgumentDescriptorBuilder {

	private int _argCnt = 0;

	private final Map<String, Integer> _argIndex = new LinkedHashMap<>();

	private final List<Provider<SearchExpression>> _defaultProvider = new ArrayList<>();

	private final List<String> _names = new ArrayList<>();

	@Override
	public ArgumentDescriptor build() {
		return new ArgumentDescriptorImpl(_argCnt) {
			@Override
			protected int getArgumentIndex(String argumentName) {
				Integer result = _argIndex.get(argumentName);
				if (result != null) {
					return result.intValue();
				}
				return super.getArgumentIndex(argumentName);
			}

			@Override
			protected Collection<String> getArgumentNames() {
				return _argIndex.keySet();
			}

			@Override
			protected SearchExpression getDefaultValue(int argumentIndex) {
				Provider<SearchExpression> provider = _defaultProvider.get(argumentIndex);
				if (provider == null) {
					return null;
				}
				return provider.get();
			}

			@Override
			protected String getArgumentName(int n) {
				String result = _names.get(n);
				if (result != null) {
					return result;
				}
				return super.getArgumentName(n);
			}
		};
	}

	@Override
	public ArgumentDescriptorBuilder mandatory(String name) {
		addArg(name);
		_defaultProvider.add(null);
		return this;
	}

	@Override
	public ArgumentDescriptorBuilder optional(String name, Provider<SearchExpression> defaultProvider) {
		addArg(name);
		_defaultProvider.add(defaultProvider);
		return this;
	}

	private void addArg(String name) {
		_names.add(name);
		_argIndex.put(name, Integer.valueOf(_argCnt++));
	}

}
