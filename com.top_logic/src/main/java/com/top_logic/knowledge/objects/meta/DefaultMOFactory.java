/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.meta;

import com.top_logic.dob.AbstractMOFactory;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.KIReference;

/**
 * Default implementation of {@link MOFactory}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultMOFactory extends AbstractMOFactory {

    public static final MOFactory INSTANCE = new DefaultMOFactory();
    
    protected DefaultMOFactory() {
		// Singleton constructor.
	}

	@Override
	public MOReference createMOReference(String name, MetaObject targetType, Boolean byValue) {
		if (byValue != null && byValue.booleanValue()) {
			return KIReference.referenceByValue(name, targetType);
		} else {
			return KIReference.referenceById(name, targetType);
		}
	}

}
