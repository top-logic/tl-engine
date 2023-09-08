/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.List;

import com.top_logic.dob.MetaObject;
import com.top_logic.model.TLObject;

/**
 * Representation of branches in the persistency layer.
 * 
 * <p>
 * In contrast to a {@link Revision}, a branch is a <b>mutable</b> consistent
 * state of the knowledge base. After system setup, there is exactly one branch
 * named {@link HistoryManager#getTrunk()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Branch extends TLObject {
	
	/**
	 * Unique identifier of this branch.
	 */
	long getBranchId();

	/**
	 * The {@link Revision} that created this {@link Branch}.
	 */
	Revision getCreateRevision();
	
	/**
	 * The {@link Branch}, from which this branch was spawned.
	 * 
	 * <p>
	 * Not <code>null</code> for all {@link Branch}es except
	 * {@link HistoryManager#getTrunk()}.
	 * </p>
	 */
	Branch getBaseBranch();
	
	/**
	 * List of all {@link Branch}es that were spawned from this {@link Branch}.
	 */
	List<Branch> getChildBranches();
	
	/**
	 * The {@link Revision} of {@link #getBaseBranch()} from which this
	 * {@link Branch} was spawned.
	 */
	Revision getBaseRevision();

	/**
	 * The branch ID of the branch that stores the date for the given type.
	 */
	long getBaseBranchId(MetaObject metaObject);

	/**
	 * The {@link HistoryManager} who has created this branch.
	 */
	HistoryManager getHistoryManager();

}
