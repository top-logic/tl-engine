/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * A {@link ListOperation} describes the kind of modification of a list valued
 * property.
 * 
 * @see MapOperation
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum ListOperation implements ExternallyNamed {

	/**
	 * Used to indicate that the new entry must be added to the old list value,
	 * if there is no corresponding entry for update. In that case the old entry
	 * is updated with the new entry.
	 * 
	 * The position where the entry is added is given by a {@link Position}. If
	 * the operation is is de facto an update the position is used to move the
	 * entry to.
	 * 
	 * @see #ADD
	 * @see #UPDATE
	 */
	ADD_OR_UPDATE("addOrUpdate"),
	
	/**
	 * Used to indicate that the new entry must be added to the old list value.
	 * The position where the entry is added is given by a {@link Position}.
	 */
	ADD("add"),

	/**
	 * Used to indicate that the entry which has the same key property must be
	 * removed
	 */
	REMOVE("remove"),

	/**
	 * Used to indicate that the entry which has the same key property must be
	 * updated by the new list value. If also a {@link Position} is given, the
	 * entry is also moved.
	 */
	UPDATE("update"),
	;

	private final String _externalName;

	private ListOperation(String externalName) {
		this._externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
