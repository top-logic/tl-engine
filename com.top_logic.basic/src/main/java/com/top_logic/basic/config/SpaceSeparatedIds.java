/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.StringServices;

/**
 * {@link ConfigurationValueProvider} that parses identifiers separated by white space.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SpaceSeparatedIds extends ListConfigValueProvider<String> {

	/** Singleton instance of {@link SpaceSeparatedIds} */
	public static final ConfigurationValueProvider<List<String>> INSTANCE = new SpaceSeparatedIds();
	
	private SpaceSeparatedIds() {
		// Singleton constructor.
	}
	
	@Override
	public String getSpecificationNonNull(List<String> configValue) {
		return StringServices.join(configValue, " ");
	}

	@Override
	public List<String> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String[] result = propertyValue.toString().trim().split("\\s+");
		return new ArrayList<>(Arrays.asList(result));
	}

}
