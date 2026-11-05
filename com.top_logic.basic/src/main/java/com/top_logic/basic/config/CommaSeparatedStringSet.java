/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.StringServices;

/**
 * {@link ConfigurationValueProvider} that parses comma separated sets of strings.
 * 
 * <p>
 * The values are serialized in their natural order, so that the specification of a set does not
 * depend on the iteration order of the set.
 * </p>
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommaSeparatedStringSet extends SetConfigValueProvider<String>{

	/** Singleton instance of {@link CommaSeparatedStringSet} */
	public static final CommaSeparatedStringSet INSTANCE = new CommaSeparatedStringSet();

	private CommaSeparatedStringSet() {
		// singleton instance
	}

	@Override
	protected String getSpecificationNonNull(Set<String> configValue) {
		List<String> sorted = new ArrayList<>(configValue);
		Collections.sort(sorted);
		return StringServices.join(sorted, ",");
	}

	@Override
	protected Set<String> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		Set<String> result = StringServices.toSet(propertyValue, ',');
		return result == null ? new HashSet<>() : result;
	}

}

