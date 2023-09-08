/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.util.TLContext;

/**
 * Provides access to historic versions of versioned {@link KnowledgeItem}s.
 * 
 * @see HistoryUtils A static facade for this interface with convenience
 *      methods.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface HistoryManager {

    /**
     * Whether this {@link HistoryManager} can provide historic data.
     */
	public boolean hasHistory();

	/**
	 * The commit number of the last revision that was successfully committed to
	 * the database.
	 * 
	 * <p>
	 * The result can be used in calls to this {@link HistoryManager} to fetch
	 * stable versions of objects that correspond best to their mutable current
	 * versions.
	 * </p>
	 */
    public long getLastRevision();

	/**
	 * Returns the revision of the current session in this {@link HistoryManager}.
	 */
	long getSessionRevision();

	/**
	 * Updates the {@link #getSessionRevision() revision of the current session} to
	 * {@link #getLastRevision()} and returns the value.
	 * 
	 * <p>
	 * Note: It is not guaranteed that the returned value is the same as the last revision after
	 * this method returns, because a concurrent thread could make a commit in the meanwhile.
	 * </p>
	 * 
	 * @return The new session revision.
	 * 
	 * @throws MergeConflictException
	 *         Update of session revision merges the changes of the newer revisions with the current
	 *         local changes. Eventually detected conflicts are reported by the thrown
	 *         {@link MergeConflictException}.
	 * 
	 * @see #getSessionRevision()
	 */
	long updateSessionRevision() throws MergeConflictException;

    /**
	 * Create an object that can be used as proxy for the given item in places,
	 * where {@link Object#equals(Object) object equality} based on the
	 * underlying object (not its versioned instance) is important.
	 * 
	 * <p>
	 * From the resulting {@link ObjectReference}, the given original object can be
	 * retrieved as {@link ObjectReference#getObject()}.
	 * </p>
	 * 
	 * @param item
	 *        The {@link KnowledgeItem} for which its unversioned identity is
	 *        requested.
	 * @param original
	 *        Any other wrapper for the given {@link KnowledgeItem} that is
	 *        considered the {@link ObjectReference#getObject() original} in the usage
	 *        context of the key.
	 * @return A key object that implements equality based on the
	 *         {@link KnowledgeItem} disregarding its version.
	 */
    public ObjectReference getItemIdentity(KnowledgeItem item, Object original);
    
    /**
	 * Retrieves the revision that was created with the given commit number.
	 * 
	 * @param commitNumber
	 *        The commit number that produced the requested revision. Must not
	 *        be <code>null</code>.
	 * @return the committed revision with the given commit number.
	 */
    public Revision getRevision(long commitNumber);

	/**
	 * Retrieves the commit number of the revision that is not newer than the
	 * given date.
	 * 
	 * @param time
	 *        The date in the internal time format, see
	 *        {@link System#currentTimeMillis()}.
	 * 
	 * @return The {@link Revision} that is the oldest revision not newer than
	 *         the given date.
	 */
	public Revision getRevisionAt(long time);

	/**
	 * The revision in which the given object was last updated.
	 * 
	 * @param item
	 *        A stable historic object or current mutable object.
	 * @return The commit number of the revision, in which the given object was committed to the
	 *         database. The result is {@link Revision#CURRENT_REV}, if the given object is newly
	 *         created in the current context and not yet committed.
	 */
    public long getLastUpdate(KnowledgeItem item);

	/**
	 * The revision in which the given object was created.
	 * 
	 * @param item
	 *        A stable historic object or current mutable object.
	 * @return The commit number of the revision, in which the given object was initially committed
	 *         to the database. The result is {@link Revision#CURRENT_REV}, if the given object is
	 *         newly created in the current context and not yet committed.
	 */
    public long getCreateRevision(KnowledgeItem item);
    
	/**
	 * Retrieve the given object in the given revision.
	 * 
	 * <p>
	 * The given object may either be a current mutable object, or itself a
	 * historic version.
	 * </p>
	 * 
	 * @param revision
	 *        The revision (point in time) in which the given object is
	 *        requested.
	 * @param anObject
	 *        The sample object that should be retrieved in another point in
	 *        time.
	 * @return The historic version of the given object in the given revision.
	 */
	public KnowledgeItem getKnowledgeItem(Revision revision, KnowledgeItem anObject) throws DataObjectException;
	
	/**
	 * Retrieve an object with its complete identity.
	 * 
	 * @param branch
	 *        The branch in which the given object is requested.
	 * @param revision
	 *        The revision (point in time) in which the given object is
	 *        requested.
	 * @param type
	 *        The type of the requested object.
	 * @param objectName
	 *        The {@link KnowledgeItem#getObjectName()} of the requested object.
	 * @return The historic version of the object with the given type and
	 *         identifier.
	 */
    public KnowledgeItem getKnowledgeItem(Branch branch, Revision revision, MetaObject type, TLID objectName) throws DataObjectException;

    /**
	 * Search all objects with the given attribute/value combination that were
	 * current at the given revision.
	 * 
	 * @param branch
	 *        The branch the object is requested in.
	 * @param revision
	 *        The revision (point in time) in which the given object is
	 *        requested.
	 * @param type
	 *        The {@link MetaObject#getName()} type of the requested objects.
	 * @param attributeName
	 *        See
	 *        {@link KnowledgeBase#getObjectsByAttribute(String, String, Object)}
	 * @param attributeValue
	 *        See
	 *        {@link KnowledgeBase#getObjectsByAttribute(String, String, Object)}
	 * @return A list of historic versions of objects that have the given value
	 *         in the given attribute.
	 */
    public List getObjectsByAttribute(Branch branch, Revision revision, String type, String attributeName, Object attributeValue) throws DataObjectException;

    /**
     * The root {@link Branch}.
     */
    public Branch getTrunk();

    /**
     * The branch in which all creates and non-navigating accesses are executed.
     * 
     * @see #setContextBranch(Branch)
     */
	public Branch getContextBranch();

	/**
	 * Sets the {@link #getContextBranch() context branch} for the current
	 * thread to the given branch.
	 * 
	 * @return the formerly set context branch, the branch of the current
	 *         session.
	 * 
	 * @see TLContext#setSessionBranch(Branch) The branch of the current
	 *      session.
	 */
	public Branch setContextBranch(Branch branch);
	
	/**
	 * Search all objects with the given attribute/value combination that were
	 * current at the given revision.
	 * 
	 * @param branch
	 *        The branch the object is requested in.
	 * @param revision
	 *        The revision (point in time) in which the given object is
	 *        requested.
	 * @param type
	 *        The {@link MetaObject#getName()} type of the requested objects.
	 * @param attributeNames
	 *        See
	 *        {@link KnowledgeBase#getObjectsByAttribute(String, String[], Object[])}
	 * @param values
	 *        See
	 *        {@link KnowledgeBase#getObjectsByAttribute(String, String[], Object[])}
	 * @return A list of historic versions of objects that have the given value
	 *         in the given attribute.
	 */
	public List getObjectsByAttributes(Branch branch, Revision revision, String type, String[] attributeNames, Object[] values);

	/**
	 * Lookup the branch object for the given ID.
	 */
	public Branch getBranch(long branchId);

	/**
	 * Creates a new branch that is based on the given branch in the given
	 * {@link Revision}.
	 * 
	 * @param branchedTypes
	 *        The set of {@link MOKnowledgeItem types} whose instances are
	 *        branched. Instances of other types are visible from the base
	 *        branch. <code>null</code> means to branch instances of all
	 *        types.
	 */
	public Branch createBranch(Branch baseBranch, Revision baseRevision, Set<? extends MetaObject> branchedTypes);

}
