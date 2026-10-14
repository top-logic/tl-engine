/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ObservedObjects;

/**
 * The treatment of deleted objects in the values of {@link ViewChannel}s.
 *
 * <p>
 * A channel value is a single object, a collection of objects - a multi-selection -, or any other
 * value. An object of it that is no longer {@link TLObject#tValid() valid} names nothing: the value
 * without it is what the value means.
 * </p>
 *
 * @see DefaultViewChannel
 */
public final class DeletedObjects {

	private DeletedObjects() {
		// Static utilities only.
	}

	/**
	 * Whether the given value is a deleted {@link TLObject}.
	 */
	public static boolean isDeleted(Object value) {
		return value instanceof TLObject object && !object.tValid();
	}

	/**
	 * The given value without the objects in it that are deleted.
	 *
	 * <p>
	 * A single {@link TLObject} that is not {@link TLObject#tValid() valid} any more is answered
	 * with {@code null}. A collection holding such objects is answered with a copy leaving them out
	 * - a {@link Set} by a set, any other collection by a list, both in the original order; a
	 * collection of which nothing is left stays an empty collection. A value without deleted
	 * objects, and any other value, is answered unchanged - the very same instance.
	 * </p>
	 *
	 * @param value
	 *        The value of a channel, or a value supplied in its place.
	 * @return The value the given one means.
	 */
	public static Object withoutDeleted(Object value) {
		if (value instanceof TLObject) {
			return isDeleted(value) ? null : value;
		}
		if (value instanceof Collection<?> elements) {
			if (ObservedObjects.objects(elements).stream().allMatch(TLObject::tValid)) {
				return value;
			}
			Collection<Object> result = elements instanceof Set ? new LinkedHashSet<>() : new ArrayList<>();
			for (Object element : elements) {
				if (!isDeleted(element)) {
					result.add(element);
				}
			}
			return result;
		}
		return value;
	}

	/**
	 * Whether the given channel value is the given objects with some of them left out, and every
	 * one left out is deleted.
	 *
	 * <p>
	 * That is the value a channel announces when objects it held are deleted (see
	 * {@link DefaultViewChannel}): it asks for nothing new, it only tells that the deleted objects
	 * are gone.
	 * </p>
	 *
	 * @param objects
	 *        The objects a reader of the channel displays.
	 * @param value
	 *        The new value of the channel: a single object, a collection of them, or {@code null}
	 *        for none.
	 * @return Whether the value names none but the given objects, and leaves out at least one of
	 *         them, and only deleted ones.
	 */
	public static boolean dropsDeletedOnly(Collection<?> objects, Object value) {
		Collection<?> remaining;
		if (value == null) {
			remaining = Set.of();
		} else if (value instanceof Collection<?> elements) {
			remaining = elements;
		} else {
			remaining = Set.of(value);
		}
		if (!objects.containsAll(remaining)) {
			return false;
		}
		boolean dropped = false;
		for (Object object : objects) {
			if (!remaining.contains(object)) {
				if (!isDeleted(object)) {
					return false;
				}
				dropped = true;
			}
		}
		return dropped;
	}

}
