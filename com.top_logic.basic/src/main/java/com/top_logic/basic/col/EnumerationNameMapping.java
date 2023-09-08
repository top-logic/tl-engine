/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Mapping of {@link Enum} values to their {@link Enum#name() names}. If the
 * given input is marked as {@link ExternallyNamed} its
 * {@link ExternallyNamed#getExternalName() external name} returned.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class EnumerationNameMapping implements Mapping<Enum<?>, String> {

	/**
	 * Singleton instance of {@link EnumerationNameMapping}.
	 */
	public static final Mapping<Enum<?>, String> INSTANCE = new EnumerationNameMapping(); 
	
	private EnumerationNameMapping() {
		// Singleton constructor.
	}

	@Override
	public String map(Enum<?> input) {
		if (input instanceof ExternallyNamed) {
			return ((ExternallyNamed) input).getExternalName();
		}
		return input.name();
	}

}
