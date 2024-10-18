/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.element.layout.formeditor.FormDefinitionEditor;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormContextDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link FormProvider} that is configured with a customized form.
 */
public class SpecializedForm extends AbstractConfiguredInstance<SpecializedForm.Config<?>>
		implements FormProvider {

	/**
	 * Configuration options for {@link SpecializedForm}.
	 */
	public interface Config<I extends SpecializedForm> extends PolymorphicConfiguration<I>, FormContextDefinition {

		/** The custom form for the current process step. */
		FormDefinition getForm();

		/**
		 * @see #getForm()
		 */
		void setForm(FormDefinition formDefinition);

		/**
		 * @deprecated This property is never set. It must be declared, since a
		 *             {@link FormDefinition} property (see {@link #getForm()}) requires an owner
		 *             configuration to implement {@link FormContextDefinition}. However, the form
		 *             context type is computed dynamically in the {@link FormDefinitionEditor}
		 *             based on the surrounding {@link EditContext}, if the
		 *             {@link FormContextDefinition} does not provide a value.
		 */
		@Deprecated
		@Override
		TLModelPartRef getFormContextType();
	}

	/**
	 * Creates a {@link SpecializedForm} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SpecializedForm(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public FormDefinition getFormDefinition(Task self, TLStructuredType modelType) {
		return getConfig().getForm();
	}

}
