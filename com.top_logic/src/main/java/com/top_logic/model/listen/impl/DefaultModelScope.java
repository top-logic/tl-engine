/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen.impl;

import static java.util.Objects.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;

/**
 * Default {@link ModelScope} implementation that transforms {@link UpdateEvent}s to
 * {@link ModelChangeEvent} events and dispatches them to registered listeners.
 * 
 * @see #eventBuilder()
 */
public class DefaultModelScope implements ModelScope {

	private final ModelEventSettings _settings;

	private final Set<ModelListener> _globalListeners = new LinkedHashSet<>();

	private final Map<TLStructuredType, Set<ModelListener>> _typeListeners = new LinkedHashMap<>();

	private final Map<ObjectKey, Set<ModelListener>> _objectListeners = new LinkedHashMap<>();

	/**
	 * Creates a {@link DefaultModelScope}.
	 */
	public DefaultModelScope(ModelEventSettings settings) {
		_settings = requireNonNull(settings);
	}

	/**
	 * The {@link ModelEventSettings} to in use.
	 */
	public final ModelEventSettings settings() {
		return _settings;
	}

	@Override
	public boolean addModelListener(ModelListener listener) {
		return _globalListeners.add(listener);
	}

	@Override
	public boolean addModelListener(TLStructuredType type, ModelListener listener) {
		Set<ModelListener> listeners = _typeListeners.computeIfAbsent(type, x -> new LinkedHashSet<>());
		return listeners.add(listener);
	}

	@Override
	public boolean addModelListener(TLObject object, ModelListener listener) {
		if (object == null) {
			return false;
		}
		if (object.tId() == null) {
			/* TransientObject or something similar. Ignore, as tId() is used here and must not be null. */
			return false;
		}
		Set<ModelListener> listeners = _objectListeners.computeIfAbsent(object.tId(), x -> new LinkedHashSet<>());
		return listeners.add(listener);
	}

	@Override
	public boolean removeModelListener(ModelListener listener) {
		return _globalListeners.remove(listener);
	}

	@Override
	public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
		Set<ModelListener> listeners = _typeListeners.get(type);
		if (listeners == null) {
			return false;
		}
		boolean result = listeners.remove(listener);
		if (listeners.isEmpty()) {
			_typeListeners.remove(type);
		}
		return result;
	}

	@Override
	public boolean removeModelListener(TLObject object, ModelListener listener) {
		if (object == null) {
			return false;
		}
		if (object.tId() == null) {
			return false;
		}
		Set<ModelListener> listeners = _objectListeners.get(object.tId());
		if (listeners == null) {
			return false;
		}
		boolean result = listeners.remove(listener);
		if (listeners.isEmpty()) {
			_objectListeners.remove(object.tId());
		}
		return result;
	}

	/**
	 * Creates a builder for a {@link ModelChangeEvent}.
	 * 
	 * @see EventBuilder#add(UpdateEvent)
	 * @see EventBuilder#notifyListeners()
	 */
	public EventBuilder eventBuilder() {
		return new EventBuilder(_settings, _objectListeners, _typeListeners, _globalListeners);
	}

}
