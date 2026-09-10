/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;

/**
 * Observes the objects held by {@link ViewChannel}s: reports a change of one of those objects
 * themselves, not of the channel values pointing to them.
 *
 * <p>
 * A display that decides by an attribute of the object on a channel - a switch testing its state, a
 * button whose executability depends on it, a derived channel computing from it - must follow that
 * attribute being edited while the channel keeps pointing to the same object. The observer registers
 * an object listener for every object the channels currently hold, re-points those listeners
 * whenever a channel value changes, and forwards each received {@link ModelChangeEvent} to the
 * callback.
 * </p>
 *
 * <p>
 * A change of a channel <em>value</em> reaches no callback: the holder binds to the channel itself
 * and reacts to a new value there, so reporting it here would run the reaction twice. Nor is
 * anything reported for a change that happened while the observer was detached - only what a
 * {@link #attach(ModelScope) attached} observer receives is forwarded, and a holder that must catch
 * up on a suspended display does so where it resumes.
 * </p>
 *
 * <p>
 * Beyond the objects on the channels, {@link ObservedTypes configured types} are observed, which
 * catches the creation of an object and the change of one the display reaches only indirectly (the
 * container of the channel's object, for instance).
 * </p>
 *
 * @see RowSourceObserver
 */
public class ChannelObjectObserver implements ModelListener, ViewChannel.ChannelListener {

	private final List<ViewChannel> _channels;

	private final Set<TLStructuredType> _observedTypes;

	private final Consumer<ModelChangeEvent> _onChange;

	/**
	 * The observed objects by their identity, so that a value listing the same object twice
	 * registers (and removes) a single listener.
	 */
	private final Map<ObjectKey, TLObject> _observedObjects = new LinkedHashMap<>();

	private ModelScope _scope;

	private boolean _attached;

	/**
	 * Creates a {@link ChannelObjectObserver}.
	 *
	 * @param channels
	 *        The channels whose objects are observed.
	 * @param observedTypes
	 *        Types whose object changes are observed in addition to the channels' objects (empty
	 *        observes just those).
	 * @param onChange
	 *        Receives every change of an observed object.
	 */
	public ChannelObjectObserver(List<ViewChannel> channels, Set<TLStructuredType> observedTypes,
			Consumer<ModelChangeEvent> onChange) {
		_channels = channels;
		_observedTypes = observedTypes;
		_onChange = onChange;
	}

	/**
	 * Creates a {@link ChannelObjectObserver} for a holder that re-reads the channels itself and
	 * therefore needs no details about the change.
	 *
	 * @param channels
	 *        The channels whose objects are observed.
	 * @param observedTypes
	 *        Types whose object changes are observed in addition to the channels' objects (empty
	 *        observes just those).
	 * @param onChange
	 *        Run on every change of an observed object.
	 */
	public ChannelObjectObserver(List<ViewChannel> channels, Set<TLStructuredType> observedTypes, Runnable onChange) {
		this(channels, observedTypes, event -> onChange.run());
	}

	/**
	 * Begins observing on the given {@link ModelScope}.
	 *
	 * <p>
	 * Idempotent: observing again while observing changes nothing.
	 * </p>
	 *
	 * @param scope
	 *        The scope the model listeners are registered on; {@code null} for a display built
	 *        outside a browser window, which observes no object but stays consistent - it can be
	 *        detached and attached like any other.
	 */
	public void attach(ModelScope scope) {
		if (_attached) {
			return;
		}
		_attached = true;
		_scope = scope;
		registerTypeListeners();
		registerObjectListeners();
		for (ViewChannel channel : _channels) {
			channel.addListener(this);
		}
	}

	/**
	 * Stops observing, removing every registered listener.
	 *
	 * <p>
	 * Idempotent: stopping again while not observing changes nothing.
	 * </p>
	 */
	public void detach() {
		if (!_attached) {
			return;
		}
		_attached = false;
		for (ViewChannel channel : _channels) {
			channel.removeListener(this);
		}
		deregisterObjectListeners();
		deregisterTypeListeners();
		_scope = null;
	}

	/**
	 * Points the object listeners at the objects the channels now hold.
	 */
	@Override
	public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
		deregisterObjectListeners();
		registerObjectListeners();
	}

	@Override
	public void notifyChange(ModelChangeEvent event) {
		_onChange.accept(event);
	}

	private void registerObjectListeners() {
		if (_scope == null) {
			return;
		}
		for (ViewChannel channel : _channels) {
			for (TLObject object : objects(channel.get())) {
				ObjectKey key = object.tId();
				if (key == null) {
					// An object without an identity is named by no change event, and the scope
					// registers no listener for it.
					continue;
				}
				if (_observedObjects.put(key, object) == null) {
					_scope.addModelListener(object, this);
				}
			}
		}
	}

	private void deregisterObjectListeners() {
		if (_scope != null) {
			for (TLObject object : _observedObjects.values()) {
				_scope.removeModelListener(object, this);
			}
		}
		_observedObjects.clear();
	}

	private void registerTypeListeners() {
		if (_scope == null) {
			return;
		}
		for (TLStructuredType type : _observedTypes) {
			_scope.addModelListener(type, this);
		}
	}

	private void deregisterTypeListeners() {
		if (_scope == null) {
			return;
		}
		for (TLStructuredType type : _observedTypes) {
			_scope.removeModelListener(type, this);
		}
	}

	/**
	 * The objects a channel value consists of: the value itself, the objects among the elements of a
	 * collection value, or none.
	 *
	 * @param value
	 *        The value of a channel, which may hold a single object or - for a multi-selection - a
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
