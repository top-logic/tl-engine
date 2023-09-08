/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.lang.ref.Reference;

import com.top_logic.basic.TLID;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;

/**
 * {@link ObjectKey} with a {@link Reference} to its identified object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBObjectKey extends DefaultObjectKey {
	
	private volatile DBKnowledgeBase.IDReference reference;

	public DBObjectKey(long branchContext, long historyContext, MOKnowledgeItem objectType, TLID objectName) {
		super(branchContext, historyContext, objectType, objectName);
	}
	
	/* package protected */void updateReference(DBKnowledgeBase.IDReference newReference) {
		this.reference = newReference;
	}
	
	@Override
	public MOKnowledgeItem getObjectType() {
		// Cast is safe because constructor requires correct type.
		return (MOKnowledgeItem) super.getObjectType();
	}

	DBKnowledgeBase.IDReference getReference() {
		return reference;
	}
	
	KnowledgeItemInternal getCached() {
		if (reference != null) {
			return this.reference.get();
		} else {
			return null;
		}
	}

	
}
