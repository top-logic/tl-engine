/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.model.TLObject;

/**
 * A changing set of {@link TLObject}s a single {@link ModelListener} observes on a
 * {@link ModelScope}.
 *
 * <p>
 * A display showing model objects - a cell holding the label of a referenced object, an input
 * offering objects to choose from - must follow an object it shows being edited, although nothing
 * about the display itself changed. Such a display holds an {@link ObservedObjects} for its
 * listener, {@link #attach(ModelScope) attaches} it while it is on screen, and hands it the objects
 * it currently shows through {@link #observe(Collection)} whenever that set changes. Every change of
 * one of those objects then reaches the listener.
 * </p>
 *
 * <p>
 * Handing over a set of objects is a difference to the set observed so far: an object shown before
 * and after keeps its single registration, an object no longer shown is deregistered, an object
 * newly shown is registered. The set is kept independently of the scope, so it may be given before
 * the first {@link #attach(ModelScope)} - it is registered then - and it survives a
 * {@link #detach()}, which a later {@link #attach(ModelScope)} registers again.
 * </p>
 *
 * <p>
 * Objects are held by their identity ({@link TLObject#tId()}), so that a set listing the same object
 * twice registers a single listener. An object without an identity is named by no
 * {@link ModelChangeEvent} and therefore skipped.
 * </p>
 *
 * @see ModelScope#addModelListener(TLObject, ModelListener)
 */
public class ObservedObjects {

	private final ModelListener _listener;

	/**
	 * The observed objects by their identity, in the order they were given.
	 */
	private final Map<ObjectKey, TLObject> _observed = new LinkedHashMap<>();

	private ModelScope _scope;

	private boolean _attached;

	/**
	 * Creates an {@link ObservedObjects} registering the given listener.
	 *
	 * @param listener
	 *        The listener registered for every observed object, informed about its changes.
	 */
	public ObservedObjects(ModelListener listener) {
		_listener = listener;
	}

	/**
	 * Registers the observed objects on the given scope and keeps registering the objects given
	 * later.
	 *
	 * <p>
	 * Idempotent: attaching again while attached changes nothing.
	 * </p>
	 *
	 * @param scope
	 *        The scope the listeners are registered on; {@code null} for a display built outside a
	 *        browser window, which observes nothing but stays consistent - it can be detached and
	 *        attached like any other.
	 */
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		_scope = scope;
		if (_scope != null) {
			for (TLObject object : _observed.values()) {
				_scope.addModelListener(object, _listener);
			}
		}
	}

	/**
	 * Deregisters every observed object, keeping the set for a later {@link #attach(ModelScope)}.
	 *
	 * <p>
	 * Idempotent: detaching again while detached changes nothing.
	 * </p>
	 */
	public void detach() {
		if (!_attached) {
			return;
		}
		if (_scope != null) {
			for (TLObject object : _observed.values()) {
				_scope.removeModelListener(object, _listener);
			}
		}
		_attached = false;
		_scope = null;
	}

	/**
	 * Whether {@link #attach(ModelScope)} was called and {@link #detach()} was not.
	 */
	public boolean isAttached() {
		return _attached;
	}

	/**
	 * Makes the given objects the observed ones.
	 *
	 * @param objects
	 *        The objects to observe from now on; an empty collection observes none. An object
	 *        without an identity ({@link TLObject#tId()}) is skipped, the same object listed twice
	 *        is observed once.
	 */
	public void observe(Collection<? extends TLObject> objects) {
		Map<ObjectKey, TLObject> observed = new LinkedHashMap<>();
		for (TLObject object : objects) {
			if (object == null) {
				continue;
			}
			ObjectKey key = object.tId();
			if (key == null) {
				// An object without an identity is named by no change event, and the scope
				// registers no listener for it.
				continue;
			}
			observed.putIfAbsent(key, object);
		}

		if (_attached && _scope != null) {
			for (Map.Entry<ObjectKey, TLObject> entry : _observed.entrySet()) {
				if (!observed.containsKey(entry.getKey())) {
					_scope.removeModelListener(entry.getValue(), _listener);
				}
			}
			for (Map.Entry<ObjectKey, TLObject> entry : observed.entrySet()) {
				if (!_observed.containsKey(entry.getKey())) {
					_scope.addModelListener(entry.getValue(), _listener);
				}
			}
		}

		_observed.clear();
		_observed.putAll(observed);
	}

	/**
	 * Makes the objects a displayed value consists of the observed ones.
	 *
	 * @param value
	 *        A displayed value, see {@link #objects(Object)}.
	 */
	public void observeValue(Object value) {
		observe(objects(value));
	}

	/**
	 * The objects currently observed, in the order they were given.
	 */
	public Collection<TLObject> observedObjects() {
		return Collections.unmodifiableCollection(_observed.values());
	}

	/**
	 * The identities of the {@link #observedObjects()}.
	 */
	public Set<ObjectKey> observedKeys() {
		return Collections.unmodifiableSet(_observed.keySet());
	}

	/**
	 * The objects a displayed value consists of: the value itself, the objects among the elements of
	 * a collection value, or none.
	 *
	 * @param value
	 *        A displayed value, which may be a single object or - for a multi-selection - a
	 *        collection of them.
	 * @return The objects the given value holds, in the order the value lists them.
	 */
	public static Collection<TLObject> objects(Object value) {
		if (value instanceof TLObject object) {
			return List.of(object);
		}
		if (value instanceof Collection<?> values) {
			List<TLObject> result = new ArrayList<>(values.size());
			for (Object element : values) {
				if (element instanceof TLObject object) {
					result.add(object);
				}
			}
			return result;
		}
		return List.of();
	}

}
