/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.listen.ObservedObjects;

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
 * <p>
 * A deletion is told apart from a change: while one of the objects the channels hold is
 * {@link TLObject#tValid() invalid}, every received event runs the deletion callback of
 * {@link #ChannelObjectObserver(List, Set, Consumer, Consumer)} instead of the change one. The
 * deleting transaction is committed - and its model event delivered - before the channel is
 * written, so the channel points at the deleted object for the span of that delivery; evaluating an
 * expression over it would fail. A holder that has nothing to do with a deletion therefore keeps
 * its display until the channel delivers its next value, which its ordinary channel listener then
 * displays; a holder that wants the deletion - a link hiding itself once its target is gone -
 * reacts in the deletion callback.
 * </p>
 *
 * @see RowSourceObserver
 */
public class ChannelObjectObserver implements ModelListener, ViewChannel.ChannelListener {

	/**
	 * Change callback of a holder that reacts to nothing but the deletion of an observed object.
	 *
	 * @see #ChannelObjectObserver(List, Set, Consumer, Consumer)
	 */
	public static final Consumer<ModelChangeEvent> IGNORE_CHANGE = event -> {
		// The holder displays a value of its channel, which writes it whenever it changes.
	};

	/**
	 * Deletion callback of a holder that keeps its display until the channel delivers its next
	 * value.
	 */
	private static final Consumer<ModelChangeEvent> IGNORE_DELETION = event -> {
		// The holder displays what it displayed before; the channel write following the deletion
		// updates it.
	};

	private final List<ViewChannel> _channels;

	private final Set<TLStructuredType> _observedTypes;

	private final Consumer<ModelChangeEvent> _onChange;

	private final Consumer<ModelChangeEvent> _onDeleted;

	/**
	 * The objects the channels currently hold, observed for this listener.
	 */
	private final ObservedObjects _observedObjects = new ObservedObjects(this);

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
	 *        Receives every change of an observed object, as long as all objects the channels hold
	 *        are {@link TLObject#tValid() valid}.
	 * @param onDeleted
	 *        Receives every event that arrives while one of the objects the channels hold is
	 *        {@link TLObject#tValid() invalid}. Such an event reaches {@code onChange} no more, so
	 *        a holder evaluating an expression over the channel is spared the deleted object.
	 */
	public ChannelObjectObserver(List<ViewChannel> channels, Set<TLStructuredType> observedTypes,
			Consumer<ModelChangeEvent> onChange, Consumer<ModelChangeEvent> onDeleted) {
		_channels = channels;
		_observedTypes = observedTypes;
		_onChange = onChange;
		_onDeleted = onDeleted;
	}

	/**
	 * Creates a {@link ChannelObjectObserver} for a holder that has nothing to do when an observed
	 * object is deleted: it keeps its display until the channel delivers its next value.
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
		this(channels, observedTypes, onChange, IGNORE_DELETION);
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
		this(channels, observedTypes, event -> onChange.run(), IGNORE_DELETION);
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
		_observedObjects.attach(scope);
		updateObservedObjects();
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
		_observedObjects.detach();
		deregisterTypeListeners();
		_scope = null;
	}

	/**
	 * Points the object listeners at the objects the channels now hold.
	 */
	@Override
	public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
		updateObservedObjects();
	}

	/**
	 * Reports the event as a change, or as a deletion while one of the objects the channels hold is
	 * {@link TLObject#tValid() invalid}.
	 */
	@Override
	public void notifyChange(ModelChangeEvent event) {
		if (holdsDeletedObject()) {
			_onDeleted.accept(event);
		} else {
			_onChange.accept(event);
		}
	}

	/**
	 * Whether one of the objects the channels currently hold is deleted.
	 *
	 * <p>
	 * The objects themselves are inspected, not what the event reports: an event that names other
	 * objects - the change of another object of an {@link ObservedTypes observed type}, say -
	 * arrives at a channel still pointing to the deleted object just as the deletion event does.
	 * </p>
	 */
	private boolean holdsDeletedObject() {
		for (ViewChannel channel : _channels) {
			for (TLObject object : objects(channel.get())) {
				if (!object.tValid()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Points the observation at the objects the channels now hold.
	 */
	private void updateObservedObjects() {
		List<TLObject> objects = new ArrayList<>();
		for (ViewChannel channel : _channels) {
			objects.addAll(objects(channel.get()));
		}
		_observedObjects.observe(objects);
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
		return ObservedObjects.objects(value);
	}

}
