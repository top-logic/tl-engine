/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * {@link FrameworkInternal} interface version of {@link Branch} with methods returning identifier
 * of {@link Revision} and {@link Branch} instead of the object itself.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public interface InternalBranch {

	/**
	 * {@link Revision#getCommitNumber() Commit number} of {@link Branch#getBaseRevision()}.
	 */
	long baseRevision();

	/**
	 * {@link Revision#getCommitNumber() Commit number} of {@link Branch#getCreateRevision()}.
	 */
	long createRevision();

	/**
	 * {@link Branch#getBranchId() Branch id} of {@link Branch#getBaseBranch()}.
	 * <p>
	 * Return 0 when no base branch is given.
	 * </p>
	 */
	long baseBranch();

	/**
	 * Value of {@link Branch#getBranchId()}.
	 */
	long branchId();

}

