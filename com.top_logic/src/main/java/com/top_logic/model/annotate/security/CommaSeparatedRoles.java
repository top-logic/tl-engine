/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.annotate.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ListConfigValueProvider;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Format for parsing a comma-separated list of role names in configuration.
 *
 * <p>
 * Each entry in the comma-separated string becomes a {@link RoleConfig} with the trimmed name as
 * its value.
 * </p>
 *
 * @see AccessGrant#getRoles()
 *
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommaSeparatedRoles extends ListConfigValueProvider<RoleConfig> {

	@Override
	protected List<RoleConfig> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String[] roleNames = propertyValue.toString().trim().split("\\s*,\\s*");
		List<RoleConfig> result = new ArrayList<>(roleNames.length);
		for (String role : roleNames) {
			RoleConfig config = TypedConfiguration.newConfigItem(RoleConfig.class);
			config.setName(role);
			result.add(config);
		}
		return result;
	}

	@Override
	protected String getSpecificationNonNull(List<RoleConfig> configValue) {
		return configValue.stream().map(RoleConfig::getName).collect(Collectors.joining(", "));
	}

}

