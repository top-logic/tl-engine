/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.layout.basic.ThemeImage;

/**
 * A bundle of icons and a mapping of IconDescription#get
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class IconBundle {

	private List<IconDescription> _icons = new ArrayList<>();

	private Map<String, String> _mapping;

	/**
	 * Create a new IconBundle.
	 * 
	 * @param mapping
	 *        The mapping of style and infix.
	 */
	public IconBundle(Map<String, String> mapping) {
		_mapping = mapping;
	}

	/**
	 * Reads one json entry after the other and creates a new {@link IconBundle} containing a list
	 * of IconDescription.
	 * 
	 * @param reader
	 *        The reader for the json.
	 * @param mapping
	 *        The mapping of style and infix.
	 * @return The created {@link IconBundle}
	 * @throws IOException
	 *         If the reader cannot read the input stream.
	 */
	public static IconBundle read(JsonReader reader, Map<String, String> mapping) throws IOException {
		IconBundle result = new IconBundle(mapping);

		reader.beginObject();

		while (reader.hasNext()) {
			result.getIcons().add(IconDescription.read(reader, mapping));
		}

		reader.endObject();

		return result;
	}

	/**
	 * Gets the mapping of style-attributes of a json entry and the infixes for the
	 * {@link ThemeImage}s.
	 * 
	 * @return The mapping of style and infix.
	 */
	public Map<String, String> getMapping() {
		return _mapping;
	}

	/**
	 * Gets the list of {@link IconDescription}s.
	 * 
	 * @return The list of {@link IconDescription}.
	 */
	public List<IconDescription> getIcons() {
		return _icons;
	}
}