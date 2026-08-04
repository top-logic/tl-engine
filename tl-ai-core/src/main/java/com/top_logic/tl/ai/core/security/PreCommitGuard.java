/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * The last gate before an agent turn's changes are persisted.
 *
 * <p>
 * Called from the agent loop immediately before the single per-turn commit, with the changes the
 * turn accumulated. This is the layer that catches tools that performed no check of their own -
 * remote MCP tools, TL-Script tools authored in the running application, and any future Java tool
 * whose author forgot. It is therefore the highest-value layer of the design: every other layer
 * assumes the tool cooperates, this one does not.
 * </p>
 *
 * <p>
 * On a violation the loop rolls the whole turn back - through the same path a failed tool call takes,
 * so that all-or-nothing means the same thing in both cases - audits each violation and tells the
 * model what happened, naming the operation and type but not the values.
 * </p>
 *
 * <p>
 * <b>Known limit.</b> {@code ModelAccessRights} deliberately allows every instance-level check on an
 * object created in the current, uncommitted transaction, because rule-derived roles are computed at
 * commit time. So the guard cannot validate a fresh object's own rights; what it validates for a
 * creation is the CREATE right on the type in the committed parent context plus the WRITE right on
 * the parent's composition attribute - exactly the check the engine performs. Updates and deletes of
 * pre-existing objects are checked in full. A parent that was itself created in the same turn is
 * equally unvalidatable, and is covered by its own creation being checked against <em>its</em>
 * container, up to the first committed ancestor.
 * </p>
 *
 * @see ToolChangeSet
 * @see ModelSecurityPreCommitGuard
 * @see ModelSecurity
 */
public interface PreCommitGuard {

	/** Accepts every change. The behaviour of {@link AiSecurityMode#LEGACY}. */
	PreCommitGuard PERMISSIVE = new PreCommitGuard() {

		@Override
		public boolean enforces() {
			return false;
		}

		@Override
		public List<Violation> check(SecurityContext security, ToolChangeSet changes) {
			return List.of();
		}
	};

	/**
	 * Verifies that the run's principal may persist every change the turn accumulated.
	 *
	 * <p>
	 * Called once per turn, only when a transaction is active - turns in which no write tool ran
	 * never open one, so the overwhelming majority of turns skip the guard entirely.
	 * </p>
	 *
	 * @param security
	 *        The run's security context, carrying the principal and the effective mode.
	 * @param changes
	 *        The changes accumulated in the current transaction.
	 * @return The violations found; empty means the commit may proceed.
	 */
	List<Violation> check(SecurityContext security, ToolChangeSet changes);

	/**
	 * Whether this guard checks anything at all.
	 *
	 * <p>
	 * Answered before the change set is built, so that a run which enforces nothing pays nothing:
	 * reading the engine's transaction bookkeeping is cheap but not free, and a turn that could not
	 * produce a violation must not be charged for it.
	 * </p>
	 */
	default boolean enforces() {
		return true;
	}

	/**
	 * The guard to use for a run with the given security context.
	 *
	 * <p>
	 * The one place the mode gate for this layer lives. A run whose mode does not
	 * {@link AiSecurityMode#enforcesModelAccess() enforce model access} - which is every run in an
	 * application that has configured no policy, and every {@link AiSecurityMode#SYSTEM} run - gets
	 * {@link #PERMISSIVE} and is not charged for the change set either.
	 * </p>
	 *
	 * @param security
	 *        The run's security context.
	 * @return The guard for this run, never <code>null</code>.
	 */
	static PreCommitGuard forRun(SecurityContext security) {
		if (security == null || !security.securityMode().enforcesModelAccess()) {
			return PERMISSIVE;
		}
		return new ModelSecurityPreCommitGuard();
	}

	/**
	 * What to tell the model about the given violations.
	 *
	 * <p>
	 * Names the operation and the type and counts the objects, and <b>never</b> a value or a label:
	 * the model must learn that its writes are gone and why, and must not learn the contents of data
	 * its principal may not touch. Counting per (operation, type) pair keeps the notice bounded when a
	 * turn touched a hundred objects.
	 * </p>
	 *
	 * @param violations
	 *        The violations found, not empty.
	 * @return One sentence per (operation, type) pair, safe to feed back to the model.
	 */
	static String describe(List<Violation> violations) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (Violation violation : violations) {
			String type = violation.typeName() == null ? "an unnamed type" : "'" + violation.typeName() + "'";
			counts.merge(violation.operation().getID() + " on objects of type " + type, 1, Integer::sum);
		}
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			if (result.length() > 0) {
				result.append("; ");
			}
			result.append("you attempted '").append(entry.getKey()).append("' for ")
				.append(entry.getValue())
				.append(entry.getValue() == 1 ? " object" : " objects")
				.append(" without permission");
		}
		return result.toString();
	}

	/**
	 * One change the run's principal was not entitled to make.
	 *
	 * @param object
	 *        The object that would have been changed.
	 * @param typeName
	 *        The qualified name of its type, or <code>null</code> if it has none. Resolved when the
	 *        violation is found, because a rolled-back object may no longer answer for its type
	 *        afterwards.
	 * @param operation
	 *        The operation that is not permitted.
	 * @param reason
	 *        A message safe for the audit log and for the model: names the operation and type, never
	 *        the value.
	 */
	record Violation(TLObject object, String typeName, BoundCommandGroup operation, String reason) {
		// Intentionally empty.
	}
}
