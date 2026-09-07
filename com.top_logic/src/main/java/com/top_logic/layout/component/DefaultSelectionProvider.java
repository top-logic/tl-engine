/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Collection;

/**
 * Algorithm computing the default selection of a data component from its model.
 *
 * <p>
 * The selection is derived from the component's model and its last selection alone, without
 * enumerating the available rows or nodes. This allows a (tree-)table, grid or tree to select an
 * object that is currently collapsed or not yet loaded, revealing it on demand, instead of
 * materializing all rows or the complete tree only to pick a default.
 * </p>
 *
 * <p>
 * A component that selects from a fixed, in-memory option list uses {@link ListSelectionProvider}
 * instead, which receives the options directly.
 * </p>
 */
public interface DefaultSelectionProvider {

	/**
	 * Computes the objects to select by default.
	 *
	 * @param model
	 *        The current component's model.
	 * @param lastSelection
	 *        The last selection (before the last change of the component's model), or
	 *        <code>null</code> if there was none. This selection may no longer be available, but a
	 *        new selection can be derived from it. In case of a multi-selection, this is a
	 *        {@link Collection} of objects.
	 * @return The business objects to select by default. The component reveals and selects them; a
	 *         single-selection component uses the first element. An empty result selects nothing.
	 */
	Collection<?> computeDefaultSelection(Object model, Object lastSelection);

}
