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
	 * This should make all changes necessary to provide a consistent state.
	 * 
	 * @param aContext
	 *        depends on container.
	 *
	 * @return <code>false</code> to indicate failure. In that case, the ongoing transaction is
	 *         rolled back.
	 */
    public boolean commit(CommitContext aContext);

    /** Will be called to signal that the transaction is rolled back.
     * 
     * This should place the object in the state just before it
     * was added to the container, if possible.
     * 
     * @param aContext depends on container.  
     *
     * @return false to indicate that rollback failed, but expect
     *         this to be ignored anyway.
     */
    public boolean rollback(CommitContext aContext);

}
