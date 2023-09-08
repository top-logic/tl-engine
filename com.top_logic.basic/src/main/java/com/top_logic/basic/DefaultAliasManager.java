/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link AliasManager} retrieving alias definitions from {@link XMLProperties}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultAliasManager extends AliasManager {

	/** Pattern matching an environment variable. */
	private static final Pattern ENVIRONMENT_VARIABLE =
		Pattern.compile(
			"\\$\\{env:" +
				"(" + "(?:" + "[^\\\\}:]|\\\\." + ")*" + ")" +
				"(?:" +
				":(" + "(?:" + "[^\\\\}]|\\\\." + ")*" + ")" +
				")?" +
				"\\}");

	@Override
	public boolean usesXMLProperties() {
		return true;
	}

	@Override
	protected Map<String, String> resolveConfiguredAliases() {
		XMLProperties xProp = XMLProperties.getInstance();
		Properties theProps = xProp.getAliases();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, String> result = (Map) replaceWithEnvironment(theProps);
		return result;
	}

	private static Properties replaceWithEnvironment(Properties aliases) {
		aliases = (Properties) aliases.clone();
		for (Entry<Object, Object> aliasEntry : aliases.entrySet()) {
			Object aliasValue = aliasEntry.getValue();
			if (aliasValue instanceof String) {
				Matcher matcher = ENVIRONMENT_VARIABLE.matcher((CharSequence) aliasValue);
				if (matcher.find()) {
					StringBuffer buffer = new StringBuffer();
					do {
						String variableName = matcher.group(1);
						String defaultValue = matcher.group(2);

						if (defaultValue != null) {
							defaultValue = defaultValue.replaceAll("\\\\(.)", "$1");
						}

						String replacement = Environment.getSystemPropertyOrEnvironmentVariable(variableName, defaultValue);
						if (replacement == null) {
							replacement = useEmptyString(variableName, aliasEntry.getKey());
						}

						matcher.appendReplacement(buffer, quoteReplacement(replacement));
					} while (matcher.find());
					matcher.appendTail(buffer);
					aliasEntry.setValue(buffer.toString());
				}
			}
		}
		return aliases;
	}

	private static String useEmptyString(String variableName, Object aliasName) {
		StringBuilder message = new StringBuilder();
		message.append("Neither system property nor environment variable '");
		message.append(variableName);
		message.append("' set. No default value specified. Using empty alias for alias '");
		message.append(String.valueOf(aliasName));
		message.append("'.");
		Logger.info(message.toString(), AliasManager.class);
		return StringServices.EMPTY_STRING;
	}

}
