/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.sql.CommitContext;

/**
 * Use together with some {@link com.top_logic.knowledge.service.CommitHandler}.
 * 
 * <p>
 * Classes that implement this interface participate in the commit/rollback handling of the
 * providing container. Code that must run only after a successful commit registers an
 * {@link AfterCommitAction} through {@link CommitActions#afterCommit(AfterCommitAction)} instead.
 * </p>
 * 
 * <p>
 * Protocol: {@link #prepare(CommitContext)} and {@link #prepareDelete(CommitContext)} are called
 * first, {@link #commit(CommitContext)} is the last call before the database transaction is
 * committed. {@link #rollback(CommitContext)} is called exactly when the database transaction is not
 * committed - also after a successful {@link #commit(CommitContext)}, if a later step or the
 * database commit itself fails. After a successful database commit, {@link #rollback(CommitContext)}
 * is never called, even if publishing the changes in the running application fails afterwards.
 * </p>
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface Committable {

    /** Will be called to check if a potential commit will succeed.
     * 
     * @param aContext depends on container.  
     *
     * @return false to indicate failure. This will result in a rollback
     *         and an eventually call to rollback(), later.  
     */
    public boolean prepare(CommitContext aContext);

    /** Will be called to check if a potential delete will succeed.
     * 
     * @param aContext depends on container.  
     *
     * @return false to indicate failure. This will result in a rollback
     *         and an eventually call to rollback(), later.  
     */
    public boolean prepareDelete(CommitContext aContext);

    /**
	 * Finally commit the changes {@link #prepare(CommitContext) prepared} so far.
	 * 
	 * <p>
	 * This should make all changes necessary to provide a consistent state. This is the last call
	 * before the database transaction is committed. Since the database commit may still fail,
	 * {@link #rollback(CommitContext)} may be called after this method has returned
	 * <code>true</code>.
	 * </p>
	 * 
	 * @param aContext
	 *        depends on container.
	 *
	 * @return <code>false</code> to indicate failure. In that case, the ongoing transaction is
	 *         rolled back.
	 */
    public boolean commit(CommitContext aContext);

    /**
	 * Will be called to signal that the transaction is rolled back.
	 * 
	 * <p>
	 * This method is called exactly when the database transaction is not committed, also after
	 * {@link #commit(CommitContext)}, when the database commit itself fails. It is never called
	 * after a successful database commit.
	 * </p>
	 * 
	 * <p>
	 * This should place the object in the state just before it was added to the container, if
	 * possible.
	 * </p>
	 * 
     * @param aContext depends on container.  
     *
     * @return false to indicate that rollback failed, but expect
     *         this to be ignored anyway.
     */
    public boolean rollback(CommitContext aContext);

}
