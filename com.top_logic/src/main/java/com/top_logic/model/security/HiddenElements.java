/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * The value to store in a reference for a writer that reads the value with a narrower check than
 * the stored value has.
 *
 * <p>
 * Such a writer sees only a part of the stored elements, and the value that it gives back holds
 * only elements that it saw. It cannot have removed an element that it did not see, so this element
 * stays at its stored position. Each writer gives its own check of what it sees.
 * </p>
 */
@FrameworkInternal
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
	 * Each copy counts: a reference that holds a hidden element twice keeps the second copy if the
	 * written value holds the element once.
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
		Map<Object, Integer> unmatched = new HashMap<>();
		for (Object element : written) {
			unmatched.merge(element, 1, Integer::sum);
		}
		int index = 0;
		for (Object element : stored) {
			if (!seen.test(element)) {
				Integer copies = unmatched.get(element);
				if (copies == null) {
					result.add(Math.min(index, result.size()), element);
				} else if (copies == 1) {
					unmatched.remove(element);
				} else {
					unmatched.put(element, copies - 1);
				}
			}
			index++;
		}
		return result;
	}
}
