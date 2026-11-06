/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.TransientObjectFactory;

/**
 * A level of editing that holds its changes in editing buffers until they are confirmed: the form
 * itself, or a dialog opened from it, editing the parts of a composition for instance.
 *
 * <p>
 * An edited object is held in a buffer, a {@link TLObjectOverlay}: over an existing object
 * created by {@link #buffer(TLObject)}, over a new transient object created by
 * {@link #create(TLClass, TLObject)}. A buffer holds the values of its object only; a composition
 * of a buffered object holds the original parts, or buffers of the parts where they are edited
 * themselves, so editing an object costs nothing for the objects below it.
 * </p>
 *
 * <p>
 * An object has at most one buffer per level: an object a level or one of its parents already
 * buffers - a part that is a row of the same table as its container, say - is read through that
 * buffer, see {@link #current(TLObject)}, and a nested level buffers that buffer.
 * </p>
 *
 * <p>
 * A {@link #nested() nested} level - a dialog - edits on top of its parent level. When the user
 * confirms it, {@link #commit(TLObject, TLStructuredTypePart, Object)} writes the value the
 * nested level edited into a buffer of the parent level, and the buffers it refers to become
 * buffers of the parent level. When the user cancels it, the nested level is simply dropped: it
 * has written nothing anywhere else. Levels nest to any depth; the buffers of the root level are
 * stored by {@link BufferSave}.
 * </p>
 */
public class EditLevel {

	/**
	 * Observer of the buffers of an {@link EditLevel}.
	 */
	public interface Listener {

		/**
		 * Called after a nested level was {@link EditLevel#commit(TLObject, TLStructuredTypePart, Object)
		 * committed} and transferred changes into a buffer of the observed level, without the change
		 * passing a field: the parts of a dialog written into the buffer of a part that is edited as
		 * a row as well, for instance.
		 *
		 * @param buffer
		 *        The buffer of the observed level that took the changes.
		 * @param parts
		 *        The attributes whose values were transferred.
		 */
		void onBufferChanged(TLObject buffer, Collection<TLStructuredTypePart> parts);

	}

	private final EditLevel _parent;

	/** The buffers belonging to this level, compared by identity. */
	private final Set<TLObject> _buffers = Collections.newSetFromMap(new IdentityHashMap<>());

	/** The overlays of this level by the object they buffer, compared by identity. */
	private final Map<TLObject, TLObjectOverlay> _overlayByBase = new IdentityHashMap<>();

	private final List<Listener> _listeners = new ArrayList<>();

	/**
	 * Creates a root {@link EditLevel}, whose buffers are stored by {@link BufferSave}.
	 */
	public EditLevel() {
		this(null);
	}

	private EditLevel(EditLevel parent) {
		_parent = parent;
	}

	/**
	 * Creates a level editing on top of this one, whose changes reach this level only when they
	 * are {@link #commit(TLObject, TLStructuredTypePart, Object) committed}.
	 */
	public EditLevel nested() {
		return new EditLevel(this);
	}

	/**
	 * Whether the given object is a buffer of this level.
	 */
	public boolean owns(Object object) {
		return object instanceof TLObject buffer && _buffers.contains(buffer);
	}

	/**
	 * Adds a {@link Listener} to the buffers of this level.
	 */
	public void addListener(Listener listener) {
		_listeners.add(listener);
	}

	/**
	 * Removes a {@link Listener} added by {@link #addListener(Listener)}.
	 */
	public void removeListener(Listener listener) {
		_listeners.remove(listener);
	}

	/**
	 * The object the given object is edited through on this level: the innermost buffer of this
	 * level or of a parent level that buffers it, the object itself if none does.
	 *
	 * <p>
	 * A value of a buffer holds the original of an object that is edited itself, as long as the
	 * value was not set: the composition of a container lists its stored parts, also when the
	 * parts are rows of the same table. Reading the value through this method shows the parts as
	 * they are edited.
	 * </p>
	 *
	 * @param object
	 *        An object, an original or a buffer.
	 * @return The buffer through which the object is edited on this level, or the object itself.
	 */
	public TLObject current(TLObject object) {
		TLObject outer = _parent == null ? object : _parent.current(object);
		TLObjectOverlay buffer = _overlayByBase.get(outer);
		return buffer != null ? buffer : outer;
	}

	/**
	 * The given value with each object replaced by {@link #current(TLObject)}.
	 *
	 * @param value
	 *        A value: an object, a collection of objects, or anything else, which is returned as
	 *        it is.
	 * @return The value itself where no object in it is buffered.
	 */
	public Object current(Object value) {
		if (value instanceof TLObject object) {
			return current(object);
		}
		if (!(value instanceof Collection<?> collection)) {
			return value;
		}
		List<Object> elements = new ArrayList<>(collection.size());
		boolean changed = false;
		for (Object element : collection) {
			Object resolved = current(element);
			changed |= resolved != element;
			elements.add(resolved);
		}
		if (!changed) {
			return collection;
		}
		return collection instanceof Set<?> ? new LinkedHashSet<>(elements) : elements;
	}

	/**
	 * The buffer holding the changes this level makes to the given object.
	 *
	 * <p>
	 * A buffer of this level is returned as it is, so is the buffer this level already holds for
	 * the object. Any other object gets a {@link TLObjectOverlay} of this level over the object it
	 * is {@link #current(TLObject) edited through} - an original, or a buffer of an outer level -
	 * so the changes made here do not reach it before they are committed.
	 * </p>
	 *
	 * @param object
	 *        The object to edit.
	 * @return The buffer to read and write the values of the object through.
	 */
	public TLObject buffer(TLObject object) {
		TLObject current = current(object);
		if (owns(current)) {
			return current;
		}
		TLObjectOverlay result = new TLObjectOverlay(current);
		add(result);
		return result;
	}

	private void add(TLObject buffer) {
		_buffers.add(buffer);
		if (buffer instanceof TLObjectOverlay overlay) {
			_overlayByBase.put(overlay.getBase(), overlay);
		}
	}

	private boolean remove(TLObject buffer) {
		if (!_buffers.remove(buffer)) {
			return false;
		}
		if (buffer instanceof TLObjectOverlay overlay && _overlayByBase.get(overlay.getBase()) == overlay) {
			_overlayByBase.remove(overlay.getBase());
		}
		return true;
	}

	/**
	 * Creates a new object of this level: a transient object, which becomes persistent when the
	 * root level is stored, edited through a buffer of this level like an existing object.
	 *
	 * <p>
	 * The buffer is an overlay over the new object, so that a change of the new object is
	 * announced like the change of an existing one, see
	 * {@link TLObjectOverlay#addValueChangeListener(TLObjectOverlay.ValueChangeListener)}. Only the
	 * buffer belongs to this level; the new object is reached through it.
	 * </p>
	 *
	 * <p>
	 * The buffer is no {@link TLObjectOverlay#isCreate() create overlay}: like the buffer of an
	 * existing object, it stands for the object it is created over. A check deciding about the
	 * object it is given - an access right, the approval of an operation - therefore sees the new
	 * transient object, see {@link com.top_logic.model.TLFormObjectBase#editedObject(TLObject)}.
	 * </p>
	 *
	 * @param type
	 *        The type of the new object.
	 * @param container
	 *        The object the new object is created within (e.g. the buffer of the object whose
	 *        composition it becomes a part of), deciding its defaults; {@code null} for none.
	 * @return The buffer of the new object, whose {@link TLObjectOverlay#getBase() base} is the
	 *         new object.
	 */
	public TLObjectOverlay create(TLClass type, TLObject container) {
		TLObject newObject = TransientObjectFactory.INSTANCE.createObject(type, container);
		TLObjectOverlay result = new TLObjectOverlay(newObject);
		add(result);
		return result;
	}

	/**
	 * Whether the given buffer edits a new object, one that is not stored yet: a transient
	 * object, or an overlay directly over one, see {@link #create(TLClass, TLObject)}.
	 */
	public static boolean isNew(TLObject buffer) {
		TLObject object = buffer instanceof TLObjectOverlay overlay ? overlay.getBase() : buffer;
		return isNewObject(object);
	}

	private static boolean isNewObject(TLObject object) {
		return object != null && !(object instanceof TLObjectOverlay) && object.tTransient();
	}

	/**
	 * Confirms this level: writes the given value into a buffer of the parent level.
	 *
	 * <p>
	 * The value is the one this level edited, a list of parts of a composition for instance, which
	 * holds originals and buffers of this level. Each buffer of this level that edits a buffer of
	 * the parent level is transferred into that buffer, which then stands in the value; every other
	 * buffer of this level reachable from the value becomes a buffer of the parent level. Nothing
	 * else is visited, so the cost is that of the objects this level touched. The
	 * {@link #addListener(Listener) listeners} of the parent level learn about the buffers of the
	 * parent level that took changes, the target excluded.
	 * </p>
	 *
	 * @param target
	 *        The buffer of the parent level receiving the value.
	 * @param part
	 *        The attribute of the target receiving the value.
	 * @param value
	 *        The edited value.
	 */
	public void commit(TLObject target, TLStructuredTypePart part, Object value) {
		if (_parent == null) {
			throw new IllegalStateException("A root level has no parent to commit to.");
		}
		Map<TLObject, Collection<TLStructuredTypePart>> changed = new IdentityHashMap<>();
		target.tUpdate(part, transfer(value, new IdentityHashMap<>(), changed));
		changed.remove(target);
		for (Map.Entry<TLObject, Collection<TLStructuredTypePart>> entry : changed.entrySet()) {
			for (Listener listener : new ArrayList<>(_parent._listeners)) {
				listener.onBufferChanged(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * The given value as it belongs to the parent level, with the buffers of this level reachable
	 * from it transferred to the parent level.
	 *
	 * @param done
	 *        The buffers already transferred, with what stands for them in the parent level.
	 * @param changed
	 *        Collects the buffers of the parent level that took changes, with the attributes
	 *        changed.
	 * @return The value itself where it holds no buffer of this level.
	 */
	private Object transfer(Object value, Map<TLObject, TLObject> done,
			Map<TLObject, Collection<TLStructuredTypePart>> changed) {
		if (value instanceof Collection<?> collection) {
			return transferAll(collection, done, changed);
		}
		if (!(value instanceof TLObject object)) {
			return value;
		}
		TLObject transferred = done.get(object);
		if (transferred != null) {
			return transferred;
		}
		if (!remove(object)) {
			// An original, or a buffer of an outer level: it already belongs there.
			return value;
		}
		done.put(object, object);
		transferValues(object, done, changed);
		if (object instanceof TLObjectOverlay overlay && _parent.owns(overlay.getBase())) {
			// The parent level buffers the object itself: its buffer takes the changes.
			TLObject base = overlay.getBase();
			List<TLStructuredTypePart> parts = overlay.getChangedParts();
			if (!parts.isEmpty()) {
				changed.computeIfAbsent(base, x -> new LinkedHashSet<>()).addAll(parts);
			}
			overlay.applyToBuffer();
			done.put(object, base);
			return base;
		}
		_parent.add(object);
		return object;
	}

	private Object transferAll(Collection<?> collection, Map<TLObject, TLObject> done,
			Map<TLObject, Collection<TLStructuredTypePart>> changed) {
		List<Object> elements = new ArrayList<>(collection.size());
		boolean modified = false;
		for (Object element : collection) {
			Object transferred = transfer(element, done, changed);
			modified |= transferred != element;
			elements.add(transferred);
		}
		if (!modified) {
			return collection;
		}
		return collection instanceof Set<?> ? new LinkedHashSet<>(elements) : elements;
	}

	/**
	 * Transfers the buffers of this level held in the values of the given buffer: the values an
	 * overlay buffers, all values of a new object.
	 */
	private void transferValues(TLObject buffer, Map<TLObject, TLObject> done,
			Map<TLObject, Collection<TLStructuredTypePart>> changed) {
		for (TLStructuredTypePart part : bufferedParts(buffer)) {
			Object value = buffer.tValue(part);
			Object transferred = transfer(value, done, changed);
			if (transferred != value) {
				buffer.tUpdate(part, transferred);
			}
		}
	}

	/**
	 * Whether the given value holds a change: a new object, or an overlay that modifies an
	 * attribute of its object or holds such a change in a composition.
	 *
	 * <p>
	 * Only the buffers in the value and in their compositions are visited.
	 * </p>
	 */
	public static boolean isModified(Object value) {
		if (value instanceof Collection<?> collection) {
			for (Object element : collection) {
				if (isModified(element)) {
					return true;
				}
			}
			return false;
		}
		if (value instanceof TLObjectOverlay overlay) {
			if (isNewObject(overlay.getBase())) {
				// The buffer of a new object.
				return true;
			}
			for (TLStructuredTypePart part : overlay.getChangedParts()) {
				if (overlay.isModified(part)) {
					return true;
				}
				if (isComposition(part) && isModified(overlay.tValue(part))) {
					return true;
				}
			}
			return false;
		}
		return value instanceof TLObject object && object.tTransient();
	}

	/**
	 * Transfers the changes of all overlays in the given value into their bases, where the bases
	 * are new (transient) objects: the value then holds the new objects themselves, with all their
	 * changes, as a new object that is made persistent as a whole expects.
	 *
	 * <p>
	 * Only the buffers in the value and in their values are visited. An overlay of a persistent
	 * object stays as it is.
	 * </p>
	 *
	 * @return The value with the overlays of new objects replaced by the new objects.
	 */
	public static Object applyToNewObjects(Object value) {
		if (value instanceof Collection<?> collection) {
			List<Object> elements = new ArrayList<>(collection.size());
			boolean changed = false;
			for (Object element : collection) {
				Object applied = applyToNewObjects(element);
				changed |= applied != element;
				elements.add(applied);
			}
			if (!changed) {
				return collection;
			}
			return collection instanceof Set<?> ? new LinkedHashSet<>(elements) : elements;
		}
		if (!(value instanceof TLObject object) || !object.tTransient()) {
			// No buffer, or an overlay of a persistent object.
			return value;
		}
		for (TLStructuredTypePart part : bufferedParts(object)) {
			Object partValue = object.tValue(part);
			Object applied = applyToNewObjects(partValue);
			if (applied != partValue) {
				object.tUpdate(part, applied);
			}
		}
		if (object instanceof TLObjectOverlay overlay) {
			overlay.applyToBuffer();
			return applyToNewObjects(overlay.getBase());
		}
		return object;
	}

	private static boolean isComposition(TLStructuredTypePart part) {
		return part instanceof TLReference reference && reference.isComposite();
	}

	/**
	 * The attributes holding the values of the given buffer: the ones an overlay holds a value for,
	 * the stored attributes of a new object.
	 */
	static List<TLStructuredTypePart> bufferedParts(TLObject buffer) {
		if (buffer instanceof TLObjectOverlay overlay) {
			return overlay.getChangedParts();
		}
		List<TLStructuredTypePart> result = new ArrayList<>();
		for (TLStructuredTypePart part : buffer.tType().getAllParts()) {
			if (part.isDerived() || part.isAbstract()) {
				continue;
			}
			if (part instanceof TLReference reference && reference.isBackwards()) {
				continue;
			}
			result.add(part);
		}
		return result;
	}

}
