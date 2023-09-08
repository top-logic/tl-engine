/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;

/**
 * {@link TLClassProperty} implementation based on a {@link PersistentProperty}.
 * 
 * @since 5.8.0
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersistentClassProperty extends PersistentProperty implements TLClassProperty {

	/** 
	 * Creates a new {@link PersistentClassProperty}.
	 */
	public PersistentClassProperty(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	protected String ownerRef() {
		return KBBasedMetaAttribute.OWNER_REF;
	}

	@Override
	public TLClass getOwner() {
		return tGetDataReference(TLClass.class, ownerRef());
	}

}
