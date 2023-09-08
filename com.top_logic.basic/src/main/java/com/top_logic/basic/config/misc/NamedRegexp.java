/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.misc;

import java.util.regex.Pattern;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.RegExpValueProvider;

/**
 * Configuration of a named regular expression.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface NamedRegexp extends NamedConfigMandatory {

	/** Configuration property for the value of {@link #getPattern()}. */
	String PATTERN = "pattern";

	/**
	 * The configured regular expression.
	 */
	@Format(RegExpValueProvider.class)
	@Mandatory
	@Name(PATTERN)
	Pattern getPattern();

	/**
	 * Setter for {@link #getPattern()}.
	 */
	void setPattern(Pattern regexp);

}

