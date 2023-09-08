/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.TimeZone;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Global default configuration for allocating various formats.
 * 
 * @see FormatDefinition#newFormat(FormatConfig, TimeZone, java.util.Locale)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormatConfig extends ConfigurationItem {

	/**
	 * @see #getLenientParsing()
	 */
	String LENIENT_PARSING_PROPERTY = "lenient";

	/**
	 * @see #getRoundingMode()
	 */
	String ROUNDING_MODE = "rounding-mode";

	/**
	 * Configured option whether to use lenient parsing for date and time formats by default.
	 * 
	 * @see DateFormat#setLenient(boolean)
	 */
	@Name(LENIENT_PARSING_PROPERTY)
	boolean getLenientParsing();

	/**
	 * @see #getLenientParsing()
	 */
	void setLenientParsing(boolean value);

	/**
	 * The {@link RoundingMode} to use.
	 * 
	 * <p>
	 * The default value is {@link RoundingMode#HALF_EVEN} (the Java built-in value).
	 * </p>
	 */
	@Name(ROUNDING_MODE)
	RoundingMode getRoundingMode();

	/**
	 * @see #getRoundingMode()
	 */
	void setRoundingMode(RoundingMode value);

}
