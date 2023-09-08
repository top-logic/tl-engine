/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.character;

/**
 * Takes take of storing and returning the name of the {@link CharacterContent}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractCharacterContent implements CharacterContent {

	private final String _debugName;

	/**
	 * Creates a {@link AbstractCharacterContent}.
	 * 
	 * @param debugName
	 *        Is allowed to be <code>null</code>, which will result in {@link #NO_NAME}.
	 */
	public AbstractCharacterContent(String debugName) {
		if (debugName == null) {
			_debugName = NO_NAME;
		} else {
			_debugName = debugName;
		}
	}

	@Override
	public String toString() {
		return _debugName;
	}

}
