/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import com.top_logic.knowledge.service.Branch;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Reference to {@link Branch} objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BranchName extends ModelName {

	/**
	 * The {@link Branch#getBranchId() ID} of the referenced {@link Branch} object.
	 * 
	 * @see Branch#getBranchId()
	 */
	long getBranchId();

	/** @see #getBranchId() */
	void setBranchId(long branchId);

}
