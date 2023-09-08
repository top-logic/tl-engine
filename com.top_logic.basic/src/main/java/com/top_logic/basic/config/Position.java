/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Describes the position to which a list entry is moved during an {@link ListOperation#UPDATE
 * update} or where a new list entry is {@link ListOperation#ADD inserted}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum Position implements ExternallyNamed {

	/**
	 * Indicates that the entry has to be moved to the begin of the list.
	 */
	BEGIN("begin"),

	/**
	 * Indicates that the entry has to be moved to the end of the list.
	 */
	END("end"),

	/**
	 * Indicates that the entry has to be moved directly before that element in
	 * the list whose serialized key property is the attribute value of the
	 * attribute {@link ConfigurationSchemaConstants#LIST_REFERENCE_ATTR_NAME}
	 * 
	 * @see ConfigurationSchemaConstants#LIST_REFERENCE_ATTR_NAME
	 */
	BEFORE("before"),

	/**
	 * Indicates that the entry has to be moved directly after that element in
	 * the list whose serialized key property is the attribute value of the
	 * attribute {@link ConfigurationSchemaConstants#LIST_REFERENCE_ATTR_NAME}
	 * 
	 * @see ConfigurationSchemaConstants#LIST_REFERENCE_ATTR_NAME
	 */
	AFTER("after"),

	;

	private final String _externalName;

	private Position(String externalName) {
		this._externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
