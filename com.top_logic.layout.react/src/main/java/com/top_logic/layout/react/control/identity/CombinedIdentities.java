/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.identity;

import java.util.List;

import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The {@link ObjectIdentities} of a control displaying several kinds of objects, each kind with
 * ids of its own - e.g. the cards and the columns of a board.
 *
 * <p>
 * The ids of the parts are disjoint, so an id resolves in the part that allocated it. An object
 * displayed by several parts is identified by the first of them.
 * </p>
 */
public final class CombinedIdentities implements ObjectIdentities {

	private final List<ObjectIdentities> _parts;

	/**
	 * Creates a {@link CombinedIdentities}.
	 *
	 * @param parts
	 *        The identities of the kinds of objects, in the order an object displayed by several of
	 *        them is identified by.
	 */
	public CombinedIdentities(ObjectIdentities... parts) {
		_parts = List.of(parts);
	}

	@Override
	public Object objectOf(String id) {
		for (ObjectIdentities part : _parts) {
			Object object = part.objectOf(id);
			if (object != null) {
				return object;
			}
		}
		return null;
	}

	@Override
	public String idOf(Object object) {
		for (ObjectIdentities part : _parts) {
			String id = part.idOf(object);
			if (id != null) {
				return id;
			}
		}
		return null;
	}

	/**
	 * The key of the object the part that displays it under the given id builds.
	 */
	@Override
	public ModelName keyOf(String id) {
		for (ObjectIdentities part : _parts) {
			if (part.objectOf(id) != null) {
				return part.keyOf(id);
			}
		}
		return null;
	}

	/**
	 * The id of the object the key designates in the first part that displays it.
	 */
	@Override
	public String idOfKey(ModelName key) {
		for (ObjectIdentities part : _parts) {
			String id = part.idOfKey(key);
			if (id != null) {
				return id;
			}
		}
		return null;
	}

}
