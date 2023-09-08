/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;

/**
 * {@link ConfigurationValueProvider} that parses comma separated string lists.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommaSeparatedStrings extends ListConfigValueProvider<String> {

	/** Singleton instance of {@link CommaSeparatedStrings} */
	public static final ConfigurationValueProvider<List<String>> INSTANCE = new CommaSeparatedStrings();
	
	private CommaSeparatedStrings() {
		// Singleton constructor.
	}
	
	@Override
	public String getSpecificationNonNull(List<String> configValue) {
		return StringServices.join(configValue, ",");
	}

	@Override
	public List<String> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		List<String> result = StringServices.toList(propertyValue, ',');
		return result == null ? new ArrayList<>() : result;
	}

}
