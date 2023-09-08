/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.identifier;

import com.top_logic.basic.TLID;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.Revision;

/**
 * An {@link ObjectBranchId} represents the identity of an object independent of the
 * revision of a concrete version of the object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObjectBranchId {

	private final long branchId;
	private final MetaObject objectType;

	private final TLID objectName;

	/**
	 * Creates a {@link ObjectBranchId}.
	 * 
	 * @param branchId
	 *        See {@link #getBranchId()}
	 * @param objectType
	 *        See {@link #getObjectType()}
	 * @param objectName
	 *        See {@link #getObjectName()}
	 */
	public ObjectBranchId(long branchId, MetaObject objectType, TLID objectName) {
		this.branchId = branchId;
		this.objectType = objectType;
		this.objectName = objectName;
	}

	/**
	 * The identifier of the item's branch.
	 */
	public long getBranchId() {
		return branchId;
	}

	/**
	 * The type of the item.
	 * 
	 * @see KnowledgeItem#tTable()
	 */
	public MetaObject getObjectType() {
		return objectType;
	}

	/**
	 * The name of the item.
	 * 
	 * @see KnowledgeItem#getObjectName()
	 */
	public TLID getObjectName() {
		return objectName;
	}
	
	@Override
	public int hashCode() {
		return hashCode(this);
	}
	
	@Override
	public boolean equals(Object other) { // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
		return equals(this, other);
	}
	
	@Override
	public String toString() {
		return toString(this);
	}
	
	static boolean equals(ObjectBranchId self, Object other) {
		if (other == self) {
			return true;
		}
		
		if (! (other instanceof ObjectBranchId)) {
			return false;
		}

		ObjectBranchId otherId = (ObjectBranchId) other;
		
		return equalsObjectId(self, otherId);
	}

	static boolean equalsObjectId(ObjectBranchId self, ObjectBranchId otherId) {
		boolean result = 
			self.getObjectName().equals(otherId.getObjectName()) && 
			self.getBranchId() == otherId.getBranchId() && 
			self.getObjectType().equals(otherId.getObjectType()); 

		return result;
	}

	static int hashCode(ObjectBranchId self) {
		int result = self.getObjectType().hashCode();
		result += 16661 * self.getObjectName().hashCode();
		result += 44641 * self.getBranchId();
		return result;
	}

	/**
	 * Equality of {@link ObjectBranchId}s based on type name equality instead of type equality.
	 */
	public static boolean equalsObjectIdTypeName(ObjectBranchId self, ObjectBranchId otherId) {
		boolean result = 
			self.getObjectName().equals(otherId.getObjectName()) && 
			self.getBranchId() == otherId.getBranchId() && 
			self.getObjectType().getName().equals(otherId.getObjectType().getName()); 
		
		return result;
	}

	/**
	 * Hash code of {@link ObjectBranchId}s base on type name hash code instead of
	 * type hash code.
	 */
	public static int hashCodeObjectIdTypeName(ObjectBranchId self) {
		int result = self.getObjectType().getName().hashCode();
		result += 16661 * self.getObjectName().hashCode();
		result += 44641 * self.getBranchId();
		return result;
	}
	
	static String toString(ObjectBranchId self) {
		return self.getObjectType().getName() + ":" + self.getObjectName().toString() + "-" + self.getBranchId();
	}
	
	/**
	 * Translates this {@link ObjectBranchId} to an {@link ObjectKey} for the given {@link Revision}
	 * 
	 * @param revision
	 *        must not be <code>null</code> and is used as {@link ObjectKey#getHistoryContext()} of
	 *        the returned {@link ObjectKey}.
	 *        
	 * @return an {@link ObjectKey} with the same branch, type, and object name.
	 * 
	 * @see ObjectBranchId#toCurrentObjectKey()
	 */
	public ObjectKey toObjectKey(Revision revision) {
		return toObjectKey(revision.getCommitNumber());
	}

	/**
	 * Translates this {@link ObjectBranchId} to an {@link ObjectKey} for the given history context
	 * 
	 * @param historyContext
	 *        must not be <code>null</code> and is used as {@link ObjectKey#getHistoryContext()} of
	 *        the returned {@link ObjectKey}.
	 * 
	 * @return an {@link ObjectKey} with the same branch, type, and object name.
	 * 
	 * @see ObjectBranchId#toObjectKey(Revision)
	 */
	public ObjectKey toObjectKey(long historyContext) {
		return new DefaultObjectKey(getBranchId(), historyContext, getObjectType(), getObjectName());
	}

	/**
	 * Translates this {@link ObjectBranchId} to an {@link ObjectKey} in {@link Revision#CURRENT}
	 * 
	 * @return an {@link ObjectKey} with the same branch, type, and object name.
	 * 
	 * @see ObjectBranchId#toObjectKey(Revision)
	 */
	public ObjectKey toCurrentObjectKey() {
		return toObjectKey(Revision.CURRENT);
	}

	/**
	 * adaptor method to create an {@link ObjectBranchId} from the given {@link ObjectKey}.
	 * 
	 * @param key
	 *        the key to adopt
	 * 
	 * @return an {@link ObjectBranchId} with the values from the given {@link ObjectKey}.
	 */
	public static ObjectBranchId toObjectBranchId(ObjectKey key) {
		return new ObjectBranchId(key.getBranchContext(), key.getObjectType(), key.getObjectName());
	}

}
