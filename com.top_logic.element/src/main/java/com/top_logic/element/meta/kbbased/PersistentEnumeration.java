/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Set;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FastList} extension fetching its model properties from the dynamic model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersistentEnumeration extends FastList {

	/**
	 * This constructor creates a new {@link PersistentClassifier}.
	 */
	public PersistentEnumeration(KnowledgeObject ko) {
		super(ko);
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

