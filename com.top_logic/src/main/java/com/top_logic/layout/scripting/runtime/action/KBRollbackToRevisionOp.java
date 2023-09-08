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
import com.top_logic.layout.scripting.action.KBRollbackToRevision;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * A {@link KBRollbackActionOp} that rollsback to the named revision.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class KBRollbackToRevisionOp extends KBRollbackActionOp<KBRollbackToRevision> {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link KBRollbackToRevisionOp}.
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public KBRollbackToRevisionOp(InstantiationContext context, KBRollbackToRevision config) {
		super(context, config);
	}

	@Override
	protected Revision getStopRevision(ActionContext context) {
		ModelName revisionRef = getConfig().getRevision();
		Revision targetRevision = (Revision) context.resolve(revisionRef);
		if (targetRevision == null) {
			throw ApplicationAssertions.fail(getConfig(), "Revision not found: " + revisionRef + " Maximum revision: "
				+ HistoryUtils.getLastRevision().getCommitNumber());
		}
		return targetRevision;
	}

}
