/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.identifier;

import com.top_logic.basic.TLID;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;

/**
 * {@link ObjectKey} that intrinsically can retrieve the object it identifies.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObjectReference extends DefaultObjectKey {
	
	private final Object original;

	public ObjectReference(Object original, long branchContext, long historyContext, MetaObject objectType, TLID objectName) {
		super(branchContext, historyContext, objectType, objectName);
		this.original = original;
	}
	
	/**
	 * The original object identified by this key.
	 */
	public Object getObject() {
		return original;
	}

}
