/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ListConfigValueProvider;

/**
 * {@link ConfigurationValueProvider} for a comma-separated list of strings that keeps empty
 * entries.
 * 
 * <p>
 * The specification is split at commas and each entry is trimmed. Every entry is kept, an empty
 * entry stands for the empty string. An empty specification is the list with the empty string as
 * its only entry. For example, <code>"1, true"</code> is the list <code>["1", "true"]</code>,
 * <code>"none,"</code> the list <code>["none", ""]</code>.
 * </p>
 * 
 * @see PropertySwitch.Config.ValueHandler
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CaseValues extends ListConfigValueProvider<String> {

	/** Singleton instance of {@link CaseValues}. */
	public static final ConfigurationValueProvider<List<String>> INSTANCE = new CaseValues();

	private CaseValues() {
		// Singleton constructor.
	}

	@Override
	protected List<String> getValueEmpty(String propertyName) throws ConfigurationException {
		List<String> result = new ArrayList<>();
		result.add(StringServices.EMPTY_STRING);
		return result;
	}

	@Override
	protected List<String> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		List<String> result = new ArrayList<>();
		for (String entry : propertyValue.toString().split(",", -1)) {
			result.add(entry.trim());
		}
		return result;
	}

	@Override
	protected String getSpecificationNonNull(List<String> configValue) {
		return StringServices.join(configValue, ",");
	}

}
