/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.file;

import java.util.List;
import java.util.regex.Pattern;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.format.RegExpValueProvider;

/**
 * The configuration of a single rule of the {@link FileNameConvention}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface FileNameRule extends NamedConfigMandatory {

	/** Property name of {@link #getRegex()}. */
	String REGEX = "regex";

	/** Property name of {@link #getExceptions()}. */
	String EXCEPTIONS = "exceptions";

	/**
	 * The regex {@link Pattern} that the file names have to match.
	 * <p>
	 * The file names are relative to the eclipse project. The path elements are guaranteed to
	 * be separated with a "/", independent of the operation system.
	 * </p>
	 */
	@Format(RegExpValueProvider.class)
	Pattern getRegex();

	/**
	 * The regex {@link Pattern} defining the exceptions.
	 * <p>
	 * Files matched by this regex are excluded from the rule.
	 * </p>
	 */
	@ListBinding(format = RegExpValueProvider.class)
	List<Pattern> getExceptions();

}
