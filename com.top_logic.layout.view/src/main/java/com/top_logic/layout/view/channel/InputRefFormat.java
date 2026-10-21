/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Parses a single {@link InputRef} from a configuration attribute value.
 *
 * <p>
 * The value is a channel name, optionally followed by the keyword {@value CommaSeparatedInputRefs#AS}
 * and the name of the variable: {@code input="selection as ticket"} names the value of the channel
 * {@code selection} {@code ticket}. This is the format of one entry of
 * {@link CommaSeparatedInputRefs}.
 * </p>
 *
 * @see SingleInput
 */
public class InputRefFormat extends AbstractConfigurationValueProvider<InputRef> {

	/** Singleton instance. */
	public static final InputRefFormat INSTANCE = new InputRefFormat();

	/**
	 * Creates an {@link InputRefFormat}.
	 */
	public InputRefFormat() {
		super(InputRef.class);
	}

	@Override
	protected InputRef getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		return parse(propertyName, propertyValue, propertyValue.toString());
	}

	@Override
	protected String getSpecificationNonNull(InputRef configValue) {
		return configValue.toString();
	}

	/**
	 * Parses an {@link InputRef} from its textual form.
	 *
	 * @param propertyName
	 *        The name of the property being parsed, for error reporting.
	 * @param propertyValue
	 *        The complete value of the property, for error reporting.
	 * @param text
	 *        The text of the single reference.
	 * @return The parsed reference.
	 * @throws ConfigurationException
	 *         If the text is neither a channel name nor a channel name followed by
	 *         {@value CommaSeparatedInputRefs#AS} and a name.
	 */
	public static InputRef parse(String propertyName, CharSequence propertyValue, String text)
			throws ConfigurationException {
		String[] parts = text.trim().split("\\s+");
		if (parts.length == 1) {
			return new InputRef(ChannelRefFormat.INSTANCE.getValueNonEmpty(propertyName, parts[0]));
		}
		if (parts.length == 3 && CommaSeparatedInputRefs.AS.equals(parts[1])) {
			return new InputRef(ChannelRefFormat.INSTANCE.getValueNonEmpty(propertyName, parts[0]), parts[2]);
		}
		throw new ConfigurationException(I18NConstants.ERROR_INVALID_INPUT__VALUE.fill(text),
			propertyName, propertyValue);
	}
}
