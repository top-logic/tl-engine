/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.cleanup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase.ResourcePrefix;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.tools.FileHandler;
import com.top_logic.layout.tools.FileUtil;

/**
 * Tool for migrating I18NConstants-defined resource keys, see Ticket #18247.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MigrateI18NConstants implements FileHandler {

	private static final String OMITTED_COMMON_PREFIX = "com.top_logic.";

	private static final char NAME_PART_SEPARATOR = '_';

	private static final char PARAMETER_START_CHAR = '(';

	private static final char PARAMETER_SEPARATOR_CHAR = ',';

	private static final char PARAMETER_STOP_CHAR = ')';

	Map<String, Set<String>> _keyMapping = new HashMap<>();

	private boolean _useActualValue = true;

	/**
	 * Generates a resource key mapping for all keys defined in the given I18NConstants files
	 * (*.java).
	 * 
	 * @param args
	 *        Paths with I18NConstants Java source files. The corresponding classes must be
	 *        resolvable from the current class path.
	 */
	public static void main(String[] args) throws Exception {
		MigrateI18NConstants tool = new MigrateI18NConstants();
		FileUtil.handleFiles(args, tool);
		tool.storeMapping();
	}

	private void storeMapping() throws FileNotFoundException, IOException {
		ResourceMappingUtil.storeMappings(_keyMapping);
	}

	@Override
	public void handleFile(String fileName) throws Exception {
		File file = new File(fileName);
		String localName = file.getName();
		if (!localName.endsWith(".java")) {
			System.err.println("WARNING: No Java source file: " + fileName);
			return;
		}
		String src = FileUtilities.readFileToString(file);
		String ws = "\\s*";
		String separator = "\\s+";
		Pattern packagePattern =
			Pattern
				.compile("^" + ws + "package" + separator + "([a-zA-Z0-9_\\.]+)" + ws + ";" + ws + "$",
					Pattern.MULTILINE);
		Matcher matcher = packagePattern.matcher(src);
		if (!matcher.find()) {
			System.err.println("WARNING: No package declaration found in: " + fileName);
			return;
		}
		String pkg = matcher.group(1);
		String className = pkg + "." + localName.substring(0, localName.length() - ".java".length());

		Class<?> i18nClass = Class.forName(className);

		String prefix = i18nPrefix(i18nClass);

		for (Field field : i18nClass.getFields()) {
			int constantModifiers = field.getModifiers();
			if (!Modifier.isStatic(constantModifiers) // ignore all none (public static String)
				|| !Modifier.isPublic(constantModifiers)
				|| !resourceType(field))
				continue;

			boolean resourcePrefix = isPrefix(field);
			String constantFieldName = field.getName();
			String keyName = convertToKey(constantFieldName);
			StringBuilder legacyKeyBuilder = new StringBuilder();
			legacyKeyBuilder.append(prefix);
			legacyKeyBuilder.append('.');
			legacyKeyBuilder.append(keyName);
			if (resourcePrefix) {
				legacyKeyBuilder.append('.');
			}
			String legacyKey = legacyKeyBuilder.toString();

			String newKey;
			if (_useActualValue) {
				newKey = getValue(field);
			} else {
				StringBuilder newKeyBuilder = new StringBuilder();
				newKeyBuilder.append("class.");
				newKeyBuilder.append(i18nClass.getName());
				newKeyBuilder.append('.');
				newKeyBuilder.append(constantFieldName);
				if (resourcePrefix) {
					newKeyBuilder.append('.');
				}
				
				newKey = newKeyBuilder.toString();
			}

			MultiMaps.add(_keyMapping, legacyKey, newKey);
		}
	}

	private String getValue(Field field) {
		Object value;
		try {
			value = field.get(null);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		if (value instanceof ResKey) {
			return ((ResKey) value).getKey();
		}
		if (value instanceof ResPrefix) {
			return ((ResPrefix) value).toPrefix();
		}
		return (String) value;
	}

	private boolean isPrefix(Field field) {
		Class<?> type = field.getType();
		return type == ResPrefix.class || field.getAnnotation(ResourcePrefix.class) != null;
	}

	private boolean resourceType(Field field) {
		Class<?> type = field.getType();
		return type == String.class || type == ResKey.class || type == ResPrefix.class;
	}

	private static String i18nPrefix(Class<?> i18nClass) {
		return getLegacyResPrefix(i18nClass.getName());
	}

	private static String getLegacyResPrefix(String className) {
		int indexOfLastDot = className.lastIndexOf('.');

		assert indexOfLastDot > 0 : "not a top-level class";

		String prefix = className.substring(
			className.startsWith(OMITTED_COMMON_PREFIX) ?
				OMITTED_COMMON_PREFIX.length() : 0, indexOfLastDot);

		return prefix;
	}

	private static String convertToKey(String constantName) {
		StringBuffer result = new StringBuffer();

		boolean firstMessagePart = true;
		boolean firstParameterPart = true;
		boolean withinParameters = false;

		for (int indexOfUnderscore, nextNamePartStartIndex = 0; nextNamePartStartIndex < constantName.length(); nextNamePartStartIndex =
			indexOfUnderscore + 1)
		{
			indexOfUnderscore = constantName.indexOf(
				NAME_PART_SEPARATOR, nextNamePartStartIndex);
			if (indexOfUnderscore < 0) {
				// Assume a trailing underscore to avoid special handling of the
				// last name part.
				indexOfUnderscore = constantName.length();
			}

			String namePart =
				constantName.substring(nextNamePartStartIndex, indexOfUnderscore);

			if (namePart.length() == 0) {
				assert (!withinParameters) : "double underscores only once";
				withinParameters = true;
				continue;
			}

			if (withinParameters) {
				if (firstParameterPart) {
					result.append(PARAMETER_START_CHAR);
					firstParameterPart = false;
				} else {
					result.append(PARAMETER_SEPARATOR_CHAR);
				}
				result.append(namePart.toLowerCase());
			} else {
				char firstCharOfNamePart = namePart.charAt(0);
				result.append(firstMessagePart ?
					Character.toLowerCase(firstCharOfNamePart) :
					Character.toUpperCase(firstCharOfNamePart));
				result.append(namePart.substring(1).toLowerCase());
				firstMessagePart = false;
			}
		}

		if (withinParameters) {
			result.append(PARAMETER_STOP_CHAR);
		}

		return result.toString();
	}

}
