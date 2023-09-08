/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Base class for {@link KnowledgeBase} internal items that support no dynamic subtyping.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class StaticSystemItem extends ImmutableKnowledgeItem implements StaticItem {

	StaticSystemItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		super(kb, staticType);
	}

}
