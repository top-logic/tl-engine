/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;

/**
 * {@link ConstraintCheck} that checks that there is no cycle in the attribute value.
 * 
 * <p>
 * This check ensures that there is no sequence of values "a_0, a_1,... ,a_n, a_0", so that a_i+1 is
 * in the attribute value of a_i and a_0 is in the attribute value of a_n.
 * </p>
 */
@InApp
public class NoAttributeCycle implements ConstraintCheck {

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		Map<TLObject, Collection<? extends TLObject>> valueCache = new HashMap<>();

		List<List<TLObject>> pathsToProcess = new ArrayList<>();
		pathsToProcess.add(new ArrayList<>(Arrays.asList(object)));
		while (!pathsToProcess.isEmpty()) {
			List<TLObject> path = pathsToProcess.remove(pathsToProcess.size() - 1);
			TLObject tail = path.get(path.size() - 1);
			Collection<? extends TLObject> collection = valueCache.get(tail);
			if (collection == null) {
				Object attrValue = tail.tValue(attribute);
				Optional<Collection<? extends TLObject>> value = asListOfTLObject(attrValue);
				if (value.isEmpty()) {
					return I18NConstants.ERROR_VALUE_NOT_LIST_OF_TLOBJECT__OBJECT_ATTRIBUTE_VALUE.fill(tail, attribute,
						attrValue);
				}
				collection = value.get();
				valueCache.put(tail, collection);
			}
			if (collection.isEmpty()) {
				// No cycle.
				continue;
			}
			Iterator<? extends TLObject> iterator = collection.iterator();
			boolean hasNext;
			do {
				List<TLObject> extendedPath = path;
				TLObject item = iterator.next();
				hasNext = iterator.hasNext();
				if (hasNext) {
					extendedPath = new ArrayList<>(path);
				}
				extendedPath.add(item);
				if (item == object) {
					return I18NConstants.ERROR_CYLCE_NOT_ALLOWED__OBJECT_ATTRIBUTE_VALUE_CYCLE.fill(object, attribute,
						object.tValue(attribute), extendedPath);
				}
				pathsToProcess.add(extendedPath);
			} while (hasNext);
		}
		return null;
	}

	private static Optional<Collection<? extends TLObject>> asListOfTLObject(Object value) {
		if (value instanceof TLObject) {
			return Optional.of(Collections.singletonList((TLObject) value));
		}
		if (value == null) {
			return Optional.of(Collections.emptyList());
		}
		if (value instanceof Collection<?>) {
			if (((Collection<?>) value).stream().anyMatch(NoAttributeCycle::isNoTLObject)) {
				return Optional.empty();
			}
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Collection<? extends TLObject> tlObjectCollection = (Collection) value;
			return Optional.of(tlObjectCollection);
		}
		return Optional.empty();
	}

	private static boolean isNoTLObject(Object o) {
		return !(o instanceof TLObject);
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace) {
		Set<TLObject> seen = new HashSet<>();
		List<TLObject> remaining = new ArrayList<>();
		remaining.add(object);
		while (!remaining.isEmpty()) {
			TLObject item = remaining.remove(remaining.size() - 1);
			if (!seen.add(item)) {
				continue;
			}
			Optional<Collection<? extends TLObject>> value = asListOfTLObject(item.tValue(attribute));
			if (value.isEmpty()) {
				continue;
			}
			remaining.addAll(value.get());
			if (item != object) {
				trace.add(Pointer.create(item, attribute));
			}
		}

	}

}
