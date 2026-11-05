/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.component.state;

import java.util.Map;

import com.top_logic.bpe.bpml.BPMLDefinitions;
import com.top_logic.bpe.bpml.I18NConstants;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} disabling a command on a workflow ({@link Collaboration}) of which
 * processes were started, see {@link BPMLDefinitions#canDelete(Collaboration)}.
 *
 * <p>
 * Guards the deletion of a workflow: a process execution, running or finished, refers to its
 * workflow, which is revoked instead of deleted. Any other model is left to the other rules of the
 * command.
 * </p>
 */
public class WorkflowNotInUseRule implements ExecutabilityRule {

	/** Singleton {@link WorkflowNotInUseRule} instance. */
	public static final WorkflowNotInUseRule INSTANCE = new WorkflowNotInUseRule();

	/**
	 * Creates a {@link WorkflowNotInUseRule}.
	 */
	protected WorkflowNotInUseRule() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		if (model instanceof Collaboration workflow && workflow.tValid() && !BPMLDefinitions.canDelete(workflow)) {
			return ExecutableState.createDisabledState(I18NConstants.WORKFLOW_IN_USE);
		}
		return ExecutableState.EXECUTABLE;
	}

}
