/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.top_logic.basic.listener.Registration;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelChangeEvent.ChangeType;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link ModelScope} a test delivers object changes through, standing in for the scope of a
 * browser window.
 *
 * <p>
 * Listeners for single objects and for the objects of a type are supported. The registrations are
 * recorded as made, so that a test can check that a listener is registered exactly once.
 * </p>
 */
public class RecordingScope implements ModelScope {

	private final Map<ObjectKey, List<ModelListener>> _objectListeners = new LinkedHashMap<>();

	private final Map<TLStructuredType, List<ModelListener>> _typeListeners = new LinkedHashMap<>();

	@Override
	public Registration addModelListener(ModelListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Registration addModelListener(TLStructuredType type, ModelListener listener) {
		_typeListeners.computeIfAbsent(type, x -> new ArrayList<>()).add(listener);
		return Registration.onDispose(() -> removeModelListener(type, listener));
	}

	@Override
	public Registration addModelListener(TLObject object, ModelListener listener) {
		_objectListeners.computeIfAbsent(object.tId(), x -> new ArrayList<>()).add(listener);
		return Registration.onDispose(() -> removeModelListener(object, listener));
	}

	@Deprecated
	@Override
	public boolean removeModelListener(ModelListener listener) {
		return false;
	}

	@Deprecated
	@Override
	public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
		List<ModelListener> listeners = _typeListeners.get(type);
		return listeners != null && listeners.remove(listener);
	}

	@Deprecated
	@Override
	public boolean removeModelListener(TLObject object, ModelListener listener) {
		List<ModelListener> listeners = _objectListeners.get(object.tId());
		return listeners != null && listeners.remove(listener);
	}

	/**
	 * The listeners registered for the given object, one entry per registration.
	 */
	public List<ModelListener> listeners(TLObject object) {
		return List.copyOf(_objectListeners.getOrDefault(object.tId(), List.of()));
	}

	/**
	 * The listeners registered for the objects of the given type, one entry per registration.
	 */
	public List<ModelListener> typeListeners(TLStructuredType type) {
		return List.copyOf(_typeListeners.getOrDefault(type, List.of()));
	}

	/**
	 * Reports the given object as created to everybody listening for its type.
	 */
	public void reportCreation(TLObject object) {
		report(new ChangeOf(ChangeType.CREATED, object, object.tType()));
	}

	/**
	 * Reports the given object as updated to everybody listening for it.
	 */
	public void reportUpdate(TLObject object) {
		report(new ChangeOf(ChangeType.UPDATED, object, object.tType()));
	}

	/**
	 * Reports the given object as deleted to everybody listening for it.
	 */
	public void reportDeletion(TLObject object) {
		report(new ChangeOf(ChangeType.DELETED, object, null));
	}

	/**
	 * Reports the given object of the given type as deleted to everybody listening for it or for
	 * its type.
	 *
	 * <p>
	 * The type is given, since a deleted object cannot tell its type anymore.
	 * </p>
	 */
	public void reportDeletion(TLObject object, TLStructuredType type) {
		report(new ChangeOf(ChangeType.DELETED, object, type));
	}

	private void report(ChangeOf event) {
		List<ModelListener> listeners = new ArrayList<>();
		if (event.kind() != ChangeType.CREATED) {
			listeners.addAll(listeners(event.object()));
		}
		TLStructuredType objectType = event.type();
		for (Map.Entry<TLStructuredType, List<ModelListener>> entry : _typeListeners.entrySet()) {
			if (objectType != null && TLModelUtil.isGeneralization(entry.getKey(), objectType)) {
				listeners.addAll(entry.getValue());
			}
		}
		for (ModelListener listener : listeners) {
			listener.notifyChange(event);
		}
	}

	/**
	 * The change of a single object, as a {@link ModelScope} reports it.
	 *
	 * @param kind
	 *        What happened to the object.
	 * @param object
	 *        The object that changed.
	 * @param type
	 *        The type of the object, {@code null} if not known.
	 */
	private record ChangeOf(ChangeType kind, TLObject object, TLStructuredType type) implements ModelChangeEvent {

		@Override
		public ChangeType getChange(TLObject existingObject) {
			return existingObject == object ? kind : ChangeType.NONE;
		}

		@Override
		public Stream<? extends TLObject> getUpdated() {
			return objects(ChangeType.UPDATED);
		}

		@Override
		public Stream<? extends TLObject> getUpdated(TLStructuredType type) {
			return this.type == type ? getUpdated() : Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated() {
			return objects(ChangeType.CREATED);
		}

		@Override
		public Stream<? extends TLObject> getCreated(TLStructuredType type) {
			return this.type == type ? getCreated() : Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted() {
			return objects(ChangeType.DELETED);
		}

		@Override
		public Stream<? extends TLObject> getDeleted(TLStructuredType type) {
			return this.type == type ? getDeleted() : Stream.empty();
		}

		private Stream<? extends TLObject> objects(ChangeType change) {
			return kind == change ? Stream.of(object) : Stream.empty();
		}
	}

}
