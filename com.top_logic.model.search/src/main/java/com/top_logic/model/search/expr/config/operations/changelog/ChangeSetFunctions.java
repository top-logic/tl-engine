/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.changelog;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.changelog.ChangeSetRestriction;
import com.top_logic.element.changelog.ChangeSetReverter;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.TLContext;

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

	/**
	 * A copy of the given commit with exactly the changes the given user may see.
	 *
	 * <p>
	 * A change is kept if the user may read the changed object, the change of an attribute only if
	 * the user may read the attribute. Objects in the old and new values of an attribute are kept if
	 * the user may discover them, the author only if the user may discover the author's account.
	 * Revision, date and message of the commit are kept. A commit without a visible change becomes
	 * an empty commit.
	 * </p>
	 *
	 * <p>
	 * If a change, the change of an attribute or an object in a value was removed, the copy is
	 * marked as incomplete (<code>tl.changelog:ChangeSet#incomplete</code>) and cannot be reverted.
	 * A hidden author alone does not make the copy incomplete.
	 * </p>
	 *
	 * <p>
	 * An application showing the change log to a user who is no administrator reads it with
	 * <code>changeLog()</code> in a privileged script and restricts every commit:
	 * <code>changeLog(...).map(cs -&gt; changeSetRestrict($cs, currentUser()))</code>.
	 * </p>
	 *
	 * @param changeSet
	 *        The commit to restrict.
	 * @param user
	 *        The user whose access rights decide what is kept, the current user by default.
	 * @return The restricted copy of the commit.
	 */
	@Label("Restrict commit to visible changes")
	@SideEffectFree
	public static ChangeSet restrict(@Mandatory ChangeSet changeSet, Person user) {
		return new ChangeSetRestriction(user == null ? TLContext.currentUser() : user).restrict(changeSet);
	}

}
