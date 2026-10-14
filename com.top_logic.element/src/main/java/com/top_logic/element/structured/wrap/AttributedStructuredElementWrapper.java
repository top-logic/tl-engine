/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.meta.kbbased.AttributedObject;
import com.top_logic.element.meta.kbbased.PersistentObjectImpl;
import com.top_logic.element.meta.kbbased.PersistentTLScope;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.service.db2.KnowledgeItemImpl;

/**
 * Enhances {@link StructuredElementWrapper} with an implementation of {@link MetaElementHolder}
 * interfaces.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class AttributedStructuredElementWrapper extends
		StructuredElementWrapper implements AttributedObject, PersistentTLScope {

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
	 * Indexes the full text of {@link StructuredElementWrapper} and the attribute values.
	 */
	@Override
	public void generateFullText(FullTextBuBuffer buffer) {
		super.generateFullText(buffer);
		PersistentObjectImpl.generateFullText(buffer, this);
	}

}
