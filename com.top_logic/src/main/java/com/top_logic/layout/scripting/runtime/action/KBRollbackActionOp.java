/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.scripting.action.KBRollbackAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ApplicationActionOp} that reverts the {@link KnowledgeBase} to a certain revision.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class KBRollbackActionOp<C extends KBRollbackAction> extends AbstractApplicationActionOp<C> {

	/**
	 * Creates a new {@link KBRollbackActionOp}.
	 */
	public KBRollbackActionOp(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Branch currentBranch = HistoryUtils.getContextBranch();
		Revision stopRevision = getStopRevision(context);
		if (stopRevision != Revision.CURRENT) {
			KBUtils.revert(kb, stopRevision, currentBranch);
		}
		return argument;
	}

	/**
	 * The revision to which the {@link KnowledgeBase} should be reverted.
	 */
	protected abstract Revision getStopRevision(ActionContext context);

}
