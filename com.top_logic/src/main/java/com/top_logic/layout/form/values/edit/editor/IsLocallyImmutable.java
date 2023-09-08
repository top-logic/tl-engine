/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.form.model.FieldMode;

/**
 * Mapping deciding wether a given {@link FieldMode} is either {@link FieldMode#IMMUTABLE}, or
 * {@link FieldMode#LOCALLY_IMMUTABLE}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class IsLocallyImmutable implements Mapping<FieldMode, Boolean> {
	
	/**
	 * Singleton {@link IsLocallyImmutable} instance.
	 */
	public static final IsLocallyImmutable INSTANCE = new IsLocallyImmutable();
	
	private IsLocallyImmutable() {
		// Singleton constructor.
	}

	@Override
	public Boolean map(FieldMode mode) {
		if (mode == null) {
			return Boolean.FALSE;
		}
		switch (mode) {
			case IMMUTABLE:
			case LOCALLY_IMMUTABLE:
				return Boolean.TRUE;
			default:
				return Boolean.FALSE;
		}
	}
}