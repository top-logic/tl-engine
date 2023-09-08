/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.element.meta.MetaElementHolder;
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
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Class for getting wrapper with attributes, acts as a facade to a 
 * MetaElementHolderWrapperProxy.
 * 
 * @author    <a href=mailto:fma@top-logic.com>fma</a>
 */
public class AttributedWrapper extends AbstractBoundWrapper implements MetaElementHolder {

	/**
	 * @see KnowledgeItemImpl#createWrapper(KnowledgeItem)
	 */
	@CalledByReflection
    public AttributedWrapper(KnowledgeObject ko) {
        super(ko);
    }

	@Override
	public TLClass tType() {
		return PersistentObjectImpl.tType(this);
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
	
    /**
	 * The list of attribute names defined by the
	 * 
	 * @see AbstractBoundWrapper#getAllAttributeNames()
	 */
    @Override
	public Set<String> getAllAttributeNames() {
		Set<String> theResult = super.getAllAttributeNames();

		for (TLStructuredTypePart part : tType().getAllParts()) {
			theResult.add(part.getName());
		}

        return theResult;
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
