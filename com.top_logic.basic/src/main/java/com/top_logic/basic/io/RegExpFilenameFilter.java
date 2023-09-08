/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Accepts only {@link File}s matching the given {@link Pattern Regular Expression}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class RegExpFilenameFilter implements FilenameFilter {

	private final Pattern _regExp;

	/**
	 * Convenience shortcut for {@link #RegExpFilenameFilter(Pattern)}.
	 */
	public RegExpFilenameFilter(String regExp) {
		this(Pattern.compile(regExp));
	}

	/**
	 * Returns a new {@link RegExpFilenameFilter} accepting only {@link File} matching the given
	 * {@link Pattern Regular Expression}.
	 */
	public RegExpFilenameFilter(Pattern regExp) {
		_regExp = regExp;
	}

	@Override
	public boolean accept(File dir, String name) {
		return _regExp.matcher(name).matches();
	}

}
