/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.meta.kbbased.PersistentObjectImpl;
import com.top_logic.element.model.PersistentScope;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.service.db2.KnowledgeItemImpl;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * Enhances {@link StructuredElementWrapper} with an implementation of {@link MetaElementHolder}
 * interfaces.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class AttributedStructuredElementWrapper extends
		StructuredElementWrapper implements MetaElementHolder {

    /** Type of the KnowledgeObject used */
    public static final String OBJECT_NAME = "StructuredElement";
    
	/**
	 * @see KnowledgeItemImpl#createWrapper(KnowledgeItem)
	 */
	@CalledByReflection
	public AttributedStructuredElementWrapper(KnowledgeObject ko) {
        super(ko);
    }

	/**
	 * @see com.top_logic.element.meta.MetaElementHolder#addMetaElement(com.top_logic.model.TLClass)
	 */
	@Override
	public void addMetaElement(TLClass aMetaElement)
			throws IllegalArgumentException {
		PersistentObjectImpl.addMetaElement(this, aMetaElement);
	}

	/**
	 * @see com.top_logic.element.meta.MetaElementHolder#removeMetaElement(com.top_logic.model.TLClass)
	 */
	@Override
	public void removeMetaElement(TLClass aMetaElement) {
		PersistentObjectImpl.removeMetaElement(this, aMetaElement);
	}

	/**
	 * @see com.top_logic.element.meta.MetaElementHolder#getMetaElement(java.lang.String)
	 */
	@Override
	public TLClass getMetaElement(String aMetaElementType) {
		return PersistentObjectImpl.getMetaElement(this, aMetaElementType);
	}

	/**
	 * @see com.top_logic.element.meta.MetaElementHolder#getMetaElements()
	 */
	@Override
	public Set<TLClass> getMetaElements() {
		return PersistentObjectImpl.getMetaElements(this);
	}
	
	@Override
	public TLClass tType() {
		return PersistentObjectImpl.tType(this);
	}

	@Override
	public TLObject tContainer() {
		return PersistentObjectImpl.tContainer(this);
	}

	@Override
	public TLReference tContainerReference() {
		return PersistentObjectImpl.tContainerReference(this);
	}

    /**
	 * Integrates MetaAttributes. Falls back to FlexWrapper mechanism if there is no attribute of
	 * the given name.
	 * 
	 * @see com.top_logic.knowledge.wrap.Wrapper#getValue(java.lang.String)
	 */
    @Override
	public Object getValue(String anAttribute){
		return PersistentObjectImpl.getValue(this, anAttribute);
    }

	@Override
	public Object tValue(TLStructuredTypePart part) {
		return PersistentObjectImpl.getValue(this, part);
	}

    @Override
	public void generateFullText(FullTextBuBuffer buffer) {
		super.generateFullText(buffer);
		PersistentObjectImpl.generateFullText(buffer, this);
    }

    /**
	 * Integrates MetaAttributes. Falls back to FlexWrapper mechanism if there is no attribute of
	 * the given name.
	 * 
	 * @see com.top_logic.knowledge.wrap.Wrapper#setValue(java.lang.String, java.lang.Object)
	 */
    @Override
	public void setValue(String aKey, Object aValue) {
		PersistentObjectImpl.setValue(this, aKey, aValue);
    }

	@Override
	public void tAdd(TLStructuredTypePart part, Object value) {
		PersistentObjectImpl.addValue(this, part.getName(), value);
	}

	@Override
	public void tRemove(TLStructuredTypePart part, Object value) {
		PersistentObjectImpl.removeValue(this, part.getName(), value);
	}

	@Override
	public Collection<TLClass> getClasses() {
		return PersistentScope.getClasses(this);
	}

	@Override
	public Collection<TLAssociation> getAssociations() {
		return PersistentScope.getAssociations(this);
	}

	@Override
	public Collection<TLEnumeration> getEnumerations() {
		return PersistentScope.getEnumerations(this);
	}

	@Override
	public Collection<TLPrimitive> getDatatypes() {
		return PersistentScope.getDatatypes(this);
	}

	@Override
	public Collection<TLType> getTypes() {
		return PersistentScope.getTypes(this);
	}

	@Override
	public TLType getType(String name) {
		return PersistentScope.getType(this, name);
	}
    
}
