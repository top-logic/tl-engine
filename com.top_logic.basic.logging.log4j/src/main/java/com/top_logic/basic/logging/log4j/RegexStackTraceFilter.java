/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.logging.log4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.apache.logging.log4j.message.Message;

/**
 * A Log4j {@link Filter} plugin that matches the stacktrace against a pattern.
 * <p>
 * <em>Important:</em> To use this filter, you have to activate this plugin in the Log4j
 * configuration. In the configuration XML file, the root tag has to be updated to declar a the
 * attribute "packages". This attribute is a list of comma separated Java packages and has to
 * contain this Java package:
 * 
 * <pre>
 * &lt;Configuration packages="foo.bar.fubar,com.top_logic.basic.logging.log4j"&gt;
 * ...
 * &lt;/Configuration&gt;
 * </pre>
 * </p>
 * <p>
 * If a log message has no stacktrace, this filter does not match.
 * </p>
 * <p>
 * The Log4j {@link RegexFilter} only filters the message itself, not the stacktrace.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@Plugin(name = "RegexStackTraceFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class RegexStackTraceFilter extends AbstractFilter {

	/** The attribute for the regex that should be matched. */
	private static final String REGEX = "regex";

	/** The attribute for what should happen with the message if this filter matches. */
	private static final String ON_MATCH = "onMatch";

	/** The attribute for what should happen with the message if this filter does not match. */
	private static final String ON_MISMATCH = "onMismatch";

	/**
	 * The attribute value for {@link #ON_MATCH} and {@link #ON_MISMATCH} for: "Don't log this
	 * message."
	 */
	private static final String DENY = "DENY";

	/**
	 * The attribute value for {@link #ON_MATCH} and {@link #ON_MISMATCH} for: "Log this message."
	 */
	private static final String NEUTRAL = "NEUTRAL";

	private final Pattern _regex;

	/** Creates a {@link RegexStackTraceFilter}. */
	private RegexStackTraceFilter(Pattern regex, Result onMatch, Result onMismatch) {
		super(onMatch, onMismatch);
		_regex = regex;
	}

	/** Factory method for {@link RegexStackTraceFilter}. */
	@PluginFactory
	public static RegexStackTraceFilter createFilter(
			@PluginAttribute(value = REGEX) String regex,
			@PluginAttribute(value = ON_MATCH, defaultString = NEUTRAL) Result match,
			@PluginAttribute(value = ON_MISMATCH, defaultString = DENY) Result mismatch) {
		if (regex == null) {
			LOGGER.error("A regular expression must be provided for the RegexStackTraceFilter.");
			return null;
		}
		return new RegexStackTraceFilter(Pattern.compile(regex), match, mismatch);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object... parameters) {
		// No Stacktrace, therefore the filter cannot match.
		return onMismatch;
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Object message, Throwable throwable) {
		return filter(throwable);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
		return filter(throwable);
	}

	@Override
	public Result filter(LogEvent event) {
		return filter(event.getThrown());
	}

	private Result filter(Throwable throwable) {
		if (throwable == null) {
			return onMismatch;
		}
		CharSequence stacktrace = printThrowableToString(throwable);
		boolean matches = contains(stacktrace, _regex);
		return matches ? onMatch : onMismatch;
	}

	private static CharSequence printThrowableToString(Throwable throwable) {
		StringWriter writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.getBuffer();
	}

	private static boolean contains(CharSequence text, Pattern regExp) {
		return regExp.matcher(text).find();
	}

	@Override
	public String toString() {
		return "regex=" + _regex.toString();
	}

}
