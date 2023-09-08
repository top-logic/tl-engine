/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.sql.CommitContext;

/**
 * A CommitHandler is a container for 
 *  {@link com.top_logic.knowledge.service.Committable} objects.
 * 
 * This tries to support a complete way of handling Transactions
 * for commit()/rollback(). As of now this works only for very
 * special cases.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface CommitHandler {
    
    /** Convenient constant for {@link #getCommitContext(boolean)} */
    public static final boolean CREATE = true;

    /**
     * Add the given aCommitable to this container for the next commit().
     * 
     * Do not call during commit handling, result will be unpredictable.
     * 
     * @param aCommittable the Object to be committed on next commit().
     * 
     * @return true indicates that committable was added.
     *         false should only happen in case a committable is added twice.
     */
    public boolean addCommittable(Committable aCommittable);

    /**
     * Add the given aCommitable to this container for delete on next commit().
     * 
     * In case addCommitable() was called before removeCommitable() 
     * will be called (resp. a similar action taken)
     * 
     * Do not call during commit handling, result will be unpredictable.
     * 
     * @param aCommitable the Object to be commitDelted() on next commit().
     * 
     * @return true indicates that committable was added.
     *         false should only happen in case a committable is added twice.
     */
    public boolean addCommittableDelete(Committable aCommitable);

    /** The semantic of this function is unclear, don't use it. 
     * 
     * KHA why should someone call this function ?
     *     what happens when it is called during commit()/rollback() etc.
     *  
     * @return true when object was actually removed.
     */
    public boolean removeCommittable(Committable aCommittable);
    
    /** 
     * Return a context that is used to track the committables. 
     * 
     * @return an arbitrary object, must not be null.
	 * 
	 * @deprecated Use either {@link #getCurrentContext()} or {@link #createCommitContext()}.
     */
    @Deprecated
	public CommitContext getCommitContext(boolean create);

	/**
	 * Look up the current commit context.
	 * 
	 * @return the current {@link CommitContext}. <code>null</code>, if there a
	 *         context was not yet {@link #createCommitContext() created}.
	 *         
	 * @see #createCommitContext() Looking up or creating the context.
	 */
    public CommitContext getCurrentContext();

	/**
	 * Look up the current commit context or establish a new commit context, if
	 * there is no context yet.
	 * 
	 * @return the current or newly created {@link CommitContext}. Never
	 *         <code>null</code>.
	 * 
	 * @see #getCurrentContext() Looking up an existing context without creating
	 *      one, if none does exist so far.
	 */
    public CommitContext createCommitContext();

}
