/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.bpe.bpml.model.ManualTask;

/**
 * Configuration value that describes the form to display the process model to an actor of a process
 * step.
 * 
 * <p>
 * This configuration type is used as type of an attribute of {@link ManualTask} to define the form
 * for this task.
 * </p>
 */
public interface ProcessFormDefinition extends ConfigPart {

	/**
	 * The algorithm creating the form.
	 */
	PolymorphicConfiguration<? extends FormProvider> getFormProvider();

	/**
	 * @see #getFormProvider()
	 */
	void setFormProvider(PolymorphicConfiguration<? extends FormProvider> value);

}
