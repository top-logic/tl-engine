/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.identity;

import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The identities of the objects a control has at hand: the id the control displays each one under
 * and its business key.
 *
 * <p>
 * The commands of a control take objects by their ids (in properties marked
 * {@link DisplayedObjectId}). An id holds only in the session and only while the object is
 * displayed, so the script recorder replaces it by the object's {@link #keyOf(String) key}, and a
 * replay resolves the key back to the {@link #idOfKey(ModelName) id} the object has in the
 * replaying session.
 * </p>
 *
 * <p>
 * Objects are resolved only among those the control has at hand right now - the displayed nodes,
 * the rows of the current view, the options of a select, the displayed items. Resolving a key never
 * makes an object visible.
 * </p>
 */
public interface ObjectIdentities {

	/**
	 * The object the control displays under the given id, {@code null} if it displays none under
	 * it.
	 */
	Object objectOf(String id);

	/**
	 * The id the control displays the given object under, {@code null} if it does not display it.
	 */
	String idOf(Object object);

	/**
	 * The context object the keys of the objects are built relative to, {@code null} for global
	 * keys.
	 *
	 * @see ScriptingModelKey#name(Object, Object)
	 */
	default Object keyContext() {
		return null;
	}

	/**
	 * The business key of the object the control displays under the given id, {@code null} if it
	 * displays none under it or the object has no key.
	 */
	default ModelName keyOf(String id) {
		Object object = objectOf(id);
		return object == null ? null : ScriptingModelKey.name(keyContext(), object);
	}

	/**
	 * The id of the object the given business key designates, {@code null} if the key designates no
	 * object the control has at hand.
	 */
	default String idOfKey(ModelName key) {
		Object object = ScriptingModelKey.locate(keyContext(), key);
		return object == null ? null : idOf(object);
	}

}
