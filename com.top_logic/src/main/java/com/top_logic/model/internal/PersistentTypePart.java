/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.internal;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLTypePart;

/**
 * Abstract super class for {@link TLTypePart} based on {@link PersistentModelPart}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class PersistentTypePart extends PersistentModelPart implements TLTypePart {

	/**
	 * Creates a new {@link PersistentTypePart}.
	 * 
	 * @see PersistentModelPart#PersistentModelPart(KnowledgeObject)
	 */
	@CalledByReflection
	public PersistentTypePart(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public TLModel getModel() {
		return getOwner().getModel();
	}

}

