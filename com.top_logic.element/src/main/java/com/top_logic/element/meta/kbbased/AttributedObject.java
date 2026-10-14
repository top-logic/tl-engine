/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Set;

import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * A persistent {@link TLObject} whose attribute values, container and type are stored in the
 * knowledge base and accessed through {@link PersistentObjectImpl}.
 */
public interface AttributedObject extends Wrapper {

	@Override
	default TLClass tType() {
		return PersistentObjectImpl.tType(this);
	}

	@Override
	default TLObject tContainer() {
		return PersistentObjectImpl.tContainer(this);
	}

	@Override
	default TLReference tContainerReference() {
		return PersistentObjectImpl.tContainerReference(this);
	}

	/**
	 * Reads the value of the {@link #tType() type}'s part with the given name, or the raw data of
	 * the knowledge item, if the type has no such part.
	 */
	@Override
	default Object getValue(String anAttribute) {
		return PersistentObjectImpl.getValue(this, anAttribute);
	}

	/**
	 * Updates the value of the {@link #tType() type}'s part with the given name, or the raw data of
	 * the knowledge item, if the type has no such part.
	 */
	@Override
	default void setValue(String aKey, Object aValue) {
		PersistentObjectImpl.setValue(this, aKey, aValue);
	}

	@Override
	default Object tValue(TLStructuredTypePart part) {
		return PersistentObjectImpl.getValue(this, part);
	}

	@Override
	default void tUpdate(TLStructuredTypePart part, Object value) {
		PersistentObjectImpl.setValue(this, part, value);
	}

	@Override
	default void tAdd(TLStructuredTypePart part, Object value) {
		PersistentObjectImpl.addValue(this, part, value);
	}

	@Override
	default void tRemove(TLStructuredTypePart part, Object value) {
		PersistentObjectImpl.removeValue(this, part, value);
	}

	@Override
	default void generateFullText(FullTextBuBuffer buffer) {
		PersistentObjectImpl.generateFullText(buffer, this);
	}

	/**
	 * The names of the attributes of the underlying knowledge item plus the names of all parts of
	 * the {@link #tType() type}.
	 */
	@Override
	default Set<String> getAllAttributeNames() {
		Set<String> result = Wrapper.super.getAllAttributeNames();
		for (TLStructuredTypePart part : tType().getAllParts()) {
			result.add(part.getName());
		}
		return result;
	}

}
