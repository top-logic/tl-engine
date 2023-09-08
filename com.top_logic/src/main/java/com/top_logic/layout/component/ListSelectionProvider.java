/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Algorithm for choosing a default selection.
 */
public interface ListSelectionProvider {

	/**
	 * Default {@link ListSelectionProvider} that selects the first value available.
	 */
	ListSelectionProvider FIRST_VALUE = new ListSelectionProvider() {
		@Override
		public Collection<?> computeDefaultSelection(Object model, List<?> options, Object lastSelection) {
			if (options.isEmpty()) {
				return Collections.emptySet();
			}
			return Collections.singleton(options.get(0));
		}
	};

	/**
	 * Chooses a default selection.
	 *
	 * @param model
	 *        The current component's model.
	 * @param options
	 *        The available options.
	 * @param lastSelection
	 *        The last selection (before the last change of the component's model). This selection
	 *        may now no longer be available as option, but the new selection could be derived from
	 *        it. In case of a multi-selection, the last selection may be a collection of objects.
	 * @return The option to select by default.
	 */
	Collection<?> computeDefaultSelection(Object model, List<?> options, Object lastSelection);

}
