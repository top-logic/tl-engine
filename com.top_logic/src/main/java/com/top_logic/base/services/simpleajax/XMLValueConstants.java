/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.util.Map;

import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Constants defining the value marshaling protocol between client-side
 * JavaScript controller objects and server-side {@link CommandHandler} handlers.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface XMLValueConstants {

	/**
	 * Namespace for encoding values that can be exchange between Java and
	 * JavaScript.
	 * 
	 * We do not use Soap-encoding as the target is javascript which doese
	 * not provid the same datatypes as Java.
	 */
	public static final String XML_VALUE_NS = "http://top-logic.com/ns/xml-value";

	/**
	 * Encodes an {@link Integer} value.
	 */
	public static final String INT_ELEMENT = "int";
	
	/**
	 * Encodes a {@link String} value.
	 */
	public static final String STRING_ELEMENT = "string";
	
	/**
	 * Encodes a set of name value pairs (a {@link Map} of {@link String}s to
	 * values (as defined by this interface)), which is the best match for the
	 * JavaScript object model.
	 */
	public static final String OBJECT_ELEMENT = "object";
	
	/**
	 * Encodes the name of a {@link #PROPERTY_ELEMENT} of an
	 * {@link #OBJECT_ELEMENT}.
	 */
	public static final String NAME_ELEMENT = "name";
	
	/**
	 * Encodes a single property of an {@link #OBJECT_ELEMENT}.
	 */
	public static final String PROPERTY_ELEMENT = "property";
	
	/**
	 * Encodes an array of arbitrary values (as defined by this interface).
	 */
	public static final String ARRAY_ELEMENT = "array";
	
	/**
	 * Encodes an additional reference to an already marshaled
	 * {@link #OBJECT_ELEMENT}. This allows to exchange arbitrary object graphs
	 * between Java and JavaScript.
	 */
	public static final String REF_ELEMENT = "ref";
	
	/**
	 * Encodes the <code>null</code> value.
	 */
	public static final String NULL_ELEMENT = "null";
	
	/**
	 * Encodes the identifier that is assigned to each {@link #OBJECT_ELEMENT}
	 * to be able to refer to it later on through {@link #REF_ELEMENT}s.
	 */
	public static final String ID_ATTRIBUTE = "id";

	/**
	 * Encodes a {@link Float} value.
	 */
	public static final String FLOAT_ELEMENT = "float";
	
	/**
	 * Encodes a {@link Boolean} value.
	 */
	public static final String BOOLEAN_ELEMENT = "boolean";

}
