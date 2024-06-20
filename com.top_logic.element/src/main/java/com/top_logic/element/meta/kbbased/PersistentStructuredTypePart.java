/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Set;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.TLTypePartCommon;
import com.top_logic.model.internal.PersistentTypePart;

/**
 * Base class for persistent implementations of {@link TLStructuredTypePart}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PersistentStructuredTypePart extends PersistentTypePart implements TLStructuredTypePart {

	/** Column name for {@link #isMandatory()} property. */
	public static final String MANDATORY = TLStructuredTypePart.MANDATORY_ATTR;

	/** Column name for {@link #getType()} reference. */
	public static final String TYPE_REF = TLStructuredTypePart.TYPE_ATTR;

	/** Column name for {@link #getDefinition()} reference. */
	public static final String DEFINITION_REF = TLStructuredTypePart.DEFINITION_ATTR;

	/**
	 * Creates a {@link PersistentStructuredTypePart}.
	 */
	public PersistentStructuredTypePart(KnowledgeObject ko) {
		super(ko);
	}

	/**
	 * The reference attribute name that points to the attribute owner.
	 */
	protected abstract String ownerRef();

	@Override
	public TLType getType() {
		return tGetDataReference(TLType.class, PersistentReference.TYPE_REF);
	}

	@Override
	public void setType(TLType value) {
		tSetDataReference(PersistentReference.TYPE_REF, value);
	}

	@Override
	public TLStructuredTypePart getDefinition() {
		KnowledgeItem definition = (KnowledgeItem) tGetData(PersistentClassProperty.DEFINITION_REF);
		return (TLStructuredTypePart) definition.getWrapper();
	}

	@Override
	public void setDefinition(TLStructuredTypePart newDefinition) {
		tSetData(DEFINITION_REF, newDefinition.tHandle());
	}

	@Override
	public void updateDefinition() {
		TLTypePartCommon.updateDefinition(this);
	}

	@Override
	public boolean isOverride() {
		return !getDefinition().equals(this);
	}

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

	@Override
	public Object getValue(String anAttribute) {
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

	@Override
	public void setValue(String aKey, Object aValue) {
		PersistentObjectImpl.setValue(this, aKey, aValue);
	}

}
