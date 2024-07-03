/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.formeditor.builder.ConfiguredDynamicFormBuilder;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.element.layout.formeditor.builder.TypedFormDefinition;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command deleting the local {@link FormDefinition} of a {@link FormComponent}.
 * 
 * <p>
 * This command is only usable by {@link FormComponent}s instantiated from typed layout templates
 * using the {@link ConfiguredDynamicFormBuilder}.
 * </p>
 * 
 * @see ConfiguredDynamicFormBuilder
 * @see FormComponent
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DeleteFormDefinitionCommand extends AbstractCommandHandler {

	/**
	 * Creates an instance of {@link DeleteFormDefinitionCommand}.
	 */
	public DeleteFormDefinitionCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		if (component instanceof FormComponent) {
			FormComponent form = (FormComponent) component;

			ModelBuilder modelBuilder = form.getBuilder();
			if (modelBuilder instanceof ConfiguredDynamicFormBuilder) {
				String scope = LayoutTemplateUtils.getNonNullNameScope(component);
				TLLayout layout = LayoutTemplateUtils.getOrCreateLayout(scope);

				return deleteLocalFormDefinition(form, scope, layout);
			}

			return HandlerResult.error(I18NConstants.NO_TEMPLATE_COMPONENT_ERROR);
		}

		return HandlerResult.error(com.top_logic.element.layout.formeditor.I18NConstants.NO_FORM_COMPONENT_ERROR);
	}

	private HandlerResult deleteLocalFormDefinition(FormComponent component, String scope, TLLayout layout) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		
		try (Transaction tx = kb.beginTransaction()) {
			TypedForm typedForm = ((ConfiguredDynamicFormBuilder) component.getBuilder()).getDisplayedTypedForm();
			TLStructuredType type = typedForm.getFormType();

			if (type != null) {
				Map<TLModelPartRef, TypedFormDefinition> forms =
					AbstractConfigureFormDefinitionCommand.getFormDefinitions(layout);
				forms.remove(TLModelPartRef.ref(type));
			}

			LayoutTemplateUtils.storeLayout(scope, layout.getTemplateName(), layout.getArguments());

			tx.commit();
		} catch (ConfigurationException exception) {
			throw new ConfigurationError(exception);
		}

		LayoutTemplateUtils.replaceComponent(scope, component);

		return HandlerResult.DEFAULT_RESULT;
	}
}
