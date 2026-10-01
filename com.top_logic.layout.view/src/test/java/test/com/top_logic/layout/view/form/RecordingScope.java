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

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;

/**
 * {@link ModelScope} a test delivers object changes through, standing in for the scope of a
 * browser window.
 *
 * <p>
 * Only listeners for single objects are supported. The registrations are recorded as made, so that
 * a test can check that a listener is registered exactly once.
 * </p>
 */
public class RecordingScope implements ModelScope {

	private final Map<ObjectKey, List<ModelListener>> _objectListeners = new LinkedHashMap<>();

	@Override
	public boolean addModelListener(ModelListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addModelListener(TLStructuredType type, ModelListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addModelListener(TLObject object, ModelListener listener) {
		return _objectListeners.computeIfAbsent(object.tId(), x -> new ArrayList<>()).add(listener);
	}

	@Override
	public boolean removeModelListener(ModelListener listener) {
		return false;
	}

	@Override
	public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
		return false;
	}

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
	 * Reports the given object as updated to everybody listening for it.
	 */
	public void reportUpdate(TLObject object) {
		ModelChangeEvent event = new UpdateOf(object);
		for (ModelListener listener : listeners(object)) {
			listener.notifyChange(event);
		}
	}

	/**
	 * The change of a single object, as a {@link ModelScope} reports it.
	 *
	 * @param object
	 *        The object that was updated.
	 */
	private record UpdateOf(TLObject object) implements ModelChangeEvent {

		@Override
		public ChangeType getChange(TLObject existingObject) {
			return existingObject == object ? ChangeType.UPDATED : ChangeType.NONE;
		}

		@Override
		public Stream<? extends TLObject> getUpdated() {
			return Stream.of(object);
		}

		@Override
		public Stream<? extends TLObject> getUpdated(TLStructuredType type) {
			return object.tType() == type ? getUpdated() : Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated(TLStructuredType type) {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted(TLStructuredType type) {
			return Stream.empty();
		}
	}

}
