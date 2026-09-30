/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.knowledge;

import com.top_logic.element.changelog.KBChangeAnalzyer;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.DefaultDBContext;
import com.top_logic.model.cs.TLObjectChangeSet;

/**
 * {@link DefaultDBContext} for tl-element.
 */
public class ElementDBContext extends DefaultDBContext {

	/**
	 * Creates a new {@link ElementDBContext}.
	 * 
	 */
	public ElementDBContext(DBKnowledgeBase kb, String updater) {
		super(kb, updater);
	}

	@Override
	protected TLObjectChangeSet createModelChangeSet(UpdateEvent event) {
		return new KBChangeAnalzyer(event).analyze();
	}
}

