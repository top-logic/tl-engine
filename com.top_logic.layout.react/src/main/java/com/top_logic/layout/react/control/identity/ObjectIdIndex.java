/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.identity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Ids of a set of objects, resolvable in both directions.
 *
 * <p>
 * The owner hands the current objects over ({@link #setObjects(Collection)}). An object keeps its
 * id for as long as it stays in the set, so a client may reuse what it renders for it; an object
 * that leaves the set loses its id, and one that enters gets a fresh id from the allocator.
 * </p>
 *
 * <p>
 * As {@link ObjectIdentities}, the index identifies its objects by their global business keys; an
 * owner naming them relative to a context object overrides {@link #keyContext()}.
 * </p>
 */
public class ObjectIdIndex implements ObjectIdentities {

	private final Supplier<String> _idAllocator;

	/** Ids of the indexed objects. */
	private Map<Object, String> _ids = new HashMap<>();

	/** Indexed objects by their ids. */
	private final Map<String, Object> _objects = new HashMap<>();

	/**
	 * Creates an empty {@link ObjectIdIndex}.
	 *
	 * @param idAllocator
	 *        Allocates the id of an object that enters the index, e.g. the id allocation of the
	 *        control's {@link com.top_logic.layout.react.ReactContext}.
	 */
	public ObjectIdIndex(Supplier<String> idAllocator) {
		_idAllocator = idAllocator;
	}

	/**
	 * Sets the indexed objects.
	 *
	 * <p>
	 * An object indexed before keeps its id, one not given loses it.
	 * </p>
	 *
	 * @param objects
	 *        The objects to index; an object given several times gets one id.
	 */
	public void setObjects(Collection<?> objects) {
		Map<Object, String> ids = new HashMap<>();
		_objects.clear();
		for (Object object : objects) {
			String id = ids.computeIfAbsent(object, this::existingOrNewId);
			_objects.put(id, object);
		}
		_ids = ids;
	}

	private String existingOrNewId(Object object) {
		String existing = _ids.get(object);
		return existing != null ? existing : _idAllocator.get();
	}

	@Override
	public String idOf(Object object) {
		return _ids.get(object);
	}

	@Override
	public Object objectOf(String id) {
		return _objects.get(id);
	}

	/**
	 * Whether the given object is indexed.
	 */
	public boolean contains(Object object) {
		return _ids.containsKey(object);
	}

}
