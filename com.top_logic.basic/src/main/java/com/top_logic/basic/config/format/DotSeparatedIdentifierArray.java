/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.util.regex.Pattern;

import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} parsing a {@link String} array as dot-separated identifiers
 * like <code>foo.bar.bazz</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DotSeparatedIdentifierArray extends AbstractStringArrayFormat {

	/**
	 * Singleton {@link DotSeparatedIdentifierArray} instance.
	 */
	public static final DotSeparatedIdentifierArray INSTANCE = new DotSeparatedIdentifierArray();

	private DotSeparatedIdentifierArray() {
		// Singleton constructor.
	}

	private static final Pattern PATTERN = Pattern.compile("\\s*\\.\\s*");

	@Override
	protected Pattern splittPattern() {
		return PATTERN;
	}

	@Override
	protected String separator() {
		return ".";
	}

}