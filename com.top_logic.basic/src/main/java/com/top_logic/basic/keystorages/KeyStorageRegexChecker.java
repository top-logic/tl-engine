/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.keystorages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.RegExpValueProvider;

/**
 * Checks that no entry matches the configured {@link Pattern}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class KeyStorageRegexChecker implements KeyStorageChecker,
		ConfiguredInstance<KeyStorageRegexChecker.Config> {

	/** {@link PolymorphicConfiguration} of the {@link KeyStorageRegexChecker}. */
	public interface Config extends PolymorphicConfiguration<KeyStorageRegexChecker> {

		/** Name of the {@link #getRegex()} property. */
		String REGEX = "regex";

		/** Name of the {@link #shouldCheckKey()} property. */
		String CHECK_KEY = "check-key";

		/** Name of the {@link #getExplanation()} property. */
		String EXPLANATION = "explanation";

		/**
		 * Entries {@link Matcher#find() containing} this {@link Pattern} are reported as errors.
		 */
		@Name(REGEX)
		@Mandatory
		@Format(RegExpValueProvider.class)
		Pattern getRegex();

		/**
		 * Whether the key or the value should be checked.
		 * <p>
		 * "true" means, the key should be checked. "false" means, the value.
		 * </p>
		 */
		@Name(CHECK_KEY)
		@Mandatory
		boolean shouldCheckKey();

		/** Explanation for the error message, what is wrong, why and how to fix it. */
		@Name(EXPLANATION)
		@Mandatory
		String getExplanation();

	}

	private final Config _config;

	private final Pattern _regex;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link KeyStorageRegexChecker}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public KeyStorageRegexChecker(InstantiationContext context, Config config) {
		_config = config;
		_regex = config.getRegex();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Exception check(String key, String value) {
		if (getConfig().shouldCheckKey()) {
			return check(key);
		} else {
			return check(value);
		}
	}

	private Exception check(String entry) {
		String matchedText = match(entry);
		if (matchedText == null) {
			return null;
		}
		return createError(matchedText);
	}

	private Exception createError(String text) {
		String entry = getConfig().shouldCheckKey() ? "key" : "value";
		String regex = getConfig().getRegex().pattern();
		String message = "The " + entry + " is wrong: " + getConfig().getExplanation() + " Matched text: '" + text
			+ "'. Regex: '" + regex + "'";
		return new ConfigurationException(message);
	}

	private String match(String text) {
		Matcher matcher = createMatcher(text);
		boolean success = matcher.find();
		if (!success) {
			return null;
		}
		return matcher.group();
	}

	private Matcher createMatcher(String text) {
		return _regex.matcher(text);
	}

}
