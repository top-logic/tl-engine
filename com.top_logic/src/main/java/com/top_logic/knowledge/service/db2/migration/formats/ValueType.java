/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.formats;

import java.util.Date;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.service.db2.migration.DumpSchemaConstants;

/**
 * Type of a property value.
 * 
 * @see DumpSchemaConstants#PROPERTY
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ValueType {

	/**
	 * The type of the <code>null</code> value.
	 */
	NULL,

	/**
	 * Value of type {@link Boolean}.
	 */
	BOOLEAN,

	/**
	 * Value of type {@link Byte}.
	 */
	BYTE,

	/**
	 * Value of type {@link Short}.
	 */
	SHORT,

	/**
	 * Value of type {@link Character}.
	 */
	CHAR,

	/**
	 * Value of type {@link Integer}.
	 */
	INT,

	/**
	 * Value of type {@link Long}.
	 */
	LONG,

	/**
	 * Value of type {@link Float}.
	 */
	FLOAT,

	/**
	 * Value of type {@link Double}.
	 */
	DOUBLE,

	/**
	 * Value of type {@link String}.
	 */
	STRING,

	/**
	 * Value of type {@link Date}.
	 */
	DATE,

	/**
	 * Value of of an object reference in the form <code>type:branch/id</code>.
	 */
	REF,

	/**
	 * Configuration value.
	 */
	CONFIG,

	/**
	 * Configured instance value.
	 */
	INSTANCE,

	/**
	 * Enum value.
	 */
	ENUM,

	/**
	 * {@link com.top_logic.basic.TLID} value.
	 */
	TLID,

	/**
	 * A singleton reference.
	 */
	SINGLETON,

	/**
	 * A byte stream.
	 */
	BINARY,

	/**
	 * A {@link BinaryData} stream.
	 */
	BINARY_DATA,

	/**
	 * An {@link ExtID external ID}.
	 */
	EXT_ID,

	/**
	 * Value of of an external object reference in the form <code>type#branch/id</code>.
	 */
	EXT_REF,

}
