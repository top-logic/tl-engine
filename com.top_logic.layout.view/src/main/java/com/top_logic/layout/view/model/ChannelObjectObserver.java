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
 * and reacts to a new value there, so reporting it here would run the reaction twice. A change that
 * happens while the observer is detached reaches no listener at all - nothing is registered then -
 * and is caught up where the observation resumes: an observer built for a holder that re-reads the
 * channels itself (see
 * {@link #ChannelObjectObserver(List, Set, Runnable) the callback taking no event}) runs that
 * callback once on {@link #attach(ModelScope)} after a {@link #detach()}, so the display rebuilds
 * from the objects as they are now. A holder that is handed the {@link ModelChangeEvent} itself
 * hears nothing there: no event describes what was missed.
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

	/** Resume action of an observer that has nothing to say about a change it did not see. */
	private static final Runnable NOTHING = () -> {
		// The holder is handed the change itself, and no change was recorded while nobody observed.
	};

	private final List<ViewChannel> _channels;

	private final Set<TLStructuredType> _observedTypes;

	private final Consumer<ModelChangeEvent> _onChange;

	private final Runnable _onResume;

	/**
	 * The objects the channels currently hold, observed for this listener.
	 */
	private final ObservedObjects _observedObjects = new ObservedObjects(this);

	private ModelScope _scope;

	private boolean _attached;

	/** Whether the observation was stopped and has not begun again. */
	private boolean _suspended;

	/**
	 * Creates a {@link ChannelObjectObserver}.
	 *
	 * @param channels
	 *        The channels whose objects are observed.
	 * @param observedTypes
	 *        Types whose object changes are observed in addition to the channels' objects (empty
	 *        observes just those).
	 * @param onChange
	 *        Receives every change of an observed object. A change that happened while the
	 *        observation was stopped reaches it as little as any other unseen change: there is no
	 *        event describing it.
	 */
	public ChannelObjectObserver(List<ViewChannel> channels, Set<TLStructuredType> observedTypes,
			Consumer<ModelChangeEvent> onChange) {
		this(channels, observedTypes, onChange, NOTHING);
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
	 *        Run on every change of an observed object, and once when the observation resumes, where
	 *        the objects may have been changed unseen.
	 */
	public ChannelObjectObserver(List<ViewChannel> channels, Set<TLStructuredType> observedTypes, Runnable onChange) {
		this(channels, observedTypes, event -> onChange.run(), onChange);
	}

	/**
	 * Creates a {@link ChannelObjectObserver} reporting changes to the given callback, and resuming
	 * through the given action.
	 *
	 * @param channels
	 *        The channels whose objects are observed.
	 * @param observedTypes
	 *        Types whose object changes are observed in addition to the channels' objects (empty
	 *        observes just those).
	 * @param onChange
	 *        Receives every change of an observed object.
	 * @param onResume
	 *        Run by {@link #attach(ModelScope)} where it begins an observation that was stopped
	 *        before; {@link #NOTHING} where nothing can be said about what was missed.
	 */
	private ChannelObjectObserver(List<ViewChannel> channels, Set<TLStructuredType> observedTypes,
			Consumer<ModelChangeEvent> onChange, Runnable onResume) {
		_channels = channels;
		_observedTypes = observedTypes;
		_onChange = onChange;
		_onResume = onResume;
	}

	/**
	 * Begins observing on the given {@link ModelScope}.
	 *
	 * <p>
	 * Idempotent: observing again while observing changes nothing.
	 * </p>
	 *
	 * <p>
	 * Resuming an observation that was stopped reports a change to a holder that re-reads the
	 * channels itself: the objects on them were followed by nobody in the meantime, so what they
	 * carry now is unknown and the holder reads it again.
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
		if (_suspended) {
			_suspended = false;
			_onResume.run();
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
		_suspended = true;
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

	@Override
	public void notifyChange(ModelChangeEvent event) {
		_onChange.accept(event);
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
