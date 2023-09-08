/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.regex.Pattern;

import com.top_logic.basic.config.format.AbstractStringArrayFormat;

/**
 * {@link AbstractConfigurationValueProvider} that creates a {@link String} array from the
 * configuration value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommaSeparatedStringArray extends AbstractStringArrayFormat {

	/** Singleton instance of {@link CommaSeparatedStringArray}. */
	public static final CommaSeparatedStringArray INSTANCE = new CommaSeparatedStringArray();

	private static final char SEPARATOR_CHAR = ',';

	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*" + SEPARATOR_CHAR + "\\s*");

	private static final String SEPARATOR = ", ";

	private CommaSeparatedStringArray() {
		super();
	}

	@Override
	protected Pattern splittPattern() {
		return SPLIT_PATTERN;
	}

	@Override
	protected String separator() {
		return SEPARATOR;
	}

}

