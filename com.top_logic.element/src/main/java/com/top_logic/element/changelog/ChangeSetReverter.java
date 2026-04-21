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

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.element.model.cache.ElementModelCacheService;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Technical (non-UI) utility that reverts one or more model {@link ChangeSet}s.
 *
 * <p>
 * Unlike {@code RevertCommandHandler}, this class operates without any UI
 * state, selection, or revert/redo pairing. It performs the raw model-level
 * undo: for every given {@link ChangeSet} it applies {@link ChangeSet#revert()
 * its inverse} in descending revision order, within a single transaction.
 * </p>
 *
 * <p>
 * Intended primarily as a building block for higher-level operations such as
 * {@link #revertSubtreeTo(TLObject, Revision, ResKey) subtree rollback}.
 * </p>
 */
public final class ChangeSetReverter {

	private ChangeSetReverter() {
		// utility
	}

	/**
	 * Reverts all given {@link ChangeSet}s <strong>within the caller's active transaction</strong>.
	 *
	 * <p>
	 * The change sets are processed in descending revision order (newest first), so that each
	 * revert is applied on top of a state consistent with having already undone the later
	 * changes. Redo-of-revert semantics are <em>not</em> applied; callers that want them should
	 * follow {@code getRevertedBy()} before calling.
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
	 */
	public static List<ResKey> revertAll(Collection<? extends ChangeSet> changeSets) {
		if (changeSets.isEmpty()) {
			return Collections.emptyList();
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
				entry.setParentRev(hm.getRevision(kbCS.getRevision() - 1));
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
	 * Callers that need fine-grained control over transaction and conflict handling (e.g. a
	 * {@link com.top_logic.tool.boundsec.CommandHandler}) use this method to identify the target
	 * and then open their own transaction with {@code target.revert().getMessage()} as the commit
	 * message. For simple programmatic use cases, {@link #undoLast(TLObject, int, boolean)}
	 * wraps both steps.
	 * </p>
	 *
	 * @param root
	 *        Optional subtree root; {@code null} means the global change log.
	 * @param windowSize
	 *        How many of the most recent change log entries are inspected to find the undo
	 *        target; {@code <= 0} means no limit. Note this bounds the search, not the result.
	 * @param includeSubtree
	 *        When {@code root} is given: {@code true} considers the whole composition subtree;
	 *        {@code false} only {@code root} itself. Ignored when {@code root} is {@code null}.
	 */
	public static ChangeSet findUndoCandidate(TLObject root, int windowSize, boolean includeSubtree) {
		return findLastRealChange(readLog(root, windowSize, includeSubtree));
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
	 * @see #findUndoCandidate(TLObject, int, boolean) for the parameter semantics.
	 *
	 * @throws TopLogicException
	 *         when a pending undo exists but a newer regular change has broken the redo stack.
	 */
	public static ChangeSet findRedoCandidate(TLObject root, int windowSize, boolean includeSubtree) {
		return findNewestPendingRevert(readLog(root, windowSize, includeSubtree));
	}

	/**
	 * Convenience wrapper around {@link #findUndoCandidate(TLObject, int, boolean)} that also
	 * opens a dedicated transaction with the correct revert commit message and commits.
	 *
	 * <p>
	 * A no-op (empty result) is returned when no change is eligible. For callers that need
	 * conflict-aware commit control, use {@link #findUndoCandidate(TLObject, int, boolean)}
	 * directly and manage the transaction themselves.
	 * </p>
	 */
	public static List<ResKey> undoLast(TLObject root, int windowSize, boolean includeSubtree) {
		ChangeSet target = findUndoCandidate(root, windowSize, includeSubtree);
		if (target == null) {
			return Collections.emptyList();
		}
		return runSingleRevert(root, target);
	}

	/**
	 * Convenience wrapper around {@link #findRedoCandidate(TLObject, int, boolean)} that opens a
	 * dedicated transaction with the correct revert commit message and commits.
	 *
	 * @see #undoLast(TLObject, int, boolean)
	 * @throws TopLogicException
	 *         when a pending undo exists but a newer regular change has broken the redo stack.
	 */
	public static List<ResKey> redoLast(TLObject root, int windowSize, boolean includeSubtree) {
		ChangeSet target = findRedoCandidate(root, windowSize, includeSubtree);
		if (target == null) {
			return Collections.emptyList();
		}
		return runSingleRevert(root, target);
	}

	private static List<ResKey> runSingleRevert(TLObject root, ChangeSet target) {
		KnowledgeBase kb = root != null ? root.tKnowledgeBase() : PersistencyLayer.getKnowledgeBase();
		TransientChangeSet undo = target.revert();
		try (Transaction tx = kb.beginTransaction(undo.getMessage())) {
			List<ResKey> problems = new ArrayList<>(undo.apply());
			tx.commit();
			return problems;
		}
	}

	private static Collection<ChangeSet> readLog(TLObject root, int windowSize, boolean includeSubtree) {
		KnowledgeBase kb = root != null ? root.tKnowledgeBase() : PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();
		ChangeLogBuilder builder = new ChangeLogBuilder(kb, model);
		if (root != null) {
			builder.setFilter(new SubtreeFilter(root, includeSubtree));
		}
		if (windowSize > 0) {
			builder.setNumberEntries(windowSize);
		}
		@SuppressWarnings("unchecked")
		Collection<ChangeSet> result = (Collection<ChangeSet>) builder.build();
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
			throw new TopLogicException(I18NConstants.ERROR_CANNOT_REDO_CHAIN_BROKEN);
		}
		return candidate;
	}

}
