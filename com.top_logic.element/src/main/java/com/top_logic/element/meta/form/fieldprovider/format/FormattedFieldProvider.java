/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider.format;

import java.text.Format;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * Configurable {@link FieldProvider} that can be parameterised with a {@link FormatProvider} for
 * editing arbitrary application-defined objects in a custom string representation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormattedFieldProvider<C extends FormattedFieldProvider.Config<?>> extends AbstractFieldProvider
		implements ConfiguredInstance<C> {

	private C _config;

	/**
	 * Configuration options for {@link FormattedFieldProvider}.
	 */
	public interface Config<I extends FormattedFieldProvider<?>> extends PolymorphicConfiguration<I> {
		/**
		 * The {@link FormatProvider} to use for serializing and parsing the application-defined
		 * object.
		 */
		@InstanceFormat
		@DefaultContainer
		FormatProvider getFormat();

		/**
		 * {@link ControlProvider} to display the field.
		 */
		@InstanceFormat
		ControlProvider getControlProvider();

	}

	/**
	 * Creates a {@link FormattedFieldProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FormattedFieldProvider(InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON : null;

		Format format;
		C config = getConfig();
		try {
			format = config.getFormat().createFormat();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		ComplexField result =
			FormFactory.newComplexField(fieldName, format, null, true, isMandatory, isDisabled, mandatoryChecker);
		result.setControlProvider(config.getControlProvider());
		return result;
	}

}
