/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * Base class for {@link DBKnowledgeBase} internal implementations of
 * {@link KnowledgeObject} and {@link KnowledgeAssociation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class DBKnowledgeObjectBase extends DynamicKnowledgeItem {

	public DBKnowledgeObjectBase(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		super(kb, staticType);
		
		assert staticType.isSubtypeOf(BasicTypes.getItemType(kb)) : "Knowledge items must have knowledge item type.";
	}

	void initNew(long branchContext, TLID objectName) {
		initIdentifier(objectName, branchContext);
	}


}
