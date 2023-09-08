/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import com.top_logic.basic.sql.CommitContext;
import com.top_logic.knowledge.service.Committable;

/**
 * A {@link Committable} that will not like a {@link #commit(CommitContext)}
 * 
 * @author <a href="mailto:klaus.halfmanna@top-logic.com">Klaus Halfmann/a>
 */
public class TestingCommittable implements Committable {

    /** Decide if prepares will be allowed */
	public boolean allowPrepare = true;

    /** Decide if prepareDelete will be allowed */
	public boolean allowDelete = true;

    /** Decide if commits will be allowed */
	public boolean allowCommit = true;

	/** Decide if rollback will be allowed */
	public boolean allowRollback = true;

	/** log that a {@link #prepare(CommitContext)} happened */
	public boolean wasPrepared = false;

	/** log that a {@link #prepareDelete(CommitContext)} happened */
	public boolean wasDeleted = false;

	/** log that a {@link #commit(CommitContext)} happened */
	public boolean wasCommited = false;

	/** log that a {@link #rollback(CommitContext)} happened */
	public boolean wasRolledBack = false;

	/** log that a {@link #complete(CommitContext)} happened */
	public boolean wasCompleted = false;

    /** Setup TestingCommittable with flags as desired for testing */
    public TestingCommittable(
        boolean allowPrep, boolean allowDel, boolean allowCom, boolean allowRol) {

        allowPrepare  = allowPrep;
        allowDelete   = allowDel;
        allowCommit   = allowCom;
        allowRollback = allowRol;
    }

	/** So logging makes some sense */
    @Override
	public String toString() {
		return getClass().getSimpleName() + ' '
				+ allowPrepare + ' '
				+ allowDelete + ' '
				+ allowCommit + ' '
              + allowRollback;
    }

    /**
     * Fake a prepare
     */
    @Override
	public boolean prepare(CommitContext aContext) {
        wasPrepared = true;
        return allowPrepare;
    }

    /**
     * Fake a commit
     */
    @Override
	public boolean commit(CommitContext aContext) {
        wasCommited = true;
        return allowCommit;
    }

    /**
     * Fake and log a rollback.
     */
    @Override
	public boolean rollback(CommitContext aContext) {
        wasRolledBack = true;
        return allowRollback;
    }

    /**
     * Fake and log a prepareDelete.
     */
    @Override
	public boolean prepareDelete(CommitContext aContext) {
        wasDeleted = true;
        return allowDelete;
    }

    /**
     * Note that complete was called.
     */
    @Override
	public void complete(CommitContext aContext) {
        wasCompleted = true;
    }

}