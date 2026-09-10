/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Resolution of the configured type references of a display to the types it observes in the
 * {@link ModelScope}.
 *
 * @see ChannelObjectObserver
 * @see RowSourceObserver
 */
public class ObservedTypes {

	/**
	 * Not instantiated: a namespace for {@link #resolve(List)}.
	 */
	private ObservedTypes() {
		// No instances.
	}

	/**
	 * The types the given references name.
	 *
	 * @param refs
	 *        The configured references, {@code null} or empty for a display observing no type.
	 * @return The resolved types, empty where no type is configured.
	 * @throws RuntimeException
	 *         If a reference names no type of the application model.
	 */
	public static Set<TLStructuredType> resolve(List<TLModelPartRef> refs) {
		if (refs == null || refs.isEmpty()) {
			return Set.of();
		}
		Set<TLStructuredType> types = new HashSet<>();
		for (TLModelPartRef ref : refs) {
			TLStructuredType type = (TLStructuredType) ref.resolveType();
			if (type == null) {
				throw new RuntimeException("Failed to resolve observed type: " + ref.qualifiedName());
			}
			types.add(type);
		}
		return types;
	}

}
