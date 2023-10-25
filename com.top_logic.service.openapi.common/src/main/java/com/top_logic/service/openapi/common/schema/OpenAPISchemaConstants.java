/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.schema;

/**
 * Constants in an <i>OpenAPI</i> schema.
 */
public class OpenAPISchemaConstants {

	/**
	 * Property in a schema that defines the type of the value.
	 */
	public static final String SCHEMA_PROPERTY_TYPE = "type";

	/**
	 * Property in a schema that defines the format of a type.
	 */
	public static final String SCHEMA_PROPERTY_FORMAT = "format";

	/**
	 * Property in a schema that defines the items when the {@link #SCHEMA_PROPERTY_TYPE type} is an
	 * {@link #SCHEMA_TYPE_ARRAY array}.
	 */
	public static final String SCHEMA_PROPERTY_ITEMS = "items";

	/**
	 * Property in a schema that holds a description for the schema.
	 */
	public static final String SCHEMA_PROPERTY_DESCRIPTION = "description";

	/**
	 * Property in a schema that holds a default value for the schema.
	 */
	public static final Object SCHEMA_PROPERTY_DEFAULT = "default";

	/**
	 * Property in a schema that holds an example value for the schema.
	 */
	public static final Object SCHEMA_PROPERTY_EXAMPLE = "example";

	/**
	 * Property in a schema that defines the properties when the {@link #SCHEMA_PROPERTY_TYPE type}
	 * is an {@link #SCHEMA_TYPE_OBJECT object}.
	 */
	public static final String SCHEMA_PROPERTY_PROPERTIES = "properties";

	/**
	 * Property in a schema that defines the names of the required properties when the
	 * {@link #SCHEMA_PROPERTY_TYPE type} is an {@link #SCHEMA_TYPE_OBJECT object}.
	 */
	public static final String SCHEMA_PROPERTY_REQUIRED = "required";

	/**
	 * {@value #SCHEMA_PROPERTY_FORMAT Format} of a {@link #SCHEMA_TYPE_STRING string} attribute
	 * representing a date with time.
	 */
	public static final String SCHEMA_FORMAT_DATE_TIME = "date-time";

	/**
	 * {@value #SCHEMA_PROPERTY_FORMAT Format} of a {@link #SCHEMA_TYPE_STRING string} attribute
	 * representing a date.
	 */
	public static final String SCHEMA_FORMAT_DATE = "date";

	/**
	 * {@value #SCHEMA_PROPERTY_FORMAT Format} of a {@link #SCHEMA_TYPE_INTEGER integer} attribute
	 * representing a precision of 64 bit.
	 * 
	 * @see Long
	 */
	public static final String SCHEMA_FORMAT_INT64 = "int64";

	/**
	 * {@value #SCHEMA_PROPERTY_FORMAT Format} of a {@link #SCHEMA_TYPE_INTEGER integer} attribute
	 * representing a precision of 32 bit.
	 * 
	 * @see Integer
	 */
	public static final String SCHEMA_FORMAT_INT32 = "int32";

	/**
	 * {@value #SCHEMA_PROPERTY_FORMAT Format} of a {@link #SCHEMA_TYPE_NUMBER number} attribute
	 * representing a double.
	 * 
	 * @see Double
	 */
	public static final String SCHEMA_FORMAT_DOUBLE = "double";

	/**
	 * {@value #SCHEMA_PROPERTY_FORMAT Format} of a {@link #SCHEMA_TYPE_NUMBER number} attribute
	 * representing a float.
	 * 
	 * @see Float
	 */
	public static final String SCHEMA_FORMAT_FLOAT = "float";

	/**
	 * {@value #SCHEMA_PROPERTY_TYPE Type} representing an array.
	 */
	public static final String SCHEMA_TYPE_ARRAY = "array";

	/**
	 * {@value #SCHEMA_PROPERTY_TYPE Type} representing a number.
	 * 
	 * @see #SCHEMA_FORMAT_FLOAT
	 * @see #SCHEMA_FORMAT_DOUBLE
	 */
	public static final String SCHEMA_TYPE_NUMBER = "number";

	/**
	 * {@value #SCHEMA_PROPERTY_TYPE Type} representing an integer.
	 * 
	 * @see #SCHEMA_FORMAT_INT32
	 * @see #SCHEMA_FORMAT_INT64
	 */
	public static final String SCHEMA_TYPE_INTEGER = "integer";

	/**
	 * {@value #SCHEMA_PROPERTY_TYPE Type} representing a string.
	 * 
	 * @see #SCHEMA_FORMAT_DATE
	 * @see #SCHEMA_FORMAT_DATE_TIME
	 */
	public static final String SCHEMA_TYPE_STRING = "string";

	/**
	 * {@value #SCHEMA_PROPERTY_TYPE Type} representing a boolean.
	 */
	public static final String SCHEMA_TYPE_BOOLEAN = "boolean";

	/**
	 * {@link #SCHEMA_PROPERTY_TYPE Type} representing an object.
	 */
	public static final String SCHEMA_TYPE_OBJECT = "object";

}
