/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.form.MetaControlProvider;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.model.internal.TemplateControl;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.formeditor.parts.template.HTMLTemplateFormProvider.TemplateConfig;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormContextDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormDefinitionTemplateProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link VariableDefinition} for a HTML template that embeds a form into the custom rendering.
 */
public class EmbeddedForm extends AbstractConfiguredInstance<EmbeddedForm.Config>
		implements VariableDefinition<EmbeddedForm.Config> {

	private FormDefinitionTemplateProvider _formDefinition;

	/**
	 * Configuration options for {@link EmbeddedForm}.
	 */
	@TagName("embedded-form")
	public interface Config extends VariableDefinition.Config<EmbeddedForm>, FormContextDefinition {

		/** Configuration name for the value of the {@link #getFormDefinition()}. */
		String FORM_DEFINITION_NAME = "formDefinition";

		/** The form to render. */
		@ItemDisplay(ItemDisplayType.VALUE)
		@Name(FORM_DEFINITION_NAME)
		@DefaultContainer
		FormDefinition getFormDefinition();

		@Override
		@DerivedRef({ VARIABLE_OWNER, TemplateConfig.FORM_CONTEXT_TYPE })
		TLModelPartRef getFormContextType();

	}

	/**
	 * Creates a {@link EmbeddedForm}.
	 */
	public EmbeddedForm(InstantiationContext context, Config config) {
		super(context, config);

		_formDefinition = context.getInstance(config.getFormDefinition());
	}

	@Override
	public Object eval(LayoutComponent component, FormEditorContext editorContext, Object model) {
		try {
			TLObject item = (TLObject) model;
			String itemID = IdentifierUtil.toExternalForm(item.tIdLocal());

			FormContainer contentGroup = editorContext.getContentGroup();

			FormContainer innerGroup;
			String groupName = "formFragment-" + itemID;
			if (contentGroup.hasMember(groupName)) {
				innerGroup = (FormContainer) contentGroup.getMember(groupName);
			} else {
				innerGroup = new FormGroup(groupName, ResPrefix.NONE);
				innerGroup.setStableIdSpecialCaseMarker(item);
				contentGroup.addMember(innerGroup);
			}

			FormEditorContext innerContext = new FormEditorContext.Builder(editorContext)
				.formType(getConfig().getFormContextType().resolveClass())
				.concreteType(null)
				.model((TLObject) model)
				.contentGroup(innerGroup)
				.build();
			HTMLTemplateFragment template = _formDefinition.createDisplayTemplate(innerContext);
			return new TemplateControl(innerContext.getContentGroup(), MetaControlProvider.INSTANCE, template);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

}
