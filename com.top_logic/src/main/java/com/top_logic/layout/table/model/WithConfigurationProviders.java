/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Mix-in interface for adding {@link TableConfigurationProvider} configurations to some
 * configuration.
 */
@Abstract
public interface WithConfigurationProviders extends ConfigurationItem {

	/**
	 * @see #getConfigurationProviders()
	 */
	String CONFIGURATION_PROVIDERS = "configurationProviders";

	/**
	 * Plug-ins to the table configuration.
	 */
	@Name(CONFIGURATION_PROVIDERS)
	@Label("Column configurations")
	@Options(fun = AllInAppImplementations.class)
	List<PolymorphicConfiguration<? extends TableConfigurationProvider>> getConfigurationProviders();

}
