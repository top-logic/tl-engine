/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.compact;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Compact history
	 */
	public static ResKey COMPACT_HISTORY_JSP_TITLE;

	/**
	 * @en <p>This page collapses all revisions committed before a given date into the newest
	 *     revision committed at or before that date. The state of that revision and the state of
	 *     every newer revision are preserved exactly, all intermediate states are discarded and
	 *     their rows are physically deleted.</p> <p><b>Attention:</b> The discarded history cannot
	 *     be restored. Create a database backup before running the operation.</p>
	 *     <p><b>Attention:</b> The operation requires an active maintenance window and, in a
	 *     cluster, that this node is the only active node.</p> <p><b>Attention:</b> After the
	 *     compaction the persistency layer is restarted, because a running knowledge base keeps
	 *     discarded revisions in its caches. All running sessions (including this one) are
	 *     terminated. The maintenance window stays active over the restart and has to be left
	 *     manually afterwards.</p> <p>The analysis only counts what a compaction would change and
	 *     is safe to run at any time.</p>
	 */
	public static ResKey COMPACT_HISTORY_JSP_DESCRIPTION;

	/**
	 * @en Compact history
	 */
	public static ResKey COMPACT_HISTORY_DIALOG_TITLE;

	/**
	 * @en Collapse the history into a single revision
	 */
	public static ResKey COMPACT_HISTORY_DIALOG_HEADER;

	/**
	 * @en Attention: The compaction needs an active maintenance window, discards the history
	 *     irrevocably and restarts the persistency layer afterwards, which terminates all sessions.
	 *     The maintenance window stays active over the restart and has to be left manually.
	 */
	public static ResKey COMPACT_HISTORY_DIALOG_MESSAGE;

	/**
	 * @en Compact history before
	 */
	public static ResKey BEFORE_DATE;

	/**
	 * @en Analyze
	 * @tooltip Counts what a compaction would change, without changing anything.
	 */
	public static ResKey ANALYZE_HISTORY;

	/**
	 * @en Compact
	 * @tooltip The history before the selected date is discarded. Existing data cannot be restored.
	 */
	public static ResKey COMPACT_HISTORY;

	/**
	 * @en Start compaction? The history before the selected date is discarded irrevocably. When the
	 *     report is closed, the persistency layer is restarted and all sessions are terminated.
	 */
	public static ResKey CONFIRM_COMPACT;

	/**
	 * @en The compaction requires an active maintenance window.
	 */
	public static ResKey ERROR_NO_MAINTENANCE_WINDOW;

	/**
	 * @en The compaction requires this node to be the only active node of the cluster, but {0}
	 *     nodes are active.
	 */
	public static ResKey1 ERROR_CLUSTER_NODES_ACTIVE__COUNT;

	/**
	 * @en The state of the cluster cannot be determined.
	 */
	public static ResKey ERROR_CLUSTER_STATE_UNAVAILABLE;

	/**
	 * @en The history of this application cannot be compacted, because its persistency layer does
	 *     not store the history in a database.
	 */
	public static ResKey ERROR_UNSUPPORTED_KNOWLEDGE_BASE;

	/**
	 * @en Closing this report restarts the persistency layer and ends your session. The maintenance
	 *     window stays active.
	 */
	public static ResKey COMPACTION_FINISHED;

	/**
	 * @en Analyzing the history failed.
	 */
	public static ResKey ERROR_ANALYSIS_FAILED;

	/**
	 * @en Compacting the history failed.
	 */
	public static ResKey ERROR_COMPACTION_FAILED;

	static {
		initConstants(I18NConstants.class);
	}
}
