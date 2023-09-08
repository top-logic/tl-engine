/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Dummy implementation of {@link OptionMapping} to be used default in {@link Options} annotation.
 * 
 * @see Options#mapping()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class InvalidOptionMapping implements OptionMapping {

	private InvalidOptionMapping() {
		// Provide instantiation.
	}

	@Override
	public Object toSelection(Object option) {
		throw new UnsupportedOperationException(
			InvalidOptionMapping.class.getName() + " is just a dummy implementation and must not be used active.");
	}

}

