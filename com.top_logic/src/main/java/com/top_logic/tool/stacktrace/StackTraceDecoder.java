/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.stacktrace;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.tool.stacktrace.internal.LineNumberEncoding;

/**
 * Tool decoding line numbers in stack traces using the system-provided line number mapping files.
 * 
 * @see #main(String[])
 * @see #decode(String)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StackTraceDecoder {

	private static final String NAME = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

	private static final String DOT = "\\.";

	private static final String DOTTED_NAME = NAME + "(?:" + DOT + NAME + ")*";

	private static final String WS = "\\s+";

	private static final String WS_OPT = "(?:" + WS + ")?";

	private static final String NUM = "\\d+";

	private static final String FRAME =
		"at" + WS + opt(group(DOTTED_NAME) + DOT) + group(NAME) + DOT + group(NAME) + WS_OPT + "\\(" + WS_OPT
			+ group(NAME) + DOT + group(NAME) + WS_OPT + ":" + WS_OPT + group(NUM) + WS_OPT + "\\)";

	private static final Pattern FRAME_PATTERN = Pattern.compile(FRAME);

	private Properties _properties;

	/**
	 * Creates a {@link StackTraceDecoder}.
	 *
	 */
	public StackTraceDecoder() {
		_properties = loadSourceMappings();
	}

	/**
	 * Decodes line numbers in the given printed stack trace.
	 *
	 * @param stackTrace
	 *        The stack trace to convert.
	 * @return The stack trace with decoded line numbers.
	 */
	public String decode(String stackTrace) {
		StringBuilder result = new StringBuilder();
		Matcher matcher = FRAME_PATTERN.matcher(stackTrace);
		int pos = 0;
		while (matcher.find()) {
			int start = matcher.start();
			if (start > pos) {
				result.append(stackTrace.substring(pos, start));
			}
			String pkgName = matcher.group(1);
			String className = matcher.group(2);
			String methodName = matcher.group(3);
			String fileName = matcher.group(4);
			String fileExt = matcher.group(5);
			int lineNo = decodeLine(pkgName, fileName, Integer.parseInt(matcher.group(6)));
	
			result.append(
				"at " + (pkgName != null ? pkgName + "." : "") + className + "." + methodName + "(" + fileName + "."
					+ fileExt + ":" + lineNo + ")");
			pos = matcher.end();
		}
		if (pos < stackTrace.length()) {
			result.append(stackTrace.substring(pos));
		}
	
		return result.toString();
	}

	private int decodeLine(String pkgName, String fileName, int lineNo) {
		String key;
		if (pkgName != null) {
			key = pkgName.replace('.', '/') + '/' + fileName;
		} else {
			key = fileName;
		}

		String value = _properties.getProperty(key);
		if (value != null) {
			LineNumberEncoding encoding = LineNumberEncoding.fromString(value);
			encoding.initDecoder();

			lineNo = encoding.decode(lineNo);
		}
		return lineNo;
	}

	private static Properties loadSourceMappings() {
		try {
			return tryLoadSourceMappings();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private static Properties tryLoadSourceMappings() throws IOException {
		Enumeration<URL> resources =
			StackTraceDecoder.class.getClassLoader().getResources("META-INF/source-mapping.properties");
		Properties properties = new Properties();
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			try (InputStream in = url.openStream()) {
				properties.load(in);
			}
		}
		return properties;
	}

	private static String opt(String expr) {
		return "(?:" + expr + ")";
	}

	private static String group(String regexpr) {
		return "(" + regexpr + ")";
	}

	/**
	 * {@link StackTraceDecoder} tool for converting a stack trace on the command line.
	 * 
	 * <p>
	 * The stack trace is passed to the standard input followed by a blank line. The converted stack
	 * trace is printed to the standard output.
	 * </p>
	 */
	public static void main(String[] args) throws IOException {
		StringBuilder input = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
			while (true) {
				String line = in.readLine();
				if (line == null || line.trim().isEmpty()) {
					break;
				}
				input.append(line);
				input.append('\n');
			}
		}
	
		String stackTrace = input.toString();
	
		System.out.println();
		System.out.println("---- Decoded ----");
		System.out.println(new StackTraceDecoder().decode(stackTrace));
	}

}
