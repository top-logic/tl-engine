/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.changelog.model.Creation;
import com.top_logic.element.changelog.model.Deletion;
import com.top_logic.element.changelog.model.Modification;
import com.top_logic.element.changelog.model.Update;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.element.model.cache.ElementModelCacheService;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Technical (non-UI) utility that reverts one or more model {@link ChangeSet}s.
 *
 * <p>
 * It performs the model-level undo without any UI state or selection: a single change set is
 * reverted with {@link #revert(KnowledgeBase, ChangeSet, boolean)} (after
 * {@link #revertTarget(ChangeSet)} has chosen between undo and redo), a set of change sets with
 * {@link #revertAll(Collection)}, which applies {@link ChangeSet#revert() their inverses} in
 * descending revision order within a single transaction.
 * </p>
 *
 * <p>
 * The command handlers {@link RevertCommandHandler}, {@link UndoCommandHandler} and
 * {@link RedoCommandHandler} are built on it, as are higher-level operations such as
 * {@link #revertSubtreeTo(TLObject, Revision, ResKey) subtree rollback}.
 * </p>
 */
public final class ChangeSetReverter {

	private ChangeSetReverter() {
		// utility
	}

	/**
	 * The change set whose revert toggles the effect of the given one in the current state.
	 *
	 * <p>
	 * Follows the chain of {@link ChangeSet#getRevertedBy() reverts} to its end, the change set
	 * of the chain that is currently in effect: a change that has not been reverted is its own
	 * target, reverting it undoes it. A reverted change has its undo as target, reverting the undo
	 * redoes the change. A redone change has its redo as target, reverting the redo undoes the
	 * change again, and so on. A change set is therefore never reverted twice.
	 * </p>
	 *
	 * <p>
	 * The chain is the one the {@link ChangeLogBuilder} linked when reading the change log.
	 * </p>
	 *
	 * @param change
	 *        The change the user wants to toggle.
	 * @return The change set to pass to {@link #revert(KnowledgeBase, ChangeSet, boolean)}.
	 */
	public static ChangeSet revertTarget(ChangeSet change) {
		ChangeSet result = change;
		while (true) {
			ChangeSet revertedBy = result.getRevertedBy();
			if (revertedBy == null) {
				return result;
			}
			result = revertedBy;
		}
	}

	/**
	 * Whether the given change is undone in the current state.
	 *
	 * <p>
	 * The state of a change is decided by its chain of {@link ChangeSet#getRevertedBy() reverts}
	 * ({@link #revertTarget(ChangeSet)}): every revert in the chain toggles the change, so it is
	 * undone when the chain following it has an odd length. A change that has been reverted is
	 * undone, one whose revert has been reverted again is in effect, and so on. This holds for an
	 * undo or a redo listed in the change log as well: an undo that has been reverted by a redo is
	 * undone itself. A command offering to undo a change is therefore offered when this method
	 * returns {@code false}, one offering to redo it when this method returns {@code true}.
	 * </p>
	 *
	 * @param change
	 *        The change to check, may be {@code null}.
	 * @return Whether the change is undone; {@code false} for {@code null}.
	 */
	public static boolean isUndone(ChangeSet change) {
		boolean undone = false;
		for (ChangeSet revert = change == null ? null : change.getRevertedBy(); revert != null;
				revert = revert.getRevertedBy()) {
			undone = !undone;
		}
		return undone;
	}

	/**
	 * Reverts the given change set in a transaction of its own, committed with the
	 * {@link TransientChangeSet#getMessage() message} of its {@link ChangeSet#revert() revert}.
	 *
	 * <p>
	 * The revert does not follow {@link ChangeSet#getRevertedBy()}; a caller toggling a change
	 * passes {@link #revertTarget(ChangeSet)}.
	 * </p>
	 *
	 * @param kb
	 *        The {@link KnowledgeBase} to open the transaction on.
	 * @param change
	 *        The change set to revert.
	 * @param force
	 *        Whether the revert is committed although applying it reported problems (conflicts
	 *        with later changes of the same objects). Without, a revert with problems is rolled
	 *        back with {@link I18NConstants#UNDO_CANCELED_BY_USER} and nothing is committed; the
	 *        caller may ask the user and call again with {@code true}.
	 * @return The problems applying the revert reported, empty for a clean revert. The revert has
	 *         been committed if the list is empty or {@code force} is set.
	 * @throws TopLogicException
	 *         If the change set is {@link ChangeSet#getIncomplete() incomplete}, see
	 *         {@link #checkComplete(ChangeSet)}.
	 */
	public static List<ResKey> revert(KnowledgeBase kb, ChangeSet change, boolean force) {
		checkComplete(change);
		TransientChangeSet undo = change.revert();
		try (Transaction tx = kb.beginTransaction(undo.getMessage())) {
			List<ResKey> problems = new ArrayList<>(undo.apply());
			if (!problems.isEmpty() && !force) {
				tx.rollback(I18NConstants.UNDO_CANCELED_BY_USER);
				return problems;
			}
			tx.commit();
			return problems;
		}
	}

	/**
	 * Reverts all given {@link ChangeSet}s <strong>within the caller's active transaction</strong>.
	 *
	 * <p>
	 * The change sets are processed in descending revision order (newest first), so that each
	 * revert is applied on top of a state consistent with having already undone the later
	 * changes. Redo-of-revert semantics are <em>not</em> applied; callers that want them should
	 * follow {@link #revertTarget(ChangeSet)} before calling.
	 * </p>
	 *
	 * <p>
	 * This method does <strong>not</strong> open or commit a transaction. The caller is
	 * responsible for running it inside an active {@link Transaction} and deciding whether to
	 * commit or roll back based on the returned problem list.
	 * </p>
	 *
	 * @param changeSets
	 *        The change sets to undo. May be in any order and may be empty.
	 * @return Aggregated problems reported by the individual {@code apply()} calls. An empty list
	 *         means the revert was clean.
	 * @throws TopLogicException
	 *         If one of the change sets is {@link ChangeSet#getIncomplete() incomplete}, see
	 *         {@link #checkComplete(ChangeSet)}. Nothing has been reverted then.
	 */
	public static List<ResKey> revertAll(Collection<? extends ChangeSet> changeSets) {
		if (changeSets.isEmpty()) {
			return Collections.emptyList();
		}
		for (ChangeSet cs : changeSets) {
			checkComplete(cs);
		}

		List<ChangeSet> ordered = new ArrayList<>(changeSets);
		ordered.sort(Comparator
			.comparingLong((ChangeSet cs) -> cs.getRevision().getCommitNumber())
			.reversed());

		List<ResKey> problems = new ArrayList<>();
		for (ChangeSet cs : ordered) {
			problems.addAll(cs.revert().apply());
		}
		return problems;
	}

	/**
	 * Refuses to revert an {@link ChangeSet#getIncomplete() incomplete} change set.
	 *
	 * <p>
	 * An incomplete change set is a {@link ChangeSetRestriction restricted} copy of a commit that
	 * lacks the parts the user may not see. Its revert would undo only a part of the commit.
	 * </p>
	 *
	 * @param change
	 *        The change set to revert.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_REVERT_INCOMPLETE_CHANGE_SET__DATE}, if the change set
	 *         is incomplete.
	 */
	public static void checkComplete(ChangeSet change) throws TopLogicException {
		if (change.getIncomplete()) {
			throw new TopLogicException(I18NConstants.ERROR_REVERT_INCOMPLETE_CHANGE_SET__DATE.fill(change.getDate()));
		}
	}

	/**
	 * Refuses to revert a change set whose revert the given user is not allowed to perform.
	 *
	 * <p>
	 * Every change is checked as its revert executes it, on the current version of the changed
	 * object:
	 * </p>
	 * <ul>
	 * <li>The revert of an {@link Update} writes the old value of each modified attribute: the user
	 * needs the right to {@link SimpleBoundCommandGroup#WRITE write} each modified attribute of the
	 * object. A derived attribute is not written by the revert and not checked.</li>
	 * <li>The revert of a {@link Creation} deletes the object: the user needs the right to
	 * {@link SimpleBoundCommandGroup#DELETE delete} it.</li>
	 * <li>The revert of a {@link Deletion} creates the object again: the user needs the right to
	 * {@link ModelAccessRights#isAllowedCreate(Person, TLClass, TLObject) create} an object of its
	 * type in the container the object had before its deletion, without container in the global
	 * context.</li>
	 * </ul>
	 *
	 * <p>
	 * A creation or deletion of an object whose container is created or deleted by the same change
	 * set is not checked: it follows the change of its container, which is checked itself (or in
	 * turn follows the change of its own container), as deleting an object deletes its parts. Which
	 * changes the change set contains decides this, not {@link Change#getImplicit()}: a change set
	 * restricted by a {@link ChangeFilter} may lack the change of the container, then the change of
	 * the part is checked.
	 * </p>
	 *
	 * <p>
	 * An object that no longer exists is not checked, its revert is skipped and reported as a
	 * problem when the revert is applied.
	 * </p>
	 *
	 * <p>
	 * The check has no effect on the data. A caller checks before reverting, so that a revert is
	 * either applied completely or refused completely. A user bypassing the model security, e.g. an
	 * administrator, may revert every change set.
	 * </p>
	 *
	 * @param change
	 *        The change set to revert.
	 * @param user
	 *        The user reverting the change set.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_REVERT_NO_WRITE_ACCESS__OBJECT_ATTRIBUTE},
	 *         {@link I18NConstants#ERROR_REVERT_NO_DELETE_ACCESS__OBJECT} or
	 *         {@link I18NConstants#ERROR_REVERT_NO_CREATE_ACCESS__OBJECT_TYPE}, if the user lacks
	 *         a right for one of the changes.
	 */
	public static void checkWritable(ChangeSet change, Person user) throws TopLogicException {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Collection<? extends Change> changes = change.getChanges();
		Set<ObjectReference> created = identities(changes, Creation.class);
		Set<ObjectReference> deleted = identities(changes, Deletion.class);

		for (Change entry : changes) {
			if (entry instanceof Creation creation) {
				TLObject object = creation.getObject();
				if (followsContainer(object, created)) {
					continue;
				}
				TLObject current = currentVersion(object);
				if (current == null) {
					continue;
				}
				if (!rights.isAllowed(user, current, SimpleBoundCommandGroup.DELETE)) {
					throw new TopLogicException(I18NConstants.ERROR_REVERT_NO_DELETE_ACCESS__OBJECT.fill(current));
				}
			} else if (entry instanceof Deletion deletion) {
				TLObject object = deletion.getObject();
				if (object == null || !object.tValid() || followsContainer(object, deleted)) {
					continue;
				}
				TLClass type = (TLClass) object.tType();
				TLObject container = currentVersion(object.tContainer());
				if (!rights.isAllowedCreate(user, type, container)) {
					throw new TopLogicException(
						I18NConstants.ERROR_REVERT_NO_CREATE_ACCESS__OBJECT_TYPE.fill(object, type));
				}
			} else if (entry instanceof Update update) {
				TLObject current = currentVersion(update.getObject());
				if (current == null) {
					continue;
				}
				for (Modification modification : update.getModifications()) {
					TLStructuredTypePart part = modification.getPart();
					if (part.isDerived()) {
						continue;
					}
					if (!rights.isAllowed(user, current, part, SimpleBoundCommandGroup.WRITE)) {
						throw new TopLogicException(
							I18NConstants.ERROR_REVERT_NO_WRITE_ACCESS__OBJECT_ATTRIBUTE.fill(current, part));
					}
				}
			}
		}
	}

	/**
	 * The identities of the objects of the changes of the given kind.
	 */
	private static Set<ObjectReference> identities(Collection<? extends Change> changes,
			Class<? extends Change> kind) {
		Set<ObjectReference> result = new HashSet<>();
		for (Change entry : changes) {
			if (kind.isInstance(entry)) {
				TLObject object = entry.getObject();
				if (object != null && object.tValid()) {
					result.add(WrapperHistoryUtils.getUnversionedIdentity(object));
				}
			}
		}
		return result;
	}

	/**
	 * Whether the container of the given object is one of the given objects.
	 */
	private static boolean followsContainer(TLObject object, Set<ObjectReference> changed) {
		if (object == null || !object.tValid()) {
			return false;
		}
		TLObject container = object.tContainer();
		return container != null && changed.contains(WrapperHistoryUtils.getUnversionedIdentity(container));
	}

	/**
	 * The current version of the given object, <code>null</code> if it does not exist (anymore).
	 */
	private static TLObject currentVersion(TLObject object) {
		if (object == null || !object.tValid()) {
			return null;
		}
		return WrapperHistoryUtils.getCurrent(object);
	}

	/**
	 * Transactional wrapper around {@link #revertAll(Collection)}: opens a transaction on
	 * {@code kb}, runs the reverts and commits.
	 *
	 * @see #revertAll(Collection)
	 */
	public static List<ResKey> revertAll(KnowledgeBase kb, Collection<? extends ChangeSet> changeSets,
			ResKey transactionMessage) {
		if (changeSets.isEmpty()) {
			return Collections.emptyList();
		}
		List<ResKey> problems;
		try (Transaction tx = kb.beginTransaction(transactionMessage)) {
			problems = revertAll(changeSets);
			tx.commit();
		}
		return problems;
	}

	/**
	 * Rolls the composition subtree of {@code root} back to the state it had at
	 * {@code targetRevision}, by reverting every change that affected the subtree since
	 * {@code targetRevision} (exclusive).
	 *
	 * <p>
	 * Uses {@link SubtreeFilter} on the raw revision stream from
	 * {@link KnowledgeBase#getDiffReader(Revision, Branch, Revision, Branch, boolean)}. No
	 * revert/redo pairing and no entry-count limit are applied, so the full window between
	 * {@code targetRevision+1} and {@code HEAD} is considered.
	 * </p>
	 *
	 * @param root
	 *        The composition root whose subtree is to be rolled back.
	 * @param targetRevision
	 *        The revision whose state the subtree should be restored to. Changes on and before
	 *        this revision are kept; changes after it are undone.
	 * @param transactionMessage
	 *        The log message for the resulting transaction.
	 * @return Aggregated problems reported by the revert operations.
	 */
	public static List<ResKey> revertSubtreeTo(TLObject root, Revision targetRevision, ResKey transactionMessage) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		HistoryManager hm = kb.getHistoryManager();
		ModelTables modelTables = ElementModelCacheService.getModelTables();
		Branch trunk = hm.getTrunk();
		SubtreeFilter filter = new SubtreeFilter(root);

		// adjustStartRev may move the lower bound forward (to root's creation revision); that is
		// the earliest revision that could possibly contain subtree-relevant changes. The diff
		// reader treats startRev exclusively, so this produces events strictly after it.
		Revision effectiveStart = filter.adjustStartRev(targetRevision);
		Revision stopRev = hm.getRevision(hm.getLastRevision());

		List<ChangeSet> collected = new ArrayList<>();
		try (ChangeSetReader reader = kb.getDiffReader(effectiveStart, trunk, stopRev, trunk, true)) {
			while (true) {
				com.top_logic.knowledge.event.ChangeSet kbCS = reader.read();
				if (kbCS == null) {
					break;
				}

				ChangeSetAnalyzer analyzer =
					new ChangeSetAnalyzer(kb, modelTables, Collections.emptySet(), kbCS).setFilter(filter);
				if (!analyzer.hasChanges()) {
					continue;
				}

				Revision revision = hm.getRevision(kbCS.getRevision());
				TransientChangeSet entry =
					new TransientChangeSet(analyzer::applyChanges, TransientChangeSet.CHANGES_ATTR);
				entry.setRevision(revision);
				entry.setParentRev(HistoryUtils.getPreviousRevision(hm, kbCS.getRevision()));
				entry.setMessage(revision.getLog());
				entry.setDate(new Date(revision.getDate()));

				collected.add(entry);
			}
		}

		// Validate that ModelService still sees the same KB (sanity - avoids silent mis-routing).
		assert kb == ModelService.getApplicationModel().tKnowledgeBase();

		return revertAll(kb, collected, transactionMessage);
	}

	/**
	 * Locates the {@link ChangeSet} an {@code undo} operation should revert.
	 *
	 * <p>
	 * Returns the newest "real" change in the considered change log window that has not yet been
	 * reverted. "Real" means it is neither a revert nor a redo, and either has no pending revert
	 * or its revert has already been re-applied (redone). Returns {@code null} when no such
	 * change exists.
	 * </p>
	 *
	 * <p>
	 * Callers that need control over conflict handling (e.g. a
	 * {@link com.top_logic.tool.boundsec.CommandHandler} asking the user) use this method to
	 * identify the target and revert it with {@link #revert(KnowledgeBase, ChangeSet, boolean)}.
	 * For simple programmatic use cases, {@link #undoLast(TLObject, int, boolean)} wraps both
	 * steps.
	 * </p>
	 *
	 * @param root
	 *        Optional subtree root; {@code null} means the global change log.
	 * @param options
	 *        Common {@link ChangeLogOptions} restricting the considered change log (author, time
	 *        window, technical changes, excluded modules); {@code null} means no such
	 *        restrictions (all users, unlimited time).
	 * @param windowSize
	 *        How many of the most recent change log entries are inspected to find the undo
	 *        target; {@code <= 0} means no limit. Note this bounds the search, not the result.
	 * @param includeSubtree
	 *        When {@code root} is given: {@code true} considers the whole composition subtree;
	 *        {@code false} only {@code root} itself. Ignored when {@code root} is {@code null}.
	 */
	public static ChangeSet findUndoCandidate(TLObject root, ChangeLogOptions options, int windowSize,
			boolean includeSubtree) {
		return findLastRealChange(readLog(root, options, windowSize, includeSubtree));
	}

	/**
	 * Locates the revert {@link ChangeSet} a {@code redo} operation should re-apply.
	 *
	 * <p>
	 * Returns the revert with the highest commit number that is itself not yet redone. A redo is
	 * only valid when the trailing tail of the change log consists exclusively of undos/redos.
	 * If a regular forward change interrupted the undo stack, the method throws a
	 * {@link TopLogicException} ({@link I18NConstants#ERROR_CANNOT_REDO_CHAIN_BROKEN}). Returns
	 * {@code null} when no pending undo exists.
	 * </p>
	 *
	 * @see #findUndoCandidate(TLObject, ChangeLogOptions, int, boolean) for the parameter
	 *      semantics.
	 *
	 * @throws TopLogicException
	 *         when a pending undo exists but a newer regular change has broken the redo stack.
	 */
	public static ChangeSet findRedoCandidate(TLObject root, ChangeLogOptions options, int windowSize,
			boolean includeSubtree) {
		return findNewestPendingRevert(readLog(root, options, windowSize, includeSubtree));
	}

	/**
	 * The change set an {@code undo} operation of the given user reverts: the
	 * {@link #findUndoCandidate(TLObject, ChangeLogOptions, int, boolean) undo candidate},
	 * {@link ChangeSetRestriction restricted} to what the user may see.
	 *
	 * <p>
	 * If the user may not see all of the candidate, the result is
	 * {@link ChangeSet#getIncomplete() incomplete}, and {@link #revert(KnowledgeBase, ChangeSet, boolean)}
	 * refuses it: an undo must not change objects the user may not see.
	 * </p>
	 *
	 * @param user
	 *        The user undoing the change.
	 * @return The restricted candidate, {@code null} if there is none.
	 *
	 * @see #findUndoCandidate(TLObject, ChangeLogOptions, int, boolean) for the other parameters.
	 */
	public static ChangeSet findUndoTarget(TLObject root, ChangeLogOptions options, int windowSize,
			boolean includeSubtree, Person user) {
		return restrict(findUndoCandidate(root, options, windowSize, includeSubtree), user);
	}

	/**
	 * The change set a {@code redo} operation of the given user reverts: the
	 * {@link #findRedoCandidate(TLObject, ChangeLogOptions, int, boolean) redo candidate},
	 * {@link ChangeSetRestriction restricted} to what the user may see.
	 *
	 * @see #findUndoTarget(TLObject, ChangeLogOptions, int, boolean, Person)
	 * @throws TopLogicException
	 *         when a pending undo exists but a newer regular change has broken the redo stack.
	 */
	public static ChangeSet findRedoTarget(TLObject root, ChangeLogOptions options, int windowSize,
			boolean includeSubtree, Person user) {
		return restrict(findRedoCandidate(root, options, windowSize, includeSubtree), user);
	}

	private static ChangeSet restrict(ChangeSet candidate, Person user) {
		return candidate == null ? null : new ChangeSetRestriction(user).restrict(candidate);
	}

	/**
	 * Convenience wrapper around {@link #findUndoCandidate(TLObject, ChangeLogOptions, int, boolean)}
	 * that also opens a dedicated transaction with the correct revert commit message and commits.
	 *
	 * <p>
	 * No {@link ChangeLogOptions} restrictions are applied: changes of all users over the whole
	 * history are considered. A no-op (empty result) is returned when no change is eligible. For
	 * callers that need conflict-aware commit control, use
	 * {@link #findUndoCandidate(TLObject, ChangeLogOptions, int, boolean)} and
	 * {@link #revert(KnowledgeBase, ChangeSet, boolean)}.
	 * </p>
	 */
	public static List<ResKey> undoLast(TLObject root, int windowSize, boolean includeSubtree) {
		ChangeSet target = findUndoCandidate(root, null, windowSize, includeSubtree);
		if (target == null) {
			return Collections.emptyList();
		}
		return runSingleRevert(root, target);
	}

	/**
	 * Convenience wrapper around {@link #findRedoCandidate(TLObject, ChangeLogOptions, int, boolean)}
	 * that opens a dedicated transaction with the correct revert commit message and commits.
	 *
	 * @see #undoLast(TLObject, int, boolean)
	 * @throws TopLogicException
	 *         when a pending undo exists but a newer regular change has broken the redo stack.
	 */
	public static List<ResKey> redoLast(TLObject root, int windowSize, boolean includeSubtree) {
		ChangeSet target = findRedoCandidate(root, null, windowSize, includeSubtree);
		if (target == null) {
			return Collections.emptyList();
		}
		return runSingleRevert(root, target);
	}

	private static List<ResKey> runSingleRevert(TLObject root, ChangeSet target) {
		KnowledgeBase kb = root != null ? root.tKnowledgeBase() : PersistencyLayer.getKnowledgeBase();
		return revert(kb, target, true);
	}

	private static Collection<ChangeSet> readLog(TLObject root, ChangeLogOptions options, int windowSize,
			boolean includeSubtree) {
		KnowledgeBase kb = root != null ? root.tKnowledgeBase() : PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();
		ChangeLogBuilder builder = new ChangeLogBuilder(kb, model);
		if (options != null) {
			builder.applyOptions(options);
		}
		if (root != null) {
			builder.setFilter(new SubtreeFilter(root, includeSubtree));
		}
		if (windowSize > 0) {
			builder.setNumberEntries(windowSize);
		}
		Collection<ChangeSet> result = builder.build();
		return result;
	}

	private static ChangeSet findLastRealChange(Collection<ChangeSet> log) {
		// Log is ascending; the newest real change is the last one matching.
		ChangeSet result = null;
		for (ChangeSet cs : log) {
			if (cs.getRevertedBy() == null && !cs.isRevert()) {
				result = cs;
			}
		}
		return result;
	}

	private static ChangeSet findNewestPendingRevert(Collection<ChangeSet> log) {
		// Collect revisions of reverts that have been re-applied via a redo - those are no
		// longer pending.
		Set<Long> alreadyRedone = new HashSet<>();
		for (ChangeSet cs : log) {
			if (cs.isRedo()) {
				long origRev = cs.origRevision();
				if (origRev >= 0) {
					alreadyRedone.add(origRev);
				}
			}
		}

		// Determine:
		//   - the newest pending revert (candidate for redo), and
		//   - the newest revision of an effectively-active real change (something that is neither
		//     a revert nor a redo, and either was never reverted or whose revert has been redone).
		// A redo is only valid if the candidate revert is newer than any real change - otherwise
		// the redo stack has been invalidated by regular forward work.
		ChangeSet candidate = null;
		long candidateRev = Long.MIN_VALUE;
		long newestRealChangeRev = Long.MIN_VALUE;

		for (ChangeSet cs : log) {
			if (cs.isRevert() || cs.isRedo()) {
				// These are bookkeeping change sets, not real forward changes.
				continue;
			}
			long csRev = cs.getRevision().getCommitNumber();
			ChangeSet revert = cs.getRevertedBy();
			if (revert == null) {
				if (csRev > newestRealChangeRev) {
					newestRealChangeRev = csRev;
				}
				continue;
			}
			long revRev = revert.getRevision().getCommitNumber();
			if (alreadyRedone.contains(revRev)) {
				// The revert was undone - cs is effectively an active real change again.
				if (csRev > newestRealChangeRev) {
					newestRealChangeRev = csRev;
				}
			} else {
				if (revRev > candidateRev) {
					candidateRev = revRev;
					candidate = revert;
				}
			}
		}

		if (candidate == null) {
			// No pending undo; nothing to redo. Not an error condition.
			return null;
		}
		if (newestRealChangeRev > candidateRev) {
			// A real change happened after the newest pending undo - the redo stack was cleared
			// by that forward action, so redo is no longer meaningful.
			throw new TopLogicException(I18NConstants.ERROR_CANNOT_REDO_CHAIN_BROKEN)
				.initSeverity(ErrorSeverity.WARNING);
		}
		return candidate;
	}

}
