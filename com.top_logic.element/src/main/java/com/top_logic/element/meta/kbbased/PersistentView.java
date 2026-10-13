/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.db2.KnowledgeItemImpl;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * Representation of a {@link LayoutComponent} in the TL model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersistentView extends PersBoundComp implements AttributedObject {

	/**
	 * @see KnowledgeItemImpl#createWrapper(KnowledgeItem)
	 */
	@CalledByReflection
	public PersistentView(KnowledgeObject ko) {
		super(ko);
	}

	/**
	 * Updates through {@link #setValue(String, Object)}, which announces the change through
	 * {@link PersistentObjectImpl#notifyPreChange(com.top_logic.model.TLObject, String, Object)}
	 * before storing it.
	 */
	@Override
	public void tUpdate(TLStructuredTypePart part, Object value) {
		setValue(part.getName(), value);
	}

}
