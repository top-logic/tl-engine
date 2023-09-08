/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.scripting.action.KBRollbackXRevisions;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * A {@link KBRollbackActionOp} that rollsback the last X revisions.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class KBRollbackXRevisionsOp extends KBRollbackActionOp<KBRollbackXRevisions> {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link KBRollbackXRevisionsOp}.
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public KBRollbackXRevisionsOp(InstantiationContext context, KBRollbackXRevisions config) {
		super(context, config);
	}

	@Override
	protected Revision getStopRevision(ActionContext context) {
		long revertRevisions = Math.abs(config.getNumberRevisions());
		if (revertRevisions == 0) {
			return Revision.CURRENT;
		}
		long lastRevision = HistoryUtils.getLastRevision().getCommitNumber();
		long targetRevision = lastRevision - revertRevisions;
		return HistoryUtils.getRevision(targetRevision);
	}

}
