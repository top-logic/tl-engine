/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.convert.converters.FormatConverter;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;

/**
 * The text of an attribute value, for the index.
 *
 * <p>
 * A string is its text. An internationalized text counts with each of its languages. A binary - an
 * uploaded file - is converted to text by the engine's format converters (PDF, Office, HTML, plain
 * text). A collection is the text of its elements, a blank line between two. A referenced object
 * contributes nothing: its text belongs to its own index entry, and a change of it would not
 * re-index this object.
 * </p>
 */
public final class TextValues {

	/** Between two parts of one text. */
	private static final String SEPARATOR = "\n\n";

	private TextValues() {
		// Static utility.
	}

	/**
	 * The text of a value, at most {@code maxChars} characters long; empty for a value without
	 * text.
	 */
	public static String text(Object value, int maxChars) {
		StringBuilder out = new StringBuilder();
		append(out, value, maxChars);
		return out.length() > maxChars ? out.substring(0, maxChars) : out.toString();
	}

	private static void append(StringBuilder out, Object value, int maxChars) {
		if (value == null || out.length() >= maxChars) {
			return;
		}
		if (value instanceof CharSequence text) {
			add(out, text.toString());
		} else if (value instanceof ResKey key) {
			for (String text : languages(key)) {
				add(out, text);
			}
		} else if (value instanceof BinaryDataSource binary) {
			add(out, convert(binary, maxChars - out.length()));
		} else if (value instanceof Collection<?> many) {
			for (Object element : many) {
				append(out, element, maxChars);
			}
		} else if (value instanceof TLObject) {
			// Another object's text is indexed with that object.
		} else if (value instanceof Number || value instanceof Boolean) {
			// A number is no text to search by meaning; a keyword filters by it.
		} else {
			add(out, String.valueOf(value));
		}
	}

	private static void add(StringBuilder out, String text) {
		if (text == null || text.isBlank()) {
			return;
		}
		if (out.length() > 0) {
			out.append(SEPARATOR);
		}
		out.append(text);
	}

	private static Set<String> languages(ResKey key) {
		Set<String> result = new LinkedHashSet<>();
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			String text = Resources.getInstance(locale).getString(key, null);
			if (text != null) {
				result.add(text);
			}
		}
		return result;
	}

	/**
	 * The text of a file, by the engine's converter for its content type.
	 */
	private static String convert(BinaryDataSource binary, int maxChars) {
		if (!FormatConverterFactory.Module.INSTANCE.isActive()) {
			return "";
		}
		String contentType = binary.getContentType();
		FormatConverter converter = FormatConverterFactory.getInstance().getFormatConverter(contentType);
		if (converter == null) {
			return "";
		}
		try (InputStream in = binary.toData().getStream(); Reader reader = converter.convert(in, contentType)) {
			StringBuilder text = new StringBuilder();
			char[] buffer = new char[8192];
			for (int read = reader.read(buffer); read > 0 && text.length() < maxChars; read = reader.read(buffer)) {
				text.append(buffer, 0, read);
			}
			return text.toString();
		} catch (IOException | RuntimeException ex) {
			Logger.warn("Cannot convert " + binary.getName() + " (" + contentType + ") to text: " + ex.getMessage(),
				TextValues.class);
			return "";
		}
	}

}
