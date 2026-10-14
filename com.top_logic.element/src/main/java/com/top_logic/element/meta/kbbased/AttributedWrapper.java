/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.db2.KnowledgeItemImpl;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Class for getting wrapper with attributes, acts as a facade to a 
 * MetaElementHolderWrapperProxy.
 * 
 * @author    <a href=mailto:fma@top-logic.com>fma</a>
 */
public class AttributedWrapper extends AbstractBoundWrapper implements AttributedObject, PersistentTLScope {

	/**
	 * @see KnowledgeItemImpl#createWrapper(KnowledgeItem)
	 */
	@CalledByReflection
    public AttributedWrapper(KnowledgeObject ko) {
        super(ko);
    }

}
