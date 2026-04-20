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
import java.util.List;

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

		// Validate that ModelService still sees the same KB (sanity — avoids silent mis-routing).
		assert kb == ModelService.getApplicationModel().tKnowledgeBase();

		return revertAll(kb, collected, transactionMessage);
	}

	/**
	 * Undoes the newest real change that has not yet been reverted.
	 *
	 * <p>
	 * "Real" means the change is neither a revert nor has itself been reverted. If no such change
	 * exists within the considered window, the method is a no-op and returns an empty list. The
	 * revert is performed in the caller's active transaction — the method does not open or commit
	 * a transaction.
	 * </p>
	 *
	 * @param root
	 *        Optional subtree root; {@code null} means the global change log.
	 * @param maxEntries
	 *        Upper bound on the change log window inspected; {@code <= 0} means unlimited.
	 * @param includeSubtree
	 *        When {@code root} is given: {@code true} considers the whole composition subtree;
	 *        {@code false} only {@code root} itself. Ignored when {@code root} is {@code null}.
	 * @return Aggregated problems from the revert; empty when no change was eligible or the revert
	 *         was clean.
	 */
	public static List<ResKey> undoLast(TLObject root, int maxEntries, boolean includeSubtree) {
		ChangeSet target = findLastRealChange(readLog(root, maxEntries, includeSubtree));
		if (target == null) {
			return Collections.emptyList();
		}
		return revertAll(Collections.singletonList(target));
	}

	/**
	 * Re-applies the most recent undo within the considered change log window.
	 *
	 * <p>
	 * Concretely: the original change whose revert has the largest commit number is located; the
	 * revert ChangeSet itself is reverted, which restores the original change. No transaction is
	 * opened or committed.
	 * </p>
	 *
	 * @see #undoLast(TLObject, int, boolean) for the parameter semantics.
	 */
	public static List<ResKey> redoLast(TLObject root, int maxEntries, boolean includeSubtree) {
		ChangeSet revertToUndo = findNewestRevert(readLog(root, maxEntries, includeSubtree));
		if (revertToUndo == null) {
			return Collections.emptyList();
		}
		return revertAll(Collections.singletonList(revertToUndo));
	}

	private static Collection<ChangeSet> readLog(TLObject root, int maxEntries, boolean includeSubtree) {
		KnowledgeBase kb = root != null ? root.tKnowledgeBase() : PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();
		ChangeLogBuilder builder = new ChangeLogBuilder(kb, model);
		if (root != null) {
			builder.setFilter(new SubtreeFilter(root, includeSubtree));
		}
		if (maxEntries > 0) {
			builder.setNumberEntries(maxEntries);
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

	private static ChangeSet findNewestRevert(Collection<ChangeSet> log) {
		ChangeSet newest = null;
		long newestRev = Long.MIN_VALUE;
		for (ChangeSet cs : log) {
			ChangeSet revert = cs.getRevertedBy();
			if (revert == null) {
				continue;
			}
			long rev = revert.getRevision().getCommitNumber();
			if (rev > newestRev) {
				newestRev = rev;
				newest = revert;
			}
		}
		return newest;
	}

}
