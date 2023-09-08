/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.TableTyped;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;

/**
 * Common base interface for {@link KnowledgeObject}s and
 * {@link KnowledgeAssociation}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface KnowledgeItem extends DataObject, TableTyped, ObjectContext {

	/**
	 * Value of {@link #getCreateCommitNumber()}, if the value is not yet available.
	 */
	static final long NO_CREATE_REVISION = Revision.CURRENT_REV;

	/**
	 * Live-cycle state of a {@link KnowledgeItem}.
	 * 
	 * @see KnowledgeItem#getState()
	 */
	public enum State {

		/**
		 * The item is locally created in a transaction that is not yet committed.
		 */
		NEW,

		/**
		 * The item has a persistent representation.
		 * 
		 * <p>
		 * In this state, the item may have local modifications in a running transaction, or may
		 * even be locally deleted in some transaction.
		 * </p>
		 */
		PERSISTENT

	}

	/**
	 * @deprecated Either use {@link #tId()} or {@link #getObjectName()}.
	 * 
	 * @see KBUtils#getObjectKeyString(KnowledgeItem) for extenalized keys as string.
	 * @see KBUtils#getObjectName(KnowledgeItem) for extenalized names as string.
	 */
	@Override
	@Deprecated
	public TLID getIdentifier();

	/**
	 * Whether this object is alive in the current context.
	 * 
	 * <p>
	 * An object is alive if it is not deleted (neither locally in the current
	 * context nor globally committed). A new object is alive for the creating
	 * thread until it is committed.
	 * </p>
	 */
	boolean isAlive();
	
	/**
	 * The life-cycle {@link State} of this item.
	 */
	State getState();

	/**
	 * Marks this item for change.
	 */
	void touch();

	/**
	 * Return the {@link KnowledgeBase} where this Object is currently contained in.
	 * 
	 * @return never <code>null</code>
	 */
	public KnowledgeBase getKnowledgeBase();

	/**
	 * The pure identity of this object independent of versions and branches.
	 * 
	 * <p>
	 * The same object in different versions has the same object name. The same
	 * object on different branches has the same object name.
	 * </p>
	 * 
	 * @return The identity of this object independent of versions and branches.
	 *         Names for the same object on different branches or the same
	 *         object in different historic versions are equal. The result is of
	 *         basic type: {@link String}, {@link Long}, {@link Integer}.
	 *         
	 * @see KBUtils#getObjectName(KnowledgeItem) for an externalized form.
	 */
	TLID getObjectName();

	/**
	 * The {@link Branch#getBranchId() branch ID} in which associations with
	 * this object are resolved.
	 * 
	 * @return The branch in which this object is navigated.
	 */
	long getBranchContext();

	/**
	 * The {@link Revision#getCommitNumber() revision ID} in which associations
	 * with this object are resolved, <code>null</code> for a
	 * {@link Revision#CURRENT current} object.
	 * 
	 * @return The history context in which this object is navigated.
	 */
	long getHistoryContext();
	
	/**
	 * Returns the commit number in which this object was created. Used to resolve
	 * {@link LifecycleAttributes}.
	 * 
	 * @return The commit number in which the item was created or {@link #NO_CREATE_REVISION} if not
	 *         yet available.
	 */
	long getCreateCommitNumber();

	/**
	 * The {@link Revision#getCommitNumber() number} of the commit that established the current
	 * state of this object.
	 */
	long getLastUpdate();

	/**
	 * Same as {@link KnowledgeBase#delete(KnowledgeItem)}.
	 */ 
    public void delete() throws DataObjectException;

	/**
	 * The implementation of the application interface for this object.
	 */
	public <T extends TLObject> T getWrapper();
	
	/**
	 * Returns a set containing all attribute names for which this {@link KnowledgeItem} can deliver
	 * a value.
	 * 
	 * <p>
	 * In contrast to {@link #getAttributeNames()} also dynamic attributes are contained.
	 * </p>
	 * 
	 */
	Set<String> getAllAttributeNames();

	/**
	 * Returns the global value of the given attribute, i.e. the value visible for all other
	 * sessions without local modification.
	 * 
	 * @see KnowledgeItem#getAttributeValue(String)
	 */
	Object getGlobalAttributeValue(String attribute) throws NoSuchAttributeException;

	/**
	 * An {@link Object} is equal to this object, iff it is <code>==</code> this
	 * {@link KnowledgeItem}.
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * {@link System#identityHashCode(Object) Identity hash Code} of this
	 *         {@link KnowledgeItem}.
	 */
	@Override
	int hashCode();

}
