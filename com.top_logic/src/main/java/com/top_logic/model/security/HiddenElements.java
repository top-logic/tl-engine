/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * The value that a writer stores in a reference when it sees only a filtered value of the reference.
 *
 * <p>
 * Such a writer keeps the elements that it does not see. The rows of a composition table show only
 * the parts that the user may discover, and an agent run reads only the elements that it may read.
 * The value that such a writer gives back holds only elements that it saw. It cannot have removed
 * an element that it did not see, so this element stays at its stored position. Each writer gives
 * its own check of what it sees, because the right differs from writer to writer.
 * </p>
 */
public final class HiddenElements {

	private HiddenElements() {
		// Static utility.
	}

	/**
	 * The given written elements, with each stored element that the writer does not see put back at
	 * its stored position.
	 *
	 * <p>
	 * A hidden element goes to its stored index, or to the end if the written value is shorter. So the
	 * written elements keep their order, and in an ordered reference each hidden element keeps its
	 * place from the start. A hidden element that the written value holds already is not added again.
	 * </p>
	 *
	 * @param stored
	 *        The stored value of the reference, not filtered, in its stored order.
	 * @param written
	 *        The new value that the writer gives.
	 * @param seen
	 *        Whether the writer sees a stored element.
	 * @return The value to store.
	 */
	public static List<Object> keep(Collection<?> stored, Collection<?> written, Predicate<Object> seen) {
		List<Object> result = new ArrayList<>(written);
		int index = 0;
		for (Object element : stored) {
			if (!seen.test(element) && !result.contains(element)) {
				result.add(Math.min(index, result.size()), element);
			}
			index++;
		}
		return result;
	}
}
