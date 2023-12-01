/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.model.form.definition.FormContextDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Configuration interface for the {@link FormEditActionOp} in the scripting framework.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public interface FormEditAction extends ApplicationAction, FormContextDefinition {

	/** Configuration name for the value of the {@link #getManualTask()}. */
	String MANUAL_TASK_NAME = "manual-task";

	/** Configuration name for the value of the {@link #getFormDefinition()}. */
	String FORM_DEFINITION_NAME = "form-definition";

	@Override
	@ClassDefault(FormEditActionOp.class)
	Class<FormEditActionOp> getImplementationClass();

	/**
	 * The {@link ManualTask} where the {@link FormDefinition} is attached to.
	 */
	@Name(MANUAL_TASK_NAME)
	ModelName getManualTask();

	/** @see #getManualTask() */
	void setManualTask(ModelName manualTask);

	/**
	 * The {@link FormDefinition} of the {@link ManualTask}.
	 */
	@Name(FORM_DEFINITION_NAME)
	FormDefinition getFormDefinition();

	/** @see #getFormDefinition() */
	void setFormDefinition(FormDefinition formDefinition);

	// Make it non-abstract, required context type for FormDefinition configurations.
	@Override
	@NullDefault
	TLModelPartRef getFormContextType();
}