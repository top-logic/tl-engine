/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution.init;

import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Algorithm initializing a new {@link ProcessExecution} immediately after creation.
 */
public interface ProcessInitializer {

	/**
	 * Initializes the given process execution with values from this creation context.
	 * 
	 * @param component
	 *        The create component.
	 * @param model
	 *        The target model of the create command.
	 * @param processExecution
	 *        The process-enabled object just created.
	 */
	void initialize(LayoutComponent component, Object model, ProcessExecution processExecution);

}
