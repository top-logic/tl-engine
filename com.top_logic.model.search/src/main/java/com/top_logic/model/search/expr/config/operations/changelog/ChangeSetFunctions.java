/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.changelog;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.changelog.ChangeSetReverter;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions on the commits ({@link ChangeSet}s) of the change log, as delivered by the
 * function {@code changeLog()}.
 */
@ScriptPrefix("changeSet")
public class ChangeSetFunctions extends TLScriptFunctions {

	/**
	 * Whether the given commit is undone in the current state.
	 *
	 * <p>
	 * Every revert following a commit toggles it: a commit that has been reverted is undone, one
	 * whose revert has been reverted again is in effect, and so on. This holds for an undo or a redo
	 * listed in the change log as well: an undo that has been reverted by a redo is undone itself. A
	 * command undoing a commit is offered for <code>!changeSetUndone($commit)</code>, one redoing it
	 * for <code>changeSetUndone($commit)</code>.
	 * </p>
	 *
	 * <p>
	 * The reverts are those linked by the change log the commit was read from.
	 * </p>
	 *
	 * @param changeSet
	 *        The commit to check.
	 * @return Whether the commit is undone; <code>false</code> for no commit.
	 */
	@Label("Commit is undone")
	@SideEffectFree
	public static boolean undone(@Mandatory ChangeSet changeSet) {
		return ChangeSetReverter.isUndone(changeSet);
	}

}
