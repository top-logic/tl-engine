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
import java.util.Objects;
import java.util.Set;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.copy.I18NConstants;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Stores the editing buffers of a root {@link EditLevel}.
 *
 * <p>
 * Starting at the buffers to store, the store follows the compositions the buffers hold values
 * for, and nothing else: an overlay writes the attributes whose values it changed into its base
 * object, a new (transient) object is created once as persistent object with its values, and a
 * part taken out of a composition is deleted. An object no buffer edits is neither visited nor
 * written, so the cost is that of the touched objects.
 * </p>
 *
 * <p>
 * Every value written is resolved: an overlay stands for the object it edits, a new object for the
 * persistent object created for it - also in a reference that is no composition.
 * </p>
 *
 * <p>
 * The current user needs the right to write each changed attribute of an existing object and the
 * right to create each new object in the context it is created in; the values of a new object are
 * not checked attribute by attribute, they were entered through fields following the rights of
 * the attributes. A refusal of a write is reported before anything is written, a refusal of a
 * creation aborts the store; either way the enclosing transaction is to be rolled back. The store must run within a transaction; the buffers themselves are left
 * as they are.
 * </p>
 */
public class BufferSave {

	/** The overlays reached, in the order they were reached. */
	private final List<TLObjectOverlay> _overlays = new ArrayList<>();

	/** The new objects reached, in the order they were reached. */
	private final List<TLObject> _newObjects = new ArrayList<>();

	/** The persistent object created for each new object reached. */
	private final Map<TLObject, TLObject> _created = new IdentityHashMap<>();

	private final Set<TLObject> _visited = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * Stores the given buffer and the buffers reachable from it through compositions.
	 *
	 * @param buffer
	 *        A buffer of a root level, or an original, which is stored as it is.
	 * @return The persistent object the buffer stands for.
	 */
	public static TLObject save(TLObject buffer) {
		return save(List.of(buffer)).get(0);
	}

	/**
	 * Stores the given buffers and the buffers reachable from them through compositions.
	 *
	 * @param buffers
	 *        Buffers of a root level, or originals, which are stored as they are.
	 * @return The persistent objects the buffers stand for, in the same order.
	 */
	public static List<TLObject> save(List<? extends TLObject> buffers) {
		return save(buffers, null, null);
	}

	/**
	 * Stores the given buffers, which a container holds in a reference, and the buffers reachable
	 * from them through compositions.
	 *
	 * @param buffers
	 *        Buffers of a root level, or originals, which are stored as they are.
	 * @param container
	 *        The persistent object holding the buffers, the context new objects among them are
	 *        created in; {@code null} for none.
	 * @param reference
	 *        The reference of the container holding the buffers, {@code null} for none. The value
	 *        of the reference itself is not written.
	 * @return The persistent objects the buffers stand for, in the same order.
	 */
	public static List<TLObject> save(List<? extends TLObject> buffers, TLObject container, TLReference reference) {
		BufferSave save = new BufferSave();
		for (TLObject buffer : buffers) {
			save.visit(buffer, container, reference);
		}
		save.checkWrite();
		save.write();
		List<TLObject> result = new ArrayList<>(buffers.size());
		for (TLObject buffer : buffers) {
			result.add((TLObject) save.resolve(buffer));
		}
		return result;
	}

	private BufferSave() {
		// Created by save().
	}

	/**
	 * Collects the given object, when it is a buffer, and the buffers reachable from it through
	 * compositions; creates the persistent object of a new object.
	 *
	 * @param container
	 *        The buffer holding the object in the given composition, {@code null} for a root.
	 * @param reference
	 *        The composition holding the object, {@code null} for a root.
	 */
	private void visit(Object object, TLObject container, TLReference reference) {
		if (!(object instanceof TLObject buffer) || !_visited.add(buffer)) {
			return;
		}
		if (buffer instanceof TLObjectOverlay overlay) {
			// An overlay of a buffer stores into what its base is stored as.
			visit(overlay.getBase(), container, reference);
			_overlays.add(overlay);
		} else if (buffer.tTransient()) {
			_created.put(buffer, create(buffer, container, reference));
			_newObjects.add(buffer);
		} else {
			// An original: nothing below it is edited.
			return;
		}
		for (TLStructuredTypePart part : EditLevel.bufferedParts(buffer)) {
			if (part instanceof TLReference composition && composition.isComposite()) {
				Object value = buffer.tValue(part);
				if (value instanceof Collection<?> parts) {
					for (Object element : parts) {
						visit(element, buffer, composition);
					}
				} else {
					visit(value, buffer, composition);
				}
			}
		}
	}

	/**
	 * Creates the persistent object of the given new object, in the context of the object its
	 * container is stored as.
	 */
	private TLObject create(TLObject newObject, TLObject container, TLReference reference) {
		TLClass type = (TLClass) newObject.tType();
		TLObject context = container == null ? null : (TLObject) resolve(container);
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		boolean allowed = reference == null
			? rights.isAllowedCreate(user, type, context)
			: rights.isAllowedCreate(user, context, reference, type);
		if (!allowed) {
			throw new TopLogicException(I18NConstants.ERROR_PERSIST_PERMISSION_DENIED__TYPE.fill(type));
		}
		return DynamicModelService.getFactoryFor(type.getModule().getName()).createObject(type, context);
	}

	/**
	 * Ensures that the current user may write every attribute an overlay modifies.
	 */
	private void checkWrite() {
		for (TLObjectOverlay overlay : _overlays) {
			TLObject target = (TLObject) resolve(overlay);
			if (_created.containsValue(target)) {
				// The values of a new object are not checked attribute by attribute.
				continue;
			}
			for (TLStructuredTypePart part : overlay.getChangedParts()) {
				if (overlay.isModified(part)) {
					Update.checkWritePermission(target, part);
				}
			}
		}
	}

	/**
	 * Writes the values of all collected buffers and deletes the parts taken out of compositions.
	 */
	private void write() {
		for (TLObject newObject : _newObjects) {
			TLObject target = _created.get(newObject);
			for (TLStructuredTypePart part : EditLevel.bufferedParts(newObject)) {
				if (isComputedInTransaction(part)) {
					// The persistent object keeps the default computed when it was created.
					continue;
				}
				Object value = resolve(newObject.tValue(part));
				if (!Objects.equals(target.tValue(part), value)) {
					target.tUpdate(part, value);
				}
			}
		}
		// Overlays of new objects write their changes over the values of the new objects.
		List<TLObject> removed = new ArrayList<>();
		for (TLObjectOverlay overlay : _overlays) {
			TLObject target = (TLObject) resolve(overlay);
			for (TLStructuredTypePart part : overlay.getChangedParts()) {
				if (!overlay.isModified(part)) {
					continue;
				}
				Object value = resolve(overlay.tValue(part));
				if (part instanceof TLReference reference && reference.isComposite()) {
					collectRemoved(target.tValue(part), value, removed);
				}
				target.tUpdate(part, value);
			}
		}
		for (TLObject part : removed) {
			if (part.tValid()) {
				part.tDelete();
			}
		}
	}

	/**
	 * Collects the parts of the old value of a composition that the new value does not hold.
	 */
	private static void collectRemoved(Object oldValue, Object newValue, List<TLObject> removed) {
		Set<Object> kept = Collections.newSetFromMap(new IdentityHashMap<>());
		kept.addAll(asCollection(newValue));
		for (Object part : asCollection(oldValue)) {
			if (part instanceof TLObject object && !kept.contains(object)) {
				removed.add(object);
			}
		}
	}

	private static Collection<?> asCollection(Object value) {
		if (value instanceof Collection<?> collection) {
			return collection;
		}
		return value == null ? List.of() : List.of(value);
	}

	/**
	 * The given value with each buffer replaced by the persistent object it stands for.
	 */
	private Object resolve(Object value) {
		if (value instanceof Collection<?> collection) {
			List<Object> result = new ArrayList<>(collection.size());
			for (Object element : collection) {
				result.add(resolve(element));
			}
			return collection instanceof Set<?> ? new LinkedHashSet<>(result) : result;
		}
		if (value instanceof TLObjectOverlay overlay) {
			return resolve(overlay.getBase());
		}
		if (value instanceof TLObject object && object.tTransient()) {
			TLObject created = _created.get(object);
			if (created == null) {
				throw new IllegalStateException(
					"Reference to a new object that is not part of a stored composition: " + object);
			}
			return created;
		}
		return value;
	}

	private static boolean isComputedInTransaction(TLStructuredTypePart part) {
		DefaultProvider provider = DisplayAnnotations.getDefaultProvider(part);
		return provider != null && provider.isComputedInTransaction();
	}

}
