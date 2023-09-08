/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

/**
 * {@link OptionMapping} where options can be used as selection. No conversion happens.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IdentityOptionMapping implements OptionMapping {

	/** Singleton {@link IdentityOptionMapping} instance. */
	public static final IdentityOptionMapping INSTANCE = new IdentityOptionMapping();

	private IdentityOptionMapping() {
		// singleton instance
	}

	@Override
	public Object toSelection(Object option) {
		return option;
	}

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		return selection;
	}

}

