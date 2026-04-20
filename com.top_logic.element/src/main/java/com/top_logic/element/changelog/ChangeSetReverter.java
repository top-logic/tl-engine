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

}
