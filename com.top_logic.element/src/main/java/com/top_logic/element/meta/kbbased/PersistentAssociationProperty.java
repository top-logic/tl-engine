/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link TLAssociationProperty} based on a {@link PersistentProperty}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersistentAssociationProperty extends PersistentProperty implements TLAssociationProperty {

	/**
	 * Creates a new {@link PersistentAssociationProperty}.
	 */
	public PersistentAssociationProperty(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public TLAssociation getOwner() {
		return tGetDataReference(TLAssociation.class, ownerRef());
	}

	@Override
	protected String ownerRef() {
		return KBBasedMetaAttribute.OWNER_REF;
	}

	@Override
	public TLStructuredTypePart getDefinition() {
		return this;
	}

	@Override
	public boolean isOverride() {
		return false;
	}

}

