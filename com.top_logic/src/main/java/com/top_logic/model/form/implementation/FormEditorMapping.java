/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import java.util.Map;

import com.top_logic.layout.Control;
import com.top_logic.model.form.definition.FormElement;

/**
 * Stores a mapping of {@link Control} IDs and their corresponding {@link FormElement}s.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorMapping {

	private Map<String, FormElement<?>> _mapping;

	/**
	 * Store a given mapping.
	 * 
	 * @param mapping
	 *        A mapping of {@link Control} IDs and their corresponding {@link FormElement}s.
	 */
	public FormEditorMapping(Map<String, FormElement<?>> mapping) {
		_mapping = mapping;
	}

	/**
	 * Returns the {@link FormElement} represented by a control with the given id.
	 */
	public FormElement<?> getMapping(String id) {
		return _mapping.get(id);
	}

	/**
	 * Stores the mapping of the id of a control and its {@link FormElement}.
	 */
	public void putMapping(String id, FormElement<?> c) {
		_mapping.put(id, c);
	}

	/**
	 * Removes a mapping of a control with the given id and its {@link FormElement}.
	 */
	public void removeMapping(String id) {
		_mapping.remove(id);
	}
}