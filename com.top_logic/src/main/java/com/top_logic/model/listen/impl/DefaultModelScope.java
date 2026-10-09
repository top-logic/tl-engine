/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen.impl;

import static java.util.Objects.*;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.listener.ListenerRegistration;
import com.top_logic.basic.listener.Registration;
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
 * <p>
 * Registrations for an observed object or type are kept in a {@link ModelListeners} registry per
 * object or type. A registry is dropped as soon as its last registration is disposed.
 * </p>
 * 
 * @see #eventBuilder()
 */
public class DefaultModelScope implements ModelScope {

	private final ModelEventSettings _settings;

	private final ModelListeners _globalListeners = new ModelListeners();

	private final Map<TLStructuredType, ModelListeners> _typeListeners = new LinkedHashMap<>();

	private final Map<ObjectKey, ModelListeners> _objectListeners = new LinkedHashMap<>();

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
	public Registration addModelListener(ModelListener listener) {
		return _globalListeners.register(listener);
	}

	@Override
	public Registration addModelListener(TLStructuredType type, ModelListener listener) {
		return register(_typeListeners, type, listener);
	}

	@Override
	public Registration addModelListener(TLObject object, ModelListener listener) {
		if (object == null) {
			return Registration.NONE;
		}
		if (object.tId() == null) {
			/* TransientObject or something similar. Ignore, as tId() is used here and must not be null. */
			return Registration.NONE;
		}
		return register(_objectListeners, object.tId(), listener);
	}

	@Deprecated
	@Override
	public boolean removeModelListener(ModelListener listener) {
		return _globalListeners.removeListener(listener);
	}

	@Deprecated
	@Override
	public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
		return remove(_typeListeners, type, listener);
	}

	@Deprecated
	@Override
	public boolean removeModelListener(TLObject object, ModelListener listener) {
		if (object == null) {
			return false;
		}
		if (object.tId() == null) {
			return false;
		}
		return remove(_objectListeners, object.tId(), listener);
	}

	private static <K> Registration register(Map<K, ModelListeners> registries, K key, ModelListener listener) {
		ModelListeners registry = registries.computeIfAbsent(key, x -> new ModelListeners());
		return new KeyedRegistration<>(registries, key, registry, registry.register(listener));
	}

	@SuppressWarnings("deprecation")
	private static <K> boolean remove(Map<K, ModelListeners> registries, K key, ModelListener listener) {
		ModelListeners registry = registries.get(key);
		if (registry == null) {
			return false;
		}
		boolean result = registry.removeListener(listener);
		dropIfEmpty(registries, key, registry);
		return result;
	}

	private static <K> void dropIfEmpty(Map<K, ModelListeners> registries, K key, ModelListeners registry) {
		if (!registry.hasRegisteredListeners()) {
			registries.remove(key, registry);
		}
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

	/**
	 * {@link Registration} of a listener for an observed object or type that drops the registry of
	 * the object or type, when its last registration is disposed.
	 */
	private static final class KeyedRegistration<K> implements Registration {

		private final Map<K, ModelListeners> _registries;

		private final K _key;

		private final ModelListeners _registry;

		private final ListenerRegistration<ModelListener> _registration;

		KeyedRegistration(Map<K, ModelListeners> registries, K key, ModelListeners registry,
				ListenerRegistration<ModelListener> registration) {
			_registries = registries;
			_key = key;
			_registry = registry;
			_registration = registration;
		}

		@Override
		public void dispose() {
			if (!_registration.isActive()) {
				return;
			}
			_registration.dispose();
			dropIfEmpty(_registries, _key, _registry);
		}

		@Override
		public boolean isActive() {
			return _registration.isActive();
		}

	}

}
