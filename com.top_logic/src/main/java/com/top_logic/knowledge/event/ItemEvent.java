/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import com.top_logic.basic.TLID;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.Branch;

/**
 * {@link KnowledgeEvent} representing object and link operations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ItemEvent extends KnowledgeEvent {

	private ObjectBranchId objectId;
	
	private ObjectKey originalObject = null;

	/**
	 * Creates a new {@link ItemEvent}
	 * 
	 * @param revision
	 *        See {@link #getRevision()}
	 * @param objectId
	 *        See {@link #getObjectId()}
	 */
	public ItemEvent(long revision, ObjectBranchId objectId) {
		super(revision);
		
		this.objectId = objectId;
	}

	/**
	 * The {@link ObjectBranchId} of the item touched by this event.
	 */
	public ObjectBranchId getObjectId() {
		return objectId;
	}
	
	/**
	 * @see #getObjectId()
	 */
	public void setObjectId(ObjectBranchId objectId) {
		initOriginalObject();
		this.objectId = objectId;
	}
	
	@Override
	public void setRevision(long revision) {
		initOriginalObject();
		super.setRevision(revision);
	}
	
	private void initOriginalObject() {
		if (originalObject != null) {
			return;
		}
		originalObject = objectId.toObjectKey(getRevision());
	}

	/**
	 * The type of the object that is modified in this event.
	 * 
	 * @see KnowledgeItem#tTable()
	 */
	public MetaObject getObjectType() {
		return this.objectId.getObjectType();
	}

	/**
	 * The {@link KnowledgeItem#getObjectName() object name} of the object
	 * modified in this event.
	 */
	public TLID getObjectName() {
		return this.objectId.getObjectName();
	}
	
		/**
	 * The {@link Branch#getBranchId()} of the branch of the the object modified
	 * in this event.
	 */
	public long getOwnerBranch() {
		return this.objectId.getBranchId();
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (! (other instanceof ItemEvent)) {
			return false;
		}
		
		ItemEvent otherEvent = ((ItemEvent) other);
		return equalsItemEvent(otherEvent);
	}

	/**
	 * Checks whether the given {@link ItemEvent} is equal to this {@link ItemEvent}. Properties of
	 * class {@link ItemEvent} and super classes are considered.
	 */
	protected final boolean equalsItemEvent(ItemEvent otherEvent) {
		return super.equalsKnowledgeEvent(otherEvent) && 
			ObjectBranchId.equalsObjectIdTypeName(this.objectId, otherEvent.objectId);
	}
	
	@Override
	public final int hashCode() {
		// Method is final, since no more information is necessary for hashing
		// in subclasses, because there might be only one event per item and
		// revision.
		
		return super.hashCode() + ObjectBranchId.hashCodeObjectIdTypeName(this.objectId);
	}
	
	@Override
	protected void appendProperties(StringBuilder result) {
		super.appendProperties(result);
		result.append(", id: '");
		result.append(this.objectId);
		result.append('\'');
	}

	/**
	 * Returns the {@link ObjectKey} of the object <b>after</b> this event. I.e. if this is an
	 * {@link ItemUpdate} then the object with the returned {@link ObjectKey} has the new values.
	 * 
	 * The event always is an {@link ObjectKey} appropriate for the source database and no changes
	 * made on this event (e.g. changing revision) are reflected by the returned object key.
	 */
	public ObjectKey getOriginalObject() {
		initOriginalObject();
		return originalObject;
	}

	/**
	 * Delegates to {@link #visitItemEvent(ItemEventVisitor, Object)};
	 * 
	 * @return result of {@link #visitItemEvent(ItemEventVisitor, Object)}
	 * 
	 * @see KnowledgeEvent#visit(KnowledgeEventVisitor, Object)
	 */
	@Override
	public final <R, A> R visit(KnowledgeEventVisitor<R, A> v, A arg) {
		return visitItemEvent(v, arg);
	}

	/**
	 * Visit interface for the {@link ItemEvent} hierarchy.
	 * 
	 * @param v
	 *        The concrete visitor.
	 * @param arg
	 *        The argument passed to the visit.
	 * @return The value computed by the given visitor.
	 */
	public abstract <R, A> R visitItemEvent(ItemEventVisitor<R, A> v, A arg);

}
