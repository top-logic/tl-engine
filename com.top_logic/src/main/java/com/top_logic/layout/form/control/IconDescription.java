/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonToken;
import com.top_logic.layout.basic.ThemeImage;

/**
 * A description for a {@link ThemeImage} of the {@link IconInputControl}.
 * 
 * @use #read(JsonReader, Map).
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class IconDescription {
	private final String _prefix;

	private String _label = "";

	private List<String> _terms = Collections.emptyList();

	private List<ThemeImage> _images = Collections.emptyList();

	/**
	 * Create a new {@link IconDescription}.
	 * 
	 * @param prefix
	 *        Prefix of {@link ThemeImage} path.
	 */
	public IconDescription(String prefix) {
		_prefix = prefix;
	}

	/**
	 * Gets the prefix of the {@link ThemeImage}.
	 * 
	 * @return The prefix.
	 */
	public String getPrefix() {
		return _prefix;
	}

	/**
	 * Gets the not-internationalized label of this icon to display.
	 * 
	 * @return The label.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * Gets the terms which describes this icon for the search.
	 * 
	 * @return A list of terms.
	 */
	public List<String> getTerms() {
		return _terms;
	}

	/**
	 * Get the variants / different styles of this icon.
	 * 
	 * @return A list of all variants of this icon.
	 */
	public List<ThemeImage> getImages() {
		return _images;
	}

	/**
	 * Sets the label for this icon.
	 * 
	 * @param label
	 *        The label.
	 */
	public void setLabel(String label) {
		_label = label;
	}

	/**
	 * Sets the list of terms for a icon.
	 * 
	 * @param terms
	 *        A list of terms.
	 */
	public void setTerms(List<String> terms) {
		_terms = terms;
	}

	/**
	 * Sets the list of all variants of this icon.
	 * 
	 * @param icons
	 *        List of all variants of this icon.
	 */
	public void setImages(List<ThemeImage> icons) {
		_images = icons;
	}

	/**
	 * Checks if the given search tag is empty, matches the prefix, matches any of the list of terms
	 * or matches the label.
	 * 
	 * @param searchTag
	 *        The input of the user.
	 * @return <code>true</code> if any of the conditions matches.
	 */
	public boolean matches(String searchTag) {
		return searchTag.isEmpty()
			|| matchesPrefix(searchTag)
			|| matchesTerms(searchTag)
			|| matchesLabel(searchTag);
	}

	private boolean matchesLabel(String searchTag) {
		return getLabel().toLowerCase().contains(searchTag);
	}

	private boolean matchesPrefix(String searchTag) {
		return getPrefix().toLowerCase().contains(searchTag);
	}

	private boolean matchesTerms(String searchTag) {
		for (String term : getTerms()) {
			if (term.contains(searchTag))
				return true;
		}
	
		return false;
	}

	/**
	 * Read an entry by a given {@link JsonReader} and create a {@link IconDescription} of it,
	 * filled with the values of the reader.
	 * 
	 * @param reader
	 *        The reader to get the values of.
	 * @param mapping
	 *        The mapping of the style of an entry and its infix.
	 * @return The created {@link IconDescription}.
	 * @throws IOException
	 *         If the reader cannot read the input stream.
	 */
	public static IconDescription read(JsonReader reader, Map<String, String> mapping) throws IOException {
		String prefix = reader.nextName();
		IconDescription icon = new IconDescription(prefix);
		
		reader.beginObject();
		
		while(reader.hasNext()) {
			switch (reader.nextName()) {
				case "label":
					readLabel(reader, icon);
					break;
				case "search":
					readSearch(reader, icon);
					break;

				case "styles":
					readStyle(reader, mapping, icon);
					break;

				default:
					reader.skipValue();
					break;
			}
		}
		
		reader.endObject();
		
		return icon;
	}

	private static void readLabel(JsonReader reader, IconDescription icon) throws IOException {
		if (reader.peek() == JsonToken.STRING) {
			icon.setLabel(reader.nextString());
		} else {
			reader.skipValue();
		}
	}

	private static void readSearch(JsonReader reader, IconDescription icon) throws IOException {
		reader.beginObject();
		
		while(reader.hasNext()) {
			switch(reader.nextName()) {
				case "terms":
					readTerms(reader, icon);
					break;
					
				default:
					reader.skipValue();
					break;
			}
		}
		
		reader.endObject();
	}

	private static void readTerms(JsonReader reader, IconDescription icon) throws IOException {
		if (reader.peek() == JsonToken.BEGIN_ARRAY) {
			List<String> result = new ArrayList<>();
			reader.beginArray();

			while (reader.hasNext()) {
				result.add(reader.nextString().toLowerCase());
			}

			reader.endArray();
			icon.setTerms(result);
		} else {
			reader.skipValue();
		}
	}

	private static void readStyle(JsonReader reader, Map<String, String> mapping, IconDescription icon)
			throws IOException {
		if (reader.peek() == JsonToken.BEGIN_ARRAY) {
			List<ThemeImage> result = new ArrayList<>();
			reader.beginArray();

			while (reader.hasNext()) {
				String style = reader.nextString();
				ThemeImage image =
					ThemeImage.icon(mapping.get(style).replace("${name}", icon.getPrefix()));
				result.add(image);
			}

			reader.endArray();

			icon.setImages(result);
		} else {
			reader.skipValue();
		}
	}

}
