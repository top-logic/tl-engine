/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;
import com.top_logic.model.util.TLModelUtil;

/**
 * Transient {@link TLObject} implementation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransientTLObjectImpl extends TransientObject {

	private static final MOClass TRANSIENT = new MOClassImpl("transient");

	private final TLStructuredType _type;

	private Map<TLStructuredTypePart, Object> _values = new HashMap<>();

	/**
	 * Creates a {@link TransientTLObjectImpl}.
	 * 
	 * @see TransientObjectFactory
	 */
	TransientTLObjectImpl(TLStructuredType type) {
		_type = type;
	}

	@Override
	public TLStructuredType tType() {
		return _type;
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		Object directValue = directValue(part);
		if (directValue == null) {
			// Value may not be set yet
			if (part.isMultiple()) {
				if (part.isOrdered()) {
					return Collections.emptyList();
				} else {
					return Collections.emptySet();
				}
			}
		}
		return directValue;
	}

	private Object directValue(TLStructuredTypePart part) {
		if (part.isDerived() && (part.getModelKind() != ModelKind.REFERENCE || !((TLReference) part).isBackwards())) {
			return part.getStorageImplementation().getAttributeValue(this, part);
		}
		return _values.get(part);
	}

	@Override
	public void tUpdate(TLStructuredTypePart part, Object newValue) {
		checkDerived(part);
		newValue = ensureMultiplicity(part, newValue);
		Object oldValue = directUpdate(part, newValue);
		if (part.getModelKind() == ModelKind.REFERENCE) {
			TLAssociationEnd updatedEnd = ((TLReference) part).getEnd();
			TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(updatedEnd);
			TLReference otherRef = otherEnd.getReference();
			if (otherRef != null) {
				for (Object oldTarget : collection(oldValue)) {
					// Note: Non-transient objects may have been assigned to transient ones (the
					// other way around is not possible).
					if (oldTarget instanceof TransientTLObjectImpl) {
						((TransientTLObjectImpl) oldTarget).directRemove(otherRef, this);
					}
				}
				for (Object newTarget : collection(newValue)) {
					// Note: Non-transient objects may have been assigned to transient ones (the
					// other way around is not possible).
					if (newTarget instanceof TransientTLObjectImpl) {
						((TransientTLObjectImpl) newTarget).directAdd(otherRef, this);
					}
				}
			}
		}
	}

	private Object ensureMultiplicity(TLStructuredTypePart part, Object newValue) {
		if (part.isMultiple()) {
			Collection<Object> result;
			if (part.isOrdered()) {
				if (newValue instanceof List<?>) {
					return newValue;
				}
				// Create mutable collection to be able to support tAdd and tRemove.
				result = new ArrayList<>();
			} else {
				if (newValue instanceof Set<?>) {
					return newValue;
				}
				// Create mutable collection to be able to support tAdd and tRemove.
				result = new HashSet<>();
			}
			if (newValue instanceof Collection<?>) {
				result.addAll((Collection<?>) newValue);
			} else if (newValue == null) {
				// Nothing to add.
			} else {
				throw new IllegalArgumentException();
			}
			return result;
		} else {
			return CollectionUtilShared.getSingleValueFrom(newValue);
		}
	}

	@Override
	public void tAdd(TLStructuredTypePart part, Object value) {
		checkDerived(part);
		if (part.getModelKind() == ModelKind.REFERENCE) {
			checkNonNull(value);
			mkCollection(part).add(value);

			// Note: Non-transient objects may have been assigned to transient ones (the
			// other way around is not possible).
			if (value instanceof TransientTLObjectImpl) {
				TLAssociationEnd updatedEnd = ((TLReference) part).getEnd();
				TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(updatedEnd);
				TLReference otherRef = otherEnd.getReference();
				if (otherRef != null) {
					((TransientTLObjectImpl) value).directAdd(otherRef, this);
				}
			}
		} else {
			super.tAdd(part, value);
		}
	}

	@Override
	public void tRemove(TLStructuredTypePart part, Object value) {
		checkDerived(part);
		if (part.getModelKind() == ModelKind.REFERENCE) {
			checkNonNull(value);
			mkCollection(part).remove(value);

			// Note: Non-transient objects may have been assigned to transient ones (the
			// other way around is not possible).
			if (value instanceof TransientTLObjectImpl) {
				TLAssociationEnd updatedEnd = ((TLReference) part).getEnd();
				TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(updatedEnd);
				TLReference otherRef = otherEnd.getReference();
				if (otherRef != null) {
					((TransientTLObjectImpl) value).directRemove(otherRef, this);
				}
			}
		} else {
			super.tRemove(part, value);
		}
	}

	private void checkNonNull(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Collection elements cannot be null.");
		}
	}

	private Collection<Object> mkCollection(TLStructuredTypePart part) {
		@SuppressWarnings("unchecked")
		Collection<Object> collection = (Collection<Object>) directValue(part);
		if (collection == null) {
			collection = createCollection((TLReference) part);
			directUpdate(part, collection);
		}
		return collection;
	}

	private Object directUpdate(TLStructuredTypePart part, Object newValue) {
		return _values.put(part, newValue);
	}

	private void directAdd(TLReference ref, TransientTLObjectImpl other) {
		Object oldValue = directValue(ref);
		if (ref.isMultiple()) {
			if (oldValue != null) {
				@SuppressWarnings("unchecked")
				Collection<Object> oldCollection = (Collection<Object>) oldValue;
				oldCollection.add(other);
			} else {
				Collection<Object> newCollection;
				newCollection = createCollection(ref);
				newCollection.add(other);
				directUpdate(ref, newCollection);
			}
		} else {
			assert oldValue == null : "Must only add to a null singleton reference '" + ref + "', was: " + oldValue;
			directUpdate(ref, other);
		}
	}

	private Collection<Object> createCollection(TLReference ref) {
		Collection<Object> newCollection;
		if (ref.isOrdered()) {
			newCollection = new ArrayList<>();
		} else {
			newCollection = new HashSet<>();
		}
		return newCollection;
	}

	private void directRemove(TLReference ref, TransientTLObjectImpl other) {
		Object oldValue = directValue(ref);
		if (ref.isMultiple()) {
			boolean success = ((Collection<?>) oldValue).remove(other);
			assert success : "Value '" + other + "' was not found in multiple reference '" + ref + "', values: "
				+ oldValue;
		} else {
			assert oldValue == other : "Removed value was not stored in singleton reference.";
			directUpdate(ref, null);
		}
	}

	private static Collection<?> collection(Object value) {
		if (value instanceof Collection<?>) {
			return (Collection<?>) value;
		} else {
			return CollectionUtilShared.singletonOrEmptyList(value);
		}
	}
	
	private static void checkDerived(TLStructuredTypePart part) {
		if (part.isDerived()) {
			throw new UnsupportedOperationException("Cannot modify derived attribute: " + part);
		}
	}

	@Override
	public Object tSetData(String property, Object value) {
		Object oldValue = tGetData(property);
		tUpdateByName(property, value);
		return oldValue;
	}

	@Override
	public Object tGetData(String property) {
		return tValueByName(property);
	}

	@Override
	public Object tValueByName(String partName) {
		return tValue(tType().getPartOrFail(partName));
	}

	@Override
	public Revision tRevision() {
		return Revision.CURRENT;
	}

	@Override
	public MOStructure tTable() {
		return TRANSIENT;
	}

	/**
	 * {@link IllegalArgumentException} to throw when for a {@link TLStructuredTypePart#isMultiple()
	 * multiple} neither a {@link Collection} nor <code>null</code> is given.
	 */
	public static IllegalArgumentException errorNoCollection() {
		return new IllegalArgumentException("Value must be a collection.");
	}

}
