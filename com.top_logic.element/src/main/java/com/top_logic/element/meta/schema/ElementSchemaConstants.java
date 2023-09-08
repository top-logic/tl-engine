/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.schema;

import com.top_logic.element.config.InterfaceConfig;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.model.TLClass;

/**
 * Constants for parsing {@link TLClass} definition files.
 * 
 * @see ModelConfig
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ElementSchemaConstants {

	public static final String MODEL_1_NS = "http://www.top-logic.com/ns/dynamic-types/1.0";

	public static final String MODEL_6_NS = "http://www.top-logic.com/ns/dynamic-types/6.0";

	/**
	 * Top-level container for model definitions.
	 */
	public static final String ROOT_ELEMENT = "model";

	public static final String LISTS_ELEMENT = "lists";
	public static final String LIST_ELEMENT = "list";
	
	/**
	 * Container element for {@link #META_ELEMENT_PROPERTY_ELEMENT}s within a
	 * {@link InterfaceConfig#TAG_NAME}.
	 */
	public static final String META_ELEMENT_PROPERTIES_ELEMENT = "attributes";

	/**
	 * Element specifying a static property of a {@link TLClass} through a
	 * key/value pair.
	 * 
	 * @see #META_ELEMENT_PROPERTY_NAME_ATTR
	 * @see #META_ELEMENT_PROPERTY_VALUE_ATTR
	 */
	public static final String META_ELEMENT_PROPERTY_ELEMENT = "meattribute";

	/**
	 * Attribute of {@link #META_ELEMENT_PROPERTY_ELEMENT} specifying the name
	 * of a {@link TLClass} property.
	 */
	public static final String META_ELEMENT_PROPERTY_NAME_ATTR = "name";
	
	/**
	 * Attribute of {@link #META_ELEMENT_PROPERTY_ELEMENT} specifying the value
	 * of a {@link TLClass} property.
	 */
	public static final String META_ELEMENT_PROPERTY_VALUE_ATTR = "value";

	/** The name of a property tag */
	public static final String PROPERTY_ELEMENT = "property";

	/** The name of a reference tag */
	public static final String REFERENCE_ELEMENT = "reference";

	/** The name of a association end tag */
	public static final String ASSOCIATION_END_ELEMENT = "end";

	/**
	 * Attribute of {@link ModelConfig#MODULE} specifying the structure name.
	 */
	public static final String NAME_STRUCTURE_ATTR = "name";

	public static final String LIST_NAME_ATTR = "name";
}
