/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.data.AbstractDataObject;
import com.top_logic.dob.data.DataObjectImpl;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Revision;

/**
 * Abstract implementation of {@link KnowledgeItem} based on a {@link DataObjectImpl}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public abstract class AbstractKnowledgeItem extends AbstractDataObject implements KnowledgeItem {

	/**
	 * Creates a new {@link AbstractKnowledgeItem}.
	 * 
	 * @param type
	 *        see {@link KnowledgeItem#tTable()}
	 */
	public AbstractKnowledgeItem(MOStructure type) {
		super(type);
	}

	@Override
	public MOStructure tTable() {
		return (MOStructure) super.tTable();
	}

	@Override
	public final long getBranchContext() {
		return tId().getBranchContext();
	}

	/**
	 * The commit number identifying the revision this object belongs to.
	 * 
	 * <p>
	 * For a historic object, the commit number identifying the revision this historic object was
	 * requested in. {@link Revision#CURRENT_REV}, for a current mutable object.
	 * </p>
	 * 
	 * <p>
	 * The value returned from {@link #getHistoryContext()} is constant for all objects that are
	 * returned form regular navigation methods (see
	 * {@link KnowledgeObject#getOutgoingAssociations()} starting with a given initial object.
	 * Regular navigation methods are all associations that navigate in current time.
	 * </p>
	 * 
	 * <p>
	 * There may be associations that allow navigating to the past (from a current object to a
	 * historic object and from a historic object in one {@link #getHistoryContext()} history
	 * context} to an object in another {@link #getHistoryContext() history context}.
	 * </p>
	 */
	@Override
	public final long getHistoryContext() {
		return tId().getHistoryContext();
	}

	@Override
	public final TLID getObjectName() {
		return tId().getObjectName();
	}

	@Override
	public final MORepository getTypeRepository() {
		return getKnowledgeBase().getMORepository();
	}

	@Override
	public final IdentifiedObject resolveObject(ObjectKey objectKey) {
		return getKnowledgeBase().resolveObjectKey(objectKey);
	}

	/**
	 * @deprecated Quirks API.
	 */
	@Override
	@Deprecated
	public final void setIdentifier(TLID anIdentifier) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public final TLID getIdentifier() {
		return getObjectName();
	}

	@Override
	public Set<String> getAllAttributeNames() {
		return CollectionUtil.toSet(getAttributeNames());
	}

    @Override
	public final boolean equals(Object anObject) {
		return this == anObject;
    }

	@Override
	public final int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public String toString() {
		return KnowledgeItemImpl.toString(this);
	}

}

