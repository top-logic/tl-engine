/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates a URI template pattern with parameter extraction capabilities.
 *
 * <p>
 * This class parses URI templates (e.g., {@code "myapp://data/{itemId}/{version}"}) into
 * a regex pattern that can match URIs and extract parameter values. Parameter names are
 * preserved in order for mapping extracted values.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * UriPattern pattern = UriPattern.compile("myapp://data/{itemId}/{version}");
 * Map&lt;String, String&gt; params = pattern.extractParameters("myapp://data/12345/v2");
 * // params contains: {"itemId": "12345", "version": "v2"}
 * </pre>
 *
 * @author Bernhard Haumacher
 */
public class UriPattern {

	private final Pattern _pattern;

	private final List<String> _parameterNames;

	/**
	 * Creates a {@link UriPattern}.
	 *
	 * @param pattern
	 *        The compiled regex pattern.
	 * @param parameterNames
	 *        The ordered list of parameter names.
	 */
	private UriPattern(Pattern pattern, List<String> parameterNames) {
		_pattern = pattern;
		_parameterNames = parameterNames;
	}

	/**
	 * Compiles a URI template into a {@link UriPattern}.
	 *
	 * <p>
	 * Template variables in the format {@code {variableName}} are replaced with regex
	 * capture groups that match one or more non-slash characters.
	 * </p>
	 *
	 * @param template
	 *        The URI template string (e.g., "myapp://data/{itemId}").
	 * @return A compiled {@code UriPattern} that can match and extract parameters from URIs.
	 * @throws IllegalArgumentException
	 *         If the template contains unclosed variable placeholders.
	 */
	public static UriPattern compile(String template) {
		List<String> parameterNames = new ArrayList<>();
		StringBuilder patternBuilder = new StringBuilder();
		int pos = 0;

		while (pos < template.length()) {
			int varStart = template.indexOf('{', pos);

			if (varStart == -1) {
				// No more variables, quote the remaining literal part
				patternBuilder.append(Pattern.quote(template.substring(pos)));
				break;
			}

			// Quote the literal part before the variable
			if (varStart > pos) {
				patternBuilder.append(Pattern.quote(template.substring(pos, varStart)));
			}

			// Find the end of the variable
			int varEnd = template.indexOf('}', varStart);
			if (varEnd == -1) {
				throw new IllegalArgumentException("Unclosed variable in template: " + template);
			}

			// Extract variable name
			String varName = template.substring(varStart + 1, varEnd);
			parameterNames.add(varName);

			// Add capture group for the variable (matches non-slash characters)
			patternBuilder.append("([^/]+)");

			// Move past the variable
			pos = varEnd + 1;
		}

		Pattern pattern = Pattern.compile(patternBuilder.toString());
		return new UriPattern(pattern, parameterNames);
	}

	/**
	 * Extracts parameter values from a URI that matches this pattern.
	 *
	 * @param uri
	 *        The full URI (e.g., "myapp://data/12345").
	 * @return A map of parameter names to their extracted values.
	 * @throws IllegalArgumentException
	 *         If the URI does not match the pattern.
	 */
	public Map<String, String> extractParameters(String uri) {
		Matcher matcher = _pattern.matcher(uri);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("URI does not match pattern: " + uri);
		}

		Map<String, String> parameters = new HashMap<>();
		for (int i = 0; i < _parameterNames.size(); i++) {
			String paramName = _parameterNames.get(i);
			String paramValue = matcher.group(i + 1);
			parameters.put(paramName, paramValue);
		}

		return parameters;
	}

	/**
	 * Returns the ordered list of parameter names in this URI pattern.
	 *
	 * @return An unmodifiable view of the parameter names.
	 */
	public List<String> getParameterNames() {
		return _parameterNames;
	}

	/**
	 * Returns the underlying regex pattern.
	 *
	 * @return The compiled pattern.
	 */
	public Pattern getPattern() {
		return _pattern;
	}
}
