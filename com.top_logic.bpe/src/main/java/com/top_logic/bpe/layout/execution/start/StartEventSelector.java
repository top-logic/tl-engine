/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution.start;

import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Selector of the process {@link StartEvent} for starting a process.
 * 
 * @see com.top_logic.bpe.layout.execution.ProcessExecutionCreateComponent.Config#getStartEvent()
 */
public interface StartEventSelector {

	/**
	 * Retrieves the process {@link StartEvent} from a component model.
	 */
	StartEvent getStartEvent(LayoutComponent component);

}
