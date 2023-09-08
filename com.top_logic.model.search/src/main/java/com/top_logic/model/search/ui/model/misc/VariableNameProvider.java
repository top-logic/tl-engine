/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.misc;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.FilterContainer;
import com.top_logic.model.search.ui.model.NamedDefinition;
import com.top_logic.model.search.ui.model.ui.TLNamedPartResourceProvider;

/**
 * {@link ConfigurationListener} that sets the names of variables in
 * {@link FilterContainer#setName(String)}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class VariableNameProvider implements ConfigurationListener {

	private static final String UNKNOWN_TYPE_VARIABLE_PREFIX = "X";

	private static final int VARIABLE_PREFIX_SIZE = UNKNOWN_TYPE_VARIABLE_PREFIX.length();

	private final NamedDefinition _valueContext;

	private final NameGenerator _nameGenerator;

	VariableNameProvider(NamedDefinition valueContext, NameGenerator nameGenerator) {
		_valueContext = valueContext;
		_nameGenerator = nameGenerator;
	}

	@Override
	public void onChange(ConfigurationChange change) {
		String varName = getPrefix() + getSuffix();
		_valueContext.setName(varName);
	}

	private String getPrefix() {
		TLType type = _valueContext.getValueType();
		if (type != null) {
			String typeName = TLNamedPartResourceProvider.getInternationalizedName(type);
			return typeName.substring(0, VARIABLE_PREFIX_SIZE).toUpperCase();
		} else {
			return UNKNOWN_TYPE_VARIABLE_PREFIX;
		}
	}

	private String getSuffix() {
		String currentName = _valueContext.getName();
		if (currentName.length() > VARIABLE_PREFIX_SIZE) {
			return currentName.substring(VARIABLE_PREFIX_SIZE);
		} else {
			return _nameGenerator.newName();
		}
	}

}
