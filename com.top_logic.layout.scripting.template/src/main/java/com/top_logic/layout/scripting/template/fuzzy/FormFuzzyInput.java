/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.fuzzy;

import java.text.DateFormat;
import java.text.Format;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.scripting.action.AbstractFormAction;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Updating a {@link FormField} with a value formatted in a scripting-only format.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormFuzzyInput extends AbstractFormAction {

	@Override
	@ClassDefault(Op.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * Formatted value.
	 * 
	 * @see FlexibleFormatKind
	 */
	String getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(String value);

	/**
	 * Default operation of {@link FormFuzzyInput}.
	 */
	class Op extends AbstractApplicationActionOp<FormFuzzyInput> {

		/**
		 * Creates a {@link FormFuzzyInput.Op} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Op(InstantiationContext context, FormFuzzyInput config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			AbstractFormField field = (AbstractFormField) ModelResolver.locateModel(context, getConfig().getField());
			String value = getConfig().getValue();

			if (field instanceof BooleanField) {
				Object parsedValue = FlexibleFormatKind.BOOLEAN.parseValue(value);
				field.setValue(parsedValue);
			} else if (field instanceof ComplexField) {
				ComplexField withFormat = (ComplexField) field;
				Format fieldFormat = withFormat.getFormat();
				if (fieldFormat instanceof DateFormat) {
					Object fuzzyDate = FlexibleFormatKind.DATE.parseValue(value);
					String rawValue = fieldFormat.format(fuzzyDate);
					updateField(field, rawValue);
				}
			} else {
				ApplicationAssertions.fail(getConfig(), "Field of unsupported type (not boolean or Date): " + field);
			}
			return argument;
		}

		private void updateField(AbstractFormField field, String value) {
			try {
				FormFieldInternals.updateField(field, value);
			} catch (VetoException ex) {
				// No user interaction during script-replay.
				ApplicationAssertions.fail(getConfig(), "Field update failed with veto.", ex);
			}
		}

	}

}
