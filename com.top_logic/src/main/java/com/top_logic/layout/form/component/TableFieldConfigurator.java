/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * Manipulation of {@link TableConfiguration} of {@link SelectField}'s that are potentially
 * displayed as tables.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableFieldConfigurator {

	/**
	 * Apply the configuration with the field's name from the component XML to the given field.
	 * 
	 * @see #makeConfigurable(SelectField, String)
	 */
	void makeConfigurable(SelectField field);
	
	/**
	 * Apply the configuration with the given name from the component XML to the given field.
	 */
	void makeConfigurable(SelectField field, String configName);
	
	/**
	 * Pushes the configuration with the given name from the component XML to the given
	 * {@link TableConfiguration}.
	 */
	void adaptTableConfiguration(String name, TableConfiguration table);
	
	/**
	 * Find the raw configuration increment with the given name from the component XML.
	 */
	TableConfigurationProvider lookupTableConfigurationBuilder(String name);

}
