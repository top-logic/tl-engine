/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.util.Resources;

/**
 * Setting a value of a {@link FormField} using its raw value from the UI.
 * 
 * @see FormInput Setting the value using the application value of the field.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormRawInput extends AbstractFormInput {

	@Override
	@ClassDefault(Op.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * Default implementation of {@link FormRawInput}.
	 */
	class Op extends AbstractApplicationActionOp<FormRawInput> {

		/**
		 * Creates a {@link FormRawInput.Op} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Op(InstantiationContext context, FormRawInput config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			try {
				AbstractFormField field =
					(AbstractFormField) ModelResolver.locateModel(context, getConfig().getField());
				if (!field.isActive()) {
					String label =
						field.hasLabel() ? "'" + Resources.getInstance().getString(field.getLabel()) + "' " : "";
					ApplicationAssertions.fail(getConfig(), "Field " + label + "not active, mode: "
						+ field.getMode().getExternalName());
				}
				Object value = ModelResolver.locateModel(context, field, getConfig().getValue());
				FormFieldInternals.updateField(field, value);
			} catch (VetoException ex) {
				// No user interaction during script-replay.
				ApplicationAssertions.fail(getConfig(), "Field update failed with veto.", ex);
			}
			return argument;
		}
	}

}
