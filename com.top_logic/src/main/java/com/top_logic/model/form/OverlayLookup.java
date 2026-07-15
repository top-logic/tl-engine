/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form;

import com.top_logic.model.TLObject;

/**
 * Lookup for form overlay objects. Replaces {@code FormContext} in constraint and tracing APIs.
 *
 * <p>
 * Both {@code FormValidationModel} (new system) and {@code AttributeUpdateContainer} (legacy
 * system) implement this interface.
 * </p>
 */
public interface OverlayLookup {

	/**
	 * Finds the overlay for a persistent object.
	 *
	 * <p>
	 * If the given object is itself an overlay {@link TLObject}, it is returned directly. If a
	 * persistent object has an overlay registered in this lookup, the overlay is returned. Otherwise
	 * returns {@code null}, meaning callers should fall back to reading from the persistent object
	 * directly.
	 * </p>
	 *
	 * @param object
	 *        The object to find the overlay for.
	 * @return The overlay, or {@code null} if no overlay exists.
	 */
	TLObject getExistingOverlay(TLObject object);

	/**
	 * All overlays managed by this lookup.
	 *
	 * <p>
	 * Includes overlays for newly created objects (which have no persistent base object). Used by
	 * cross-object constraints that need to discover all objects in the form.
	 * </p>
	 */
	Iterable<? extends TLObject> getOverlays();
}
