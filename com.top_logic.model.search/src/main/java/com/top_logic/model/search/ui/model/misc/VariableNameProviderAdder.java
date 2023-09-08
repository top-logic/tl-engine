/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.misc;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.edit.initializer.Initializer;
import com.top_logic.model.search.ui.model.FilterContainer;
import com.top_logic.model.search.ui.model.NamedDefinition;

/**
 * {@link Initializer} that adds a {@link VariableNameProvider}s to each {@link FilterContainer}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class VariableNameProviderAdder implements Initializer {

	/**
	 * The instance of {@link VariableNameProviderAdder}.
	 */
	public static final VariableNameProviderAdder INSTANCE = new VariableNameProviderAdder();

	private final NameGenerator _nameGenerator = new NameGenerator();

	@Override
	public void init(ConfigurationItem model, PropertyDescriptor property, Object value) {
		if (!(value instanceof FilterContainer)) {
			return;
		}
		init((NamedDefinition) value);
	}

	private void init(NamedDefinition valueContext) {
		ConfigurationDescriptor valueDescriptor = valueContext.descriptor();
		ConfigurationListener nameUpdate = new VariableNameProvider(valueContext, _nameGenerator);
		PropertyDescriptor valueType = valueDescriptor.getProperty(FilterContainer.VALUE_TYPE);
		valueContext.addConfigurationListener(valueType, nameUpdate);
		nameUpdate.onChange(null);
	}

}
