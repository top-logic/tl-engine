/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.Set;

import com.top_logic.knowledge.service.Branch;

/**
 * Specification of the {@link Branch branches} to search in an {@link AbstractQuery}.
 * 
 * @see AbstractQuery#getBranchParam()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum BranchParam {
	
	/**
	 * Search within a single {@link Branch branch}.
	 * 
	 * @see QueryArguments#setRequestedBranch(Branch)
	 */
	single, 
	
	/**
	 * Search within a set of {@link Branch branches}.
	 * 
	 * @see QueryArguments#setRequestedBranches(Set)
	 * 
	 * @deprecated Not yet implemented.
	 */
	@Deprecated
	set,

	/**
	 * Search within all {@link Branch branches} excluding a given set of branches.
	 * 
	 * @see QueryArguments#setRequestedBranches(Set)
	 * 
	 * @deprecated Not yet implemented.
	 */
	@Deprecated
	without,

	/**
	 * Search within all {@link Branch branches} of the system.
	 */
	all,

	;

	/**
	 * Throws an {@link AssertionError} that the given {@link BranchParam} does not exists.
	 */
	public static AssertionError unknownRangeParam(BranchParam branchParam) {
		return new AssertionError("Unknown " + BranchParam.class.getName() + ": " + branchParam);
	}

}
