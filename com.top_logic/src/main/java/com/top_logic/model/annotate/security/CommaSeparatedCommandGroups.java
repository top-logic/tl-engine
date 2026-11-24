/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ListConfigValueProvider;
import com.top_logic.model.security.I18NConstants;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * Format for parsing a comma-separated list of operation (command group) names in configuration.
 *
 * <p>
 * Each entry in the comma-separated string becomes a {@link CommandGroupReference} to the trimmed
 * name. Empty entries, e.g. from a trailing comma, are skipped. A value that is not empty but
 * names no operation at all is rejected, since it would otherwise silently mean all operations.
 * </p>
 *
 * @see CommaSeparatedRoles
 */
public class CommaSeparatedCommandGroups extends ListConfigValueProvider<CommandGroupReference> {

	@Override
	protected List<CommandGroupReference> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String[] names = propertyValue.toString().trim().split("\\s*,\\s*");
		List<CommandGroupReference> result = new ArrayList<>(names.length);
		for (String name : names) {
			if (name.isEmpty()) {
				continue;
			}
			result.add(new CommandGroupReference(name));
		}
		if (result.isEmpty()) {
			throw new ConfigurationException(
				I18NConstants.ERROR_NO_OPERATION_NAMES__PROPERTY_VALUE.fill(propertyName, propertyValue),
				propertyName, propertyValue);
		}
		return result;
	}

	@Override
	protected String getSpecificationNonNull(List<CommandGroupReference> configValue) {
		return configValue.stream().map(CommandGroupReference::id).collect(Collectors.joining(", "));
	}

}
