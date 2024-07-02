/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.builder;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.FormBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.model.TLType;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * {@link FormBuilder} creating a {@link FormContext} showing a selection of all available
 * attributes of the business model type determined by a customized {@link FormDefinition}.
 * 
 * <p>
 * The following priority hierarchy is used to get the {@link FormDefinition}:
 * </p>
 * 
 * <ol>
 * <li>Configured {@link FormDefinition} on this builder.</li>
 * <li>First annotated {@link TLFormDefinition} on a super type for the current model type.</li>
 * <li>Fallback: All attributes are displayed.</li>
 * </ol>
 * 
 * @see FormDefinition
 * @see TLFormDefinition
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class ConfiguredDynamicFormBuilder
		implements ModelBuilder, ConfiguredInstance<ConfiguredDynamicFormBuilder.Config> {

	/**
	 * Configuration for a dynamic {@link FormBuilder}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface Config extends PolymorphicConfiguration<ModelBuilder>, FormsTemplateParameter {
		// Nothing needed except the combination of the super types.
	}

	private final Config _config;

	private Map<TLType, FormDefinition> _configuredForms;

	private TypedForm _displayedTypedForm;

	/**
	 * Creates a {@link ConfiguredDynamicFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredDynamicFormBuilder(InstantiationContext context, Config config) {
		super();

		_config = config;
		_configuredForms = FormDefinitionUtil.createTypedFormMapping(config.getForms());
	}
	
	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * The tuples of a {@link FormDefinition}s and the {@link TLType} it is designed for.
	 */
	public Map<TLType, FormDefinition> getConfiguredForms() {
		return _configuredForms;
	}

	/**
	 * The displayed {@link FormDefinition} and its {@link TLType}.
	 */
	public TypedForm getDisplayedTypedForm() {
		return _displayedTypedForm;
	}

	/**
	 * Set the displayed form.
	 * 
	 * @param typedForm
	 *        The form to display.
	 */
	protected void setDisplayedTypedForm(TypedForm typedForm) {
		_displayedTypedForm = typedForm;
	}

	/**
	 * Whether the {@link #getDisplayedTypedForm()} is the standard display of its type.
	 */
	public boolean displaysStandardForm() {
		TypedForm displayedTypedForm = getDisplayedTypedForm();
		if (displayedTypedForm == null) {
			return true;
		}
		return !displayedTypedForm.isLayoutSpecific();
	}

}
