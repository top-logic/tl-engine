/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * {@link ItemEvent} that announces the creation of an object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObjectCreation extends ItemCreation {

	/**
	 * Creates a {@link ObjectCreation}.
	 *
	 * @param revision See {@link #getRevision()}.
	 * @param objectId See {@link #getObjectId()}.
	 */
	public ObjectCreation(long revision, ObjectBranchId objectId) {
		super(revision, objectId);
	}

	@Override
	public EventKind getKind() {
		return EventKind.create;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (! (other instanceof ObjectCreation)) {
			return false;
		}
		
		ObjectCreation otherEvent = ((ObjectCreation) other);
		return equalsItemChange(otherEvent);
	}


	@Override
	public <R, A> R visitItemEvent(ItemEventVisitor<R, A> v, A arg) {
		return v.visitCreateObject(this, arg);
	}

}
