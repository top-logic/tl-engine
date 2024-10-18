/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * Algorithm retrieving the form to display a {@link ProcessExecution} in some process step.
 */
@Label("Task form definition")
public interface FormProvider {

	/**
	 * The form to display the given {@link ProcessExecution} with.
	 * 
	 * @param self
	 *        The {@link Task} to compute the from definition for.
	 * @param modelType
	 *        The type of the process model.
	 */
	FormDefinition getFormDefinition(Task self, TLStructuredType modelType);

}
