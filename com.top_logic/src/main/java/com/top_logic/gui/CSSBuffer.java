/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Simplistic variable replacer for stylesheet contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CSSBuffer {

	/**
	 * Format for defining a CSS variable.
	 * 
	 * <p>
	 * CSS variables are prefixed with a double dash.
	 * </p>
	 * 
	 * <p>
	 * This format takes two arguments. The first argument is the name of the variable and the
	 * second its value.
	 * </p>
	 */
	public static final String VARIABLE_DEFINITION_FORMAT = "--%s: %s;";

	/**
	 * Format to evaluate a CSS variable.
	 * 
	 * <p>
	 * This format takes the plain CSS variable name as argument. The prefixed double dash is added
	 * automatically by this format.
	 * </p>
	 */
	public static final String VARIABLE_EVALUATION_FORMAT = "var(--%s)";

	private static final String SELECTOR_FOR_VARIABLE_DEFINITIONS = ":root";

	private static final Pattern CSS_VARIABLE_PATTERN =
		Pattern.compile("var" + "\\(" + "--" + "([A-Za-z_][-A-Za-z_0-9\\\\.@/]*)" + "\\)");

	private static final Pattern ABSOLUTE_URL_PATTERN = Pattern.compile("url" + "\\(" + "\\s*(/[^\\)]*)" + "\\)");

	/** The buffer we operate on */
	private StringBuilder _buffer = new StringBuilder();
	
	/**
	 * The newline sequence used.
	 */
	private static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * Creates an empty {@link CSSBuffer}.
	 */
	public CSSBuffer() {
        super();
	}

    /**
	 * Replaces all variables in the stylesheet using the variables in the given replacements.
	 * 
	 * @param stylesheetName
	 *        The name of the stylesheet being generated.
	 * @param contextPath
	 *        The application context to generate absolute links.
	 * @param theme
	 *        The context {@link Theme}.
	 */
	public void replaceVariables(String stylesheetName, String contextPath, Theme theme) {
		_buffer = appendVariables(theme, _buffer);
		_buffer = expandedUrls(contextPath, theme, _buffer);
    }

	private static StringBuilder appendVariables(Theme theme, StringBuilder buffer) {
		buffer.append(NEWLINE);
		buffer.append(SELECTOR_FOR_VARIABLE_DEFINITIONS + " {");
		buffer.append(NEWLINE);
		List<ThemeSetting> settings = theme.getSettings().getSettings().stream()
			.filter(ThemeSetting::isCssRelevant)
			.filter(s -> s.getValue() != null)
			.filter(s -> !s.getName().startsWith(ThemeImage.MIME_PREFIX))
			.sorted((s1, s2) -> s1.getLocalName().compareTo(s2.getLocalName()))
			.collect(Collectors.toList());
		for (ThemeSetting setting : settings) {
			// Do not use qualified names in CSS, since dot characters cannot be part of variable
			// names.
			buffer.append(String.format(VARIABLE_DEFINITION_FORMAT, setting.getLocalName(), setting.getCssValue()));
			buffer.append(NEWLINE);
		}
		buffer.append("}");

		return buffer;
	}

	// Expand CSS-embedded theme image references.
	private static StringBuilder expandedUrls(String contextPath, Theme theme,
			CharSequence source) {
		StringBuilder result = new StringBuilder(source.length());

		Matcher matcher = ABSOLUTE_URL_PATTERN.matcher(source);
		int index = 0;
		while (matcher.find(index)) {
			result.append(source, index, matcher.start());

			String url = matcher.group(1).trim();
			result.append("url(");
			result.append(contextPath);
			result.append(theme.getFileLink(url));
			result.append(")");
			index = matcher.end();
		}
		result.append(source, index, source.length());
		return result;
	}

	/** saves the style sheet data to a file (binary, with CR/LF) */
	public void save(String filename) {
		try {
			File targetFile = FileManager.getInstance().getIDEFile(filename);
			targetFile.getParentFile().mkdirs();
			FileUtilities.writeStringToFile(_buffer.toString(), targetFile);
		} catch (IOException ex) {
			Logger.error("saveStyleSheet('" + filename + "') failed", ex, CSSBuffer.class);
		}
	}

	/**
     * Loads the style sheet (*.css) from a file (binary, with CR/LF).
     * 
     * @param    fileName    The name of the file to be read.
     */
	public void load(String fileName) {
	   try {
			try (InputStream in = FileManager.getInstance().getStream(fileName)) {
				InputStreamReader streamReader = new InputStreamReader(in, StreamUtilities.ENCODING);
				readStyleSheet(fileName, streamReader);
			}
		} catch (IOException ex) {
			Logger.error("loadStyleSheet('" + fileName + "') failed", ex, CSSBuffer.class);
		}
	}

	/**
	 * Loads the style sheet (*.css) from the given {@link Reader}.
	 * 
	 * @param resourceName
	 *        The name of the original resource.
	 */
	public void readStyleSheet(String resourceName, Reader reader) throws IOException {
		if (_buffer.length() > 0) {
			_buffer.append(NEWLINE);
		}

		if (resourceName != null) {
			_buffer.append("/*");
			_buffer.append(NEWLINE);
			_buffer.append(" * Generated from '");
			_buffer.append(resourceName);
			_buffer.append('\'');
			_buffer.append(NEWLINE);
			_buffer.append(" */");
			_buffer.append(NEWLINE);
			_buffer.append(NEWLINE);
		}

		try (BufferedReader bufferedReader = new BufferedReader(reader)) {
			StreamUtilities.readAllLinesFromReader(bufferedReader, _buffer);
		}
	}

	@Override
	public String toString() {
		return _buffer.toString();
	}

}
