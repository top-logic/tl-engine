/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.CommaSeparatedListValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Parses a comma-separated list of {@link InputRef}s from a configuration attribute value.
 *
 * <p>
 * An entry is a channel name, optionally followed by the keyword {@value #AS} and the name of the
 * argument: {@code inputs="selection as ticket, draft"} names the value of the channel
 * {@code selection} {@code ticket}, the value of {@code draft} {@code draft}.
 * </p>
 */
public class CommaSeparatedInputRefs extends CommaSeparatedListValueProvider<InputRef> {

	/** Keyword separating the channel from the name of its argument. */
	public static final String AS = "as";

	/** Singleton instance. */
	public static final CommaSeparatedInputRefs INSTANCE = new CommaSeparatedInputRefs();

	@Override
	protected InputRef parseSingleValue(String propertyName, CharSequence propertyValue,
			String singlePropertyValue) throws ConfigurationException {
		String[] parts = singlePropertyValue.trim().split("\\s+");
		if (parts.length == 1) {
			return new InputRef(ChannelRefFormat.INSTANCE.getValueNonEmpty(propertyName, parts[0]));
		}
		if (parts.length == 3 && AS.equals(parts[1])) {
			return new InputRef(ChannelRefFormat.INSTANCE.getValueNonEmpty(propertyName, parts[0]), parts[2]);
		}
		throw new ConfigurationException(I18NConstants.ERROR_INVALID_INPUT__VALUE.fill(singlePropertyValue),
			propertyName, propertyValue);
	}

	@Override
	protected String formatSingleValue(InputRef value) {
		return value.toString();
	}
}
