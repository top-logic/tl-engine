/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.dob.MOFactory;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;

/**
 * {@link MOFactory} implementation for {@link DBKnowledgeBase}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public final class DBKnowledgeTypeFactory extends DefaultMOFactory {

	private final boolean versioning;

	public DBKnowledgeTypeFactory(boolean versioning) {
		this.versioning = versioning;
	}

	/**
	 * Creates a new {@link DBKnowledgeTypeFactory} with versioned types.
	 */
	public DBKnowledgeTypeFactory() {
		this(true);
	}

    @Override
	public MOClass createMOClass(String name) {
        MOKnowledgeItemImpl result = new MOKnowledgeItemImpl(name);
        result.setVersioned(versioning);
		return result;
    }

}
