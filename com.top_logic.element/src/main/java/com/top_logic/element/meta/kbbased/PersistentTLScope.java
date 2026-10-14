/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Collection;
import java.util.Set;

import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.model.PersistentScope;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;

/**
 * A {@link MetaElementHolder} whose types are stored in the knowledge base and accessed through
 * {@link PersistentScope}.
 */
public interface PersistentTLScope extends MetaElementHolder {

	@Override
	default void addMetaElement(TLClass aMetaElement) throws IllegalArgumentException {
		PersistentObjectImpl.addMetaElement(this, aMetaElement);
	}

	@Override
	default void removeMetaElement(TLClass aMetaElement) {
		PersistentObjectImpl.removeMetaElement(this, aMetaElement);
	}

	@Override
	default TLClass getMetaElement(String aMetaElementType) {
		return PersistentObjectImpl.getMetaElement(this, aMetaElementType);
	}

	@Override
	default Set<TLClass> getMetaElements() {
		return PersistentObjectImpl.getMetaElements(this);
	}

	@Override
	default Collection<TLClass> getClasses() {
		return PersistentScope.getClasses(this);
	}

	@Override
	default Collection<TLAssociation> getAssociations() {
		return PersistentScope.getAssociations(this);
	}

	@Override
	default Collection<TLEnumeration> getEnumerations() {
		return PersistentScope.getEnumerations(this);
	}

	@Override
	default Collection<TLPrimitive> getDatatypes() {
		return PersistentScope.getDatatypes(this);
	}

	@Override
	default Collection<TLType> getTypes() {
		return PersistentScope.getTypes(this);
	}

	@Override
	default TLType getType(String name) {
		return PersistentScope.getType(this, name);
	}

}
