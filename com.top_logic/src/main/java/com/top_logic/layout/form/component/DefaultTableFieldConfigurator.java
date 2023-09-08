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
 * Default implementation of {@link TableFieldConfigurator} that has no configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTableFieldConfigurator implements TableFieldConfigurator {

	@Override
	public void makeConfigurable(SelectField field) {
		// No configuration.
	}

	@Override
	public void makeConfigurable(SelectField field, String configName) {
		// No configuration.
	}

	@Override
	public void adaptTableConfiguration(String name, TableConfiguration table) {
		// No configuration.
	}

	@Override
	public TableConfigurationProvider lookupTableConfigurationBuilder(String name) {
		return null;
	}

}
