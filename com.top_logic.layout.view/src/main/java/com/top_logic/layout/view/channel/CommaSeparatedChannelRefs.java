/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.CommaSeparatedListValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Parses a comma-separated list of channel references from a configuration attribute value.
 *
 * <p>
 * Example: {@code inputs="selectedCustomer,editMode"} parses to a list of two {@link ChannelRef}
 * instances.
 * </p>
 */
public class CommaSeparatedChannelRefs extends CommaSeparatedListValueProvider<ChannelRef> {

	/** Singleton instance. */
	public static final CommaSeparatedChannelRefs INSTANCE = new CommaSeparatedChannelRefs();

	@Override
	protected ChannelRef parseSingleValue(String propertyName, CharSequence propertyValue,
			String singlePropertyValue) throws ConfigurationException {
		return ChannelRefFormat.INSTANCE.getValueNonEmpty(propertyName, singlePropertyValue);
	}

	@Override
	protected String formatSingleValue(ChannelRef value) {
		return ChannelRefFormat.INSTANCE.getSpecificationNonNull(value);
	}
}
