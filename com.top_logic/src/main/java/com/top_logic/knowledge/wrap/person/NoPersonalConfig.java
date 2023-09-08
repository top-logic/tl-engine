/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

/**
 * {@link PersonalConfiguration} fall-back implementation, if there is no context available.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class NoPersonalConfig implements PersonalConfiguration {

	/**
	 * Singleton {@link NoPersonalConfig} instance.
	 */
	public static final NoPersonalConfig INSTANCE = new NoPersonalConfig();

	private NoPersonalConfig() {
		// Singleton constructor.
	}

	@Override
	public Object getValue(String aKey) {
		return null;
	}

	@Override
	public void setValue(String aName, Object aValue) {
		throw new UnsupportedOperationException("The default configuration cannot be modified.");
	}

}
