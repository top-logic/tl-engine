/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.func.Function1;
import com.top_logic.dob.attr.BinaryAttributeKind;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;

/**
 * {@link DynamicMode} functions showing the store and threshold properties of a binary storage
 * configuration only for the {@link BinaryAttributeKind}s that use them.
 *
 * <p>
 * The argument of each function is the configured kind. A missing kind (<code>null</code>) stands
 * for a kind taken from elsewhere, e.g. a configured default; then the property is shown.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class BinaryStorageFieldModes {

	private BinaryStorageFieldModes() {
		// Namespace for the mode functions.
	}

	/**
	 * Shows a store property for the kinds storing content in a blob store.
	 */
	public static class StoreMode extends Function1<FieldMode, BinaryAttributeKind> {

		@Override
		public FieldMode apply(BinaryAttributeKind kind) {
			return kind == null || kind.isExternal() ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
		}

	}

	/**
	 * Shows a threshold property for the {@link BinaryAttributeKind#HYBRID} kind.
	 */
	public static class ThresholdMode extends Function1<FieldMode, BinaryAttributeKind> {

		@Override
		public FieldMode apply(BinaryAttributeKind kind) {
			return kind == null || kind == BinaryAttributeKind.HYBRID ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
		}

	}

}
