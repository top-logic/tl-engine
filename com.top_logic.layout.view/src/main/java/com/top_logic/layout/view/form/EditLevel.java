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
 * An edited object is held in a buffer: an existing object in a {@link TLObjectOverlay} created
 * by {@link #buffer(TLObject)}, a new object as a transient object created by
 * {@link #create(TLClass, TLObject)}. A buffer holds the values of its object only; a composition
 * of a buffered object holds the original parts, or buffers of the parts where they are edited
 * themselves, so editing an object costs nothing for the objects below it.
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

	private final EditLevel _parent;

	/** The buffers belonging to this level, compared by identity. */
	private final Set<TLObject> _buffers = Collections.newSetFromMap(new IdentityHashMap<>());

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
	 * The level this level edits on top of, {@code null} for a root level.
	 */
	public EditLevel getParent() {
		return _parent;
	}

	/**
	 * Whether the given object is a buffer of this level.
	 */
	public boolean owns(Object object) {
		return object instanceof TLObject buffer && _buffers.contains(buffer);
	}

	/**
	 * The buffer holding the changes this level makes to the given object.
	 *
	 * <p>
	 * A buffer of this level is returned as it is. Any other object - an original, or a buffer of
	 * an outer level - gets a {@link TLObjectOverlay} of this level, so the changes made here do
	 * not reach it before they are committed.
	 * </p>
	 *
	 * @param object
	 *        The object to edit.
	 * @return The buffer to read and write the values of the object through.
	 */
	public TLObject buffer(TLObject object) {
		if (owns(object)) {
			return object;
		}
		TLObjectOverlay result = new TLObjectOverlay(object);
		_buffers.add(result);
		return result;
	}

	/**
	 * Creates a new object of this level: a transient object, which becomes persistent when the
	 * root level is stored.
	 *
	 * @param type
	 *        The type of the new object.
	 * @param container
	 *        The object the new object is created within (e.g. the buffer of the object whose
	 *        composition it becomes a part of), deciding its defaults; {@code null} for none.
	 * @return The new object.
	 */
	public TLObject create(TLClass type, TLObject container) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(type, container);
		_buffers.add(result);
		return result;
	}

	/**
	 * Confirms this level: writes the given value into a buffer of the parent level.
	 *
	 * <p>
	 * The value is the one this level edited, a list of parts of a composition for instance, which
	 * holds originals and buffers of this level. Each buffer of this level that edits a buffer of
	 * the parent level is transferred into that buffer, which then stands in the value; every other
	 * buffer of this level reachable from the value becomes a buffer of the parent level. Nothing
	 * else is visited, so the cost is that of the objects this level touched.
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
		target.tUpdate(part, transfer(value, new IdentityHashMap<>()));
	}

	/**
	 * The given value as it belongs to the parent level, with the buffers of this level reachable
	 * from it transferred to the parent level.
	 *
	 * @param done
	 *        The buffers already transferred, with what stands for them in the parent level.
	 * @return The value itself where it holds no buffer of this level.
	 */
	private Object transfer(Object value, Map<TLObject, TLObject> done) {
		if (value instanceof Collection<?> collection) {
			return transferAll(collection, done);
		}
		if (!(value instanceof TLObject object)) {
			return value;
		}
		TLObject transferred = done.get(object);
		if (transferred != null) {
			return transferred;
		}
		if (!_buffers.remove(object)) {
			// An original, or a buffer of an outer level: it already belongs there.
			return value;
		}
		done.put(object, object);
		transferValues(object, done);
		if (object instanceof TLObjectOverlay overlay && _parent.owns(overlay.getBase())) {
			// The parent level buffers the object itself: its buffer takes the changes.
			overlay.applyToBuffer();
			done.put(object, overlay.getBase());
			return overlay.getBase();
		}
		_parent._buffers.add(object);
		return object;
	}

	private Object transferAll(Collection<?> collection, Map<TLObject, TLObject> done) {
		List<Object> elements = new ArrayList<>(collection.size());
		boolean changed = false;
		for (Object element : collection) {
			Object transferred = transfer(element, done);
			changed |= transferred != element;
			elements.add(transferred);
		}
		if (!changed) {
			return collection;
		}
		return collection instanceof Set<?> ? new LinkedHashSet<>(elements) : elements;
	}

	/**
	 * Transfers the buffers of this level held in the values of the given buffer: the values an
	 * overlay buffers, all values of a new object.
	 */
	private void transferValues(TLObject buffer, Map<TLObject, TLObject> done) {
		for (TLStructuredTypePart part : bufferedParts(buffer)) {
			Object value = buffer.tValue(part);
			Object transferred = transfer(value, done);
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
