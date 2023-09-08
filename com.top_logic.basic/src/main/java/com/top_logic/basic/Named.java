/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.col.Mapping;

/**
 * Represents an entity with a name.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface Named {

	/**
	 * Mapping of a {@link Named} to its {@link Named#getName() name}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class NameMapping implements Mapping<Named, String> {

		/** Singleton {@link NameMapping} instance. */
		public static final NameMapping INSTANCE = new Named.NameMapping();

		/**
		 * Creates a new {@link NameMapping}.
		 */
		private NameMapping() {
			// singleton instance
		}

		@Override
		public String map(Named input) {
			return input.getName();
		}

	}

	/**
	 * Returns the name of this, may be <code>null</code>.
	 */
	String getName();
}
