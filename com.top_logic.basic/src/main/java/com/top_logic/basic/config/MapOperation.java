/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Map;

/**
 * A {@link MapOperation} describes the kind of modification of a map valued
 * property.
 * 
 * @see ListOperation
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum MapOperation implements ExternallyNamed {

	/**
	 * Used to indicate that the new entry must be added to the {@link Map}. If
	 * there is already an entry with the same key the entry is updated.
	 * 
	 * @see MapOperation#ADD
	 * @see MapOperation#UPDATE
	 */
	ADD_OR_UPDATE("addOrUpdate"),

	/**
	 * Used to indicate that the new entry must be added to the {@link Map}
	 */
	ADD("add"),
	
	/**
	 * Used to indicate that the entry which has the same key property must be
	 * removed
	 */
	REMOVE("remove"),

	/**
	 * Used to indicate that the entry which has the same key property must be
	 * updated
	 */
	UPDATE("update");

	private final String _externalName;

	MapOperation(String externalName) {
		this._externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
