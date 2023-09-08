/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.layout.scripting.recorder.ref.value.BranchName;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for technical {@link Branch} objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BranchNamingScheme extends AbstractModelNamingScheme<Branch, BranchName> {

	@Override
	public Class<BranchName> getNameClass() {
		return BranchName.class;
	}

	@Override
	public Class<Branch> getModelClass() {
		return Branch.class;
	}

	@Override
	public Branch locateModel(ActionContext context, BranchName name) {
		return HistoryUtils.getBranch(name.getBranchId());
	}

	@Override
	protected void initName(BranchName name, Branch model) {
		name.setBranchId(model.getBranchId());
	}

}
