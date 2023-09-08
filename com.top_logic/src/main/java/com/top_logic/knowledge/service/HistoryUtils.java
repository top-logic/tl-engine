/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.EmptyCompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Static facade for {@link HistoryManager}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HistoryUtils {

	/**
	 * Retrieves the {@link HistoryManager} for the given {@link KnowledgeItem}.
	 */
	public static HistoryManager getHistoryManager(KnowledgeItem item) {
		return getHistoryManager(item.getKnowledgeBase());
	}
	
	/**
	 * Retrieves the default {@link HistoryManager}.
	 */
	public static HistoryManager getHistoryManager() {
		return getHistoryManager(KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase());
	}
	
	/**
	 * Retrieves the {@link HistoryManager} for the given {@link KnowledgeBase}.
	 */
	public static HistoryManager getHistoryManager(KnowledgeBase kb) {
		return kb.getHistoryManager();
	}

    /**
     * The {@link Branch branch context}, the given object was loaded in.
     */
	public static Branch getBranch(KnowledgeItem item) {
		return getHistoryManager(item).getBranch(item.getBranchContext());
	}

    /**
     * The root {@link Branch}.
     */
	public static Branch getTrunk() {
		return getHistoryManager().getBranch(TLContext.TRUNK_ID);
	}

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
	public static Branch setContextBranch(Branch branch) {
		return branch.getHistoryManager().setContextBranch(branch);
	}

    /**
     * The branch in which all creates and non-navigating accesses are executed.
     * 
     * @see #setContextBranch(Branch)
     */
	public static Branch getContextBranch() {
		return getHistoryManager().getContextBranch();
	}

    /**
	 * Return the revision, the given historic version of an object is part of.
	 * 
	 * <p>
	 * This revision equals the revision number used in the query to this
	 * {@link HistoryManager} that either
	 * </p>
	 * 
	 * <ul>
	 * <li>directly returned the given historic object, or </li>
	 * 
	 * <li>returned a historic object that references the given historic object
	 * through associations.</li>
	 * </ul>
	 * 
	 * <p>
	 * <b>Note:</b> This revision is potentially larger than the revision in
	 * which the given object was last updated (see
	 * {@link #getLastUpdate(KnowledgeItem)}).
	 * </p>
     * @param item
	 *        A historic object.
	 * 
	 * @return The revision the given historic object is part of.
	 */
	public static Revision getRevision(KnowledgeItem item) {
		return getHistoryManager(item).getRevision(item.getHistoryContext());
	}

	/**
	 * The revision in which the given object was last updated.
	 * 
	 * @param anObject
	 *        A stable historic object or a current mutable object.
	 * @return The revision, in which the given object was committed to the
	 *         database. The result is {@link Revision#CURRENT}, if the given
	 *         object is newly created in the current context and not yet
	 *         committed.
	 */
	public static Revision getLastUpdate(KnowledgeItem anObject) {
		HistoryManager hm = getHistoryManager(anObject);
		return hm.getRevision(hm.getLastUpdate(anObject));
	}

	/**
	 * The revision in which the given object was created.
	 * 
	 * @param anObject
	 *        A stable historic object or current mutable object.
	 * @return The commit number of the revision, in which the given object was
	 *         initially committed to the database. The result is
	 *         <code>null</code>, if the given object is newly created in the
	 *         current context and not yet committed.
	 */
	public static Revision getCreateRevision(KnowledgeItem anObject) {
		return getCreateRevision(getHistoryManager(anObject), anObject);
	}
	
	public static Revision getCreateRevision(HistoryManager hm, KnowledgeItem anObject) {
		return hm.getRevision(hm.getCreateRevision(anObject));
	}
	
    /**
	 * Retrieves the revision that was created with the given commit number.
	 * 
	 * @param commitNumber
	 *        The commit number that produced the requested revision.
	 * @return the committed revision with the given commit number.
	 */
    public static Revision getRevision(long commitNumber) {
		return getHistoryManager().getRevision(commitNumber);
	}

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
	public static Revision getRevisionAt(long time) {
		return getHistoryManager().getRevisionAt(time);
	}

	/**
	 * The last revision that was successfully committed to the database.
	 */
	public static Revision getLastRevision() {
		return getLastRevision(getHistoryManager());
	}

	/**
	 * The last revision that was successfully committed to the database.
	 */
	public static Revision getLastRevision(HistoryManager hm) {
		return hm.getRevision(hm.getLastRevision());
	}

	/**
	 * The first revision that was successfully committed to the database.
	 */
	public static Revision getInitialRevision(HistoryManager hm) {
		return hm.getRevision(Revision.FIRST_REV);
	}

	/**
	 * Switches the given object to the given branch.
	 * 
	 * <p>
	 * The {@link #getRevision(KnowledgeItem) history context} of the result is
	 * the same as the history context of the given object. If a historic object
	 * version is given, a historic version on the given branch is returned. If
	 * a current object version is given, the current version in the given
	 * branch is returned.
	 * </p>
	 * @param branch
	 *        The target branch, in which the object is requested.
	 * @param item
	 *        The original object to lookup in the given branch.
	 * 
	 * @return The object version on the given branch, or <code>null</code>,
	 *         if the given object does not exist in the given branch.
	 */
	public static KnowledgeItem getKnowledgeItem(Branch branch, KnowledgeItem item) throws DataObjectException {
		return getKnowledgeItem(getHistoryManager(item), branch, item);
	}

	public static KnowledgeItem getKnowledgeItem(HistoryManager hm, Branch branch, KnowledgeItem item) throws DataObjectException {
		return hm.getKnowledgeItem(branch, hm.getRevision(item.getHistoryContext()), item.tTable(), item.getObjectName());
	}

	public static KnowledgeItem getKnowledgeItem(Branch branch, Revision revision, MetaObject type, TLID objectName) throws DataObjectException {
		return getHistoryManager().getKnowledgeItem(branch, revision, type, objectName);
	}
	
	/**
	 * Retrieve the given object in the given revision.
	 * 
	 * <p>
	 * The given object may either be a current mutable object, or itself a
	 * historic version.
	 * </p>
	 * @param revision
	 *        The revision (point in time) in which the given object is
	 *        requested.
	 * @param item
	 *        The sample object that should be retrieved in another point in
	 *        time.
	 * 
	 * @return The historic version of the given object in the given revision.
	 */
	public static KnowledgeItem getKnowledgeItem(Revision revision, KnowledgeItem item) throws DataObjectException {
		return getHistoryManager(item).getKnowledgeItem(revision, item);
	}

	/**
	 * Retrieve a historic version of the object with the given type and
	 * identifier.
	 * 
	 * <p>
	 * Note: The given identifier must be taken from a current version of an
	 * object. Use {@link #getKnowledgeItem(Revision, KnowledgeItem)} to get
	 * other versions of historic objects.
	 * </p>
	 * @param revision
	 *        The revision (point in time) in which the given object is
	 *        requested.
	 * @param type
	 *        The type of the requested object.
	 * @param objectName
	 *        The {@link KnowledgeItem#getObjectName()} of the requested object.
	 * 
	 * @return The historic version of the object with the given type and
	 *         identifier.
	 */
	public static KnowledgeItem getKnowledgeItem(Revision revision, MetaObject type, TLID objectName) throws DataObjectException {
		return getHistoryManager().getKnowledgeItem(getContextBranch(), revision, type, objectName);
	}

	/**
	 * Switches the given object to the given branch in the given revision.
	 * @param branch
	 *        The branch to which the given object should be switched.
	 * @param revision
	 *        The revision at which the given object is requested.
	 * @param item
	 *        The item that is requested in another branch and/or another
	 *        revision.
	 * 
	 * @return The corresponding item on the given branch in the given revision,
	 *         or <code>null</code>, if the item does not exits there.
	 */
	public static KnowledgeItem getKnowledgeItem(Branch branch, Revision revision, KnowledgeItem item) throws DataObjectException {
		return getHistoryManager(item).getKnowledgeItem(branch, revision, item.tTable(), item.getObjectName());
	}

	public static List getObjectsByAttribute(Revision revision, String aType, String attributeName, Object attributeValue) throws DataObjectException {
		return getObjectsByAttribute(getHistoryManager(), revision, aType, attributeName, attributeValue);
	}

	public static List getObjectsByAttribute(HistoryManager hm, Revision revision, String aType, String attributeName, Object attributeValue) throws DataObjectException {
		return hm.getObjectsByAttribute(hm.getContextBranch(), revision, aType, attributeName, attributeValue);
	}

	public static List getObjectsByAttribute(Branch branch, Revision revision, String aType, String attributeName, Object attributeValue) throws DataObjectException {
		return getHistoryManager().getObjectsByAttribute(branch, revision, aType, attributeName, attributeValue);
	}
	
	public static List getObjectsByAttributes(Revision revision, String type, String[] attributeNames, Object[] values) throws DataObjectException {
		return getObjectsByAttributes(getHistoryManager(), revision, type, attributeNames, values);
	}

	public static List getObjectsByAttributes(HistoryManager hm, Revision revision, String type, String[] attributeNames, Object[] values) {
		return hm.getObjectsByAttributes(hm.getContextBranch(), revision, type, attributeNames, values);
	}
	
	public static List getObjectsByAttributes(Branch branch, Revision revision, String type, String[] attributeNames, Object[] values) {
		return getHistoryManager().getObjectsByAttributes(branch, revision, type, attributeNames, values);
	}

	public static ObjectReference getItemIdentity(KnowledgeItem item, Object original) {
		return getHistoryManager(item).getItemIdentity(item, original);
	}
	
	/**
	 * Lookup the branch object for the given ID.
	 */
	public static Branch getBranch(long branchId) {
		return getHistoryManager().getBranch(branchId);
	}

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
	public static Branch createBranch(Branch baseBranch, Revision baseRevision, Set<? extends MetaObject> branchedTypes) {
		return getHistoryManager().createBranch(baseBranch, baseRevision, branchedTypes);
	}

    /**
     * Branches all types.
     * 
     * @see HistoryManager#createBranch(Branch, Revision, Set)
     */
    public static Branch createBranch(Branch baseBranch, Revision baseRevision) {
    	return getHistoryManager().createBranch(baseBranch, baseRevision, null);
    }

	/**
	 * Whether the given object is a current object.
	 * 
	 * @since 5.8.0
	 * 
	 * @param item
	 *        The object to test.
	 *        
	 * @return Whether the given object is from the {@link Revision#CURRENT}.
	 */
	public static boolean isCurrent(KnowledgeItem item) {
		return isCurrent(item.tId());
	}

	/**
	 * Checks whether the given {@link ObjectKey} represents a current object.
	 * 
	 * @since 5.8.0
	 * 
	 * @param key
	 *        The key to test.
	 */
	public static boolean isCurrent(ObjectKey key) {
		long keyRevision = key.getHistoryContext();
		return keyRevision == Revision.CURRENT_REV;
	}

	/**
	 * Returns the revision of the session in given {@link HistoryManager}.
	 */
	public static Revision getSessionRevision(HistoryManager hm) {
		return hm.getRevision(hm.getSessionRevision());
	}

	/**
	 * Returns the revision of the session in the default {@link HistoryManager}.
	 */
	public static Revision getSessionRevision() {
		return getSessionRevision(getHistoryManager());
	}

	/**
	 * Updates the session revision of the given {@link HistoryManager} to the last revision and
	 * returns it.
	 * 
	 * @see HistoryManager#updateSessionRevision()
	 * @see #getLastRevision(HistoryManager)
	 */
	public static Revision updateSessionRevision(HistoryManager hm) throws MergeConflictException {
		return hm.getRevision(hm.updateSessionRevision());
	}

	/**
	 * Updates the session revision of the default {@link HistoryManager} to the last revision and
	 * returns it.
	 */
	public static Revision updateSessionRevision() throws MergeConflictException {
		return updateSessionRevision(getHistoryManager());
	}

	/**
	 * Searches for a range of revisions in the default {@link PersistencyLayer}.
	 * 
	 * <p>
	 * The result only contains persistent revisions, i.e. neither {@link Revision#CURRENT current}
	 * not {@link Revision#INITIAL intial} revisions are contained.
	 * </p>
	 * 
	 * <p>
	 * The order of the result is descending, i.e. newer revisions appear before older revisions in
	 * the result.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> The method must always called in following way:
	 * 
	 * <pre>
	 * CloseableIterator&lt;Revision&gt; revisions = HistoryUtils.getRevisions(...);
	 * try {
	 *     ... do something...
	 * } finally {
	 *     revisions.close();
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param minRevision
	 *        Each revision of the result has a {@link Revision#getCommitNumber()} which is &gt;=
	 *        the <code>minRevision</code>.
	 * @param maxRevision
	 *        Each revision of the result has a {@link Revision#getCommitNumber()} which is &lt;
	 *        than the <code>minRevision</code>.
	 */
	public static CloseableIterator<Revision> getRevisions(long minRevision, long maxRevision) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		return revisionSearchQuery(kb, minRevision, maxRevision).searchStream();
	}

	/**
	 * Returns the last <code>numberRevisions</code> revisions in the database.
	 * 
	 * <p>
	 * The result only contains persistent revisions, i.e. neither {@link Revision#CURRENT current}
	 * not {@link Revision#INITIAL intial} revisions are contained.
	 * </p>
	 * 
	 * <p>
	 * The order of the result is descending, i.e. newer revisions appear before older revisions in
	 * the result.
	 * </p>
	 * 
	 * @param numberRevisions
	 *        The number of revisions to fetch. Must not be negative.
	 */
	public static List<Revision> getLastRevisions(int numberRevisions) {
		if (numberRevisions < 0) {
			throw new IllegalArgumentException("Number revisions (" + numberRevisions + ") must be >= 0.");
		}
		if (numberRevisions == 0) {
			return Collections.emptyList();
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		long lastRevision = kb.getHistoryManager().getLastRevision();
		return getRevisions(lastRevision, numberRevisions - 1, 0);
	}

	/**
	 * Service method to get a range of revision.
	 * 
	 * <p>
	 * If there is a {@link Revision} with {@link Revision#getDate()} not newer than the given
	 * <code>commitDate</code>, the returned list contains the newest such date. Moreover it
	 * contains <code>revisionsBefore</code> {@link Revision}s with older commit date and
	 * <code>revisionsAfter</code> {@link Revision}s with newer commit date. If there are less
	 * revisions before or after, as much revisions as possible are contained.
	 * </p>
	 * 
	 * <p>
	 * The result only contains persistent revisions, i.e. neither {@link Revision#CURRENT current}
	 * not {@link Revision#INITIAL intial} revisions are contained.
	 * </p>
	 * 
	 * <p>
	 * The order of the result is descending, i.e. newer revisions appear before older revisions in
	 * the result.
	 * </p>
	 * 
	 * @param commitDate
	 *        Commit date of the reference revision.
	 * @param revisionsBefore
	 *        Number of revisions before the <code>commitDate</code> to add to the result.
	 * @param revisionsAfter
	 *        Number of revisions after the <code>commitDate</code> to add to the result.
	 * 
	 * @see HistoryUtils#getRevisions(long, int, int)
	 */
	public static List<Revision> getRevisions(Date commitDate, int revisionsBefore, int revisionsAfter) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		long revision = kb.getHistoryManager().getRevisionAt(commitDate.getTime()).getCommitNumber();
		return getRevisions(revision, revisionsBefore, revisionsAfter);
	}

	/**
	 * Service method to get a range of revision.
	 * 
	 * <p>
	 * Returns a list of revision containing the revision with the given commit number. Moreover it
	 * contains <code>revisionsBefore</code> older revisions and <code>revisionsAfter</code> newer
	 * revisions. If there are less revisions before or after, as much revisions as possible are
	 * contained.
	 * </p>
	 * 
	 * <p>
	 * The result only contains persistent revisions, i.e. neither {@link Revision#CURRENT current}
	 * not {@link Revision#INITIAL intial} revisions are contained.
	 * </p>
	 * 
	 * <p>
	 * The order of the result is descending, i.e. newer revisions appear before older revisions in
	 * the result.
	 * </p>
	 * 
	 * @param commitNumber
	 *        Commit number of the reference revision.
	 * @param revisionsBefore
	 *        Number of revisions before the <code>revision</code> to add to the result.
	 * @param revisionsAfter
	 *        Number of revisions after the <code>revision</code> to add to the result.
	 * 
	 * @see HistoryUtils#getRevisions(Date, int, int)
	 */
	public static List<Revision> getRevisions(long commitNumber, int revisionsBefore, int revisionsAfter) {
		if (revisionsAfter < 0) {
			throw new IllegalArgumentException("revisionsAfter (" + revisionsAfter + ") must be >= 0");
		}
		if (revisionsBefore < 0) {
			throw new IllegalArgumentException("revisionsBefore (" + revisionsBefore + ") must be >= 0");
		}
		int numberElements = revisionsAfter + revisionsBefore + 1;
		if (numberElements < 0) {
			// overflow
			throw new IllegalArgumentException(
				"Number of revisions (" + revisionsBefore + " + " + revisionsAfter + ") does not fit into a list.");
		}
		long maxRevision = commitNumber + revisionsAfter + 1;
		if (maxRevision < 0) {
			// overflow
			maxRevision = Long.MAX_VALUE;
		}
		long minRevision = commitNumber - revisionsBefore;
		return revisionSearchQuery(PersistencyLayer.getKnowledgeBase(), minRevision, maxRevision).search();
	}

	private static CompiledQuery<Revision> revisionSearchQuery(KnowledgeBase kb, long minRevision, long maxRevision) {
		if (maxRevision < 0) {
			// No revision with negative commit number
			return EmptyCompiledQuery.getInstance();
		}
		return revisionSearchQuery(kb, BasicTypes.REVISION_REV_ATTRIBUTE, minRevision, maxRevision);
	}

	/**
	 * Searches for a range of revisions in the default {@link PersistencyLayer}.
	 * 
	 * <p>
	 * The result only contains persistent revisions, i.e. neither {@link Revision#CURRENT current}
	 * not {@link Revision#INITIAL intial} revisions are contained.
	 * </p>
	 * 
	 * <p>
	 * The order of the result is descending, i.e. newer revisions appear before older revisions in
	 * the result.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> The method must always called in following way:
	 * 
	 * <pre>
	 * CloseableIterator&lt;Revision&gt; revisions = HistoryUtils.getRevisions(...);
	 * try {
	 *     ... do something...
	 * } finally {
	 *     revisions.close();
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param minDate
	 *        Each revision of the result has a {@link Revision#getDate()} which is &gt;= the
	 *        <code>minDate</code>.
	 * @param maxDate
	 *        Each revision of the result has a {@link Revision#getDate()} which is &lt; than the
	 *        <code>maxDate</code>.
	 */
	public static CloseableIterator<Revision> getRevisions(Date minDate, Date maxDate) {
		return revisionSearchQuery(PersistencyLayer.getKnowledgeBase(), minDate, maxDate).searchStream();
	}

	private static CompiledQuery<Revision> revisionSearchQuery(KnowledgeBase kb, Date minDate, Date maxDate) {
		return revisionSearchQuery(kb, BasicTypes.REVISION_DATE_ATTRIBUTE, minDate.getTime(), maxDate.getTime());
	}

	private static CompiledQuery<Revision> revisionSearchQuery(KnowledgeBase kb, String attributeName,
			long minAttrValue, long maxAttrValue) {
		if (maxAttrValue <= minAttrValue) {
			return EmptyCompiledQuery.getInstance();
		}
		SetExpression allOfRevisionType = allOf(BasicTypes.REVISION_TYPE_NAME);
		Expression inRange = attributeRange(BasicTypes.REVISION_TYPE_NAME, attributeName, minAttrValue, maxAttrValue);
		Order order = order(attribute(BasicTypes.REVISION_TYPE_NAME, attributeName), true);
		RevisionQuery<Revision> query = queryResolved(filter(allOfRevisionType, inRange), order, Revision.class);
		return kb.compileQuery(query);
	}

	/**
	 * Updates the session and interaction revision of the given {@link HistoryManager} to the given
	 * revision.
	 * 
	 * @param hm
	 *        The {@link HistoryManager} to update.
	 * @param subSession
	 *        The current subsession.
	 * @param revision
	 *        The {@link UpdateChainLink} representing the revision that must be used to access
	 *        persistent objects.
	 * @return The former subsession revision.
	 */
	@FrameworkInternal
	public static UpdateChainLink updateSessionAndInteractionRevision(HistoryManager hm, TLSubSessionContext subSession,
			UpdateChainLink revision) {
		UpdateChainLink oldRevision = subSession.updateSessionRevision(hm, revision);
		TLContextManager.getInteraction().updateInteractionRevision(hm, revision.getRevision());
		return oldRevision;
	}

}
