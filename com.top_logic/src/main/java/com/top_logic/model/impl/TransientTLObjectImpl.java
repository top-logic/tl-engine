/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
		return directValue(part);
	}

	private Object directValue(TLStructuredTypePart part) {
		return _values.get(part);
	}

	@Override
	public void tUpdate(TLStructuredTypePart part, Object newValue) {
		Object oldValue = directUpdate(part, newValue);
		if (part.getModelKind() == ModelKind.REFERENCE) {
			TLAssociationEnd updatedEnd = ((TLReference) part).getEnd();
			TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(updatedEnd);
			TLReference otherRef = otherEnd.getReference();
			if (otherRef != null) {
				for (Object oldTarget : collection(oldValue)) {
					((TransientTLObjectImpl) oldTarget).directRemove(otherRef, this);
				}
				for (Object newTarget : collection(newValue)) {
					((TransientTLObjectImpl) newTarget).directAdd(otherRef, this);
				}
			}
		}
	}

	@Override
	public void tAdd(TLStructuredTypePart part, Object value) {
		if (part.getModelKind() == ModelKind.REFERENCE) {
			checkNonNull(value);
			mkCollection(part).add(value);

			TLAssociationEnd updatedEnd = ((TLReference) part).getEnd();
			TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(updatedEnd);
			TLReference otherRef = otherEnd.getReference();
			if (otherRef != null) {
				((TransientTLObjectImpl) value).directAdd(otherRef, this);
			}
		} else {
			super.tAdd(part, value);
		}
	}

	@Override
	public void tRemove(TLStructuredTypePart part, Object value) {
		if (part.getModelKind() == ModelKind.REFERENCE) {
			checkNonNull(value);
			mkCollection(part).remove(value);

			TLAssociationEnd updatedEnd = ((TLReference) part).getEnd();
			TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(updatedEnd);
			TLReference otherRef = otherEnd.getReference();
			if (otherRef != null) {
				((TransientTLObjectImpl) value).directRemove(otherRef, this);
			}
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
}
