/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.Function0;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.fieldprovider.ConfigurationFieldProvider;
import com.top_logic.element.meta.form.fieldprovider.form.TLFormType;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.definition.ContainerDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.definition.FormVisibility;

/**
 * {@link FormProvider} using the standard form of the {@link ProcessExecution} object customized
 * with visibility annotations.
 */
public class StandardForm extends AbstractConfiguredInstance<StandardForm.Config<?>> implements FormProvider {

	/**
	 * Configuration options for {@link StandardForm}.
	 */
	public interface Config<I extends StandardForm> extends PolymorphicConfiguration<I> {

		/**
		 * The visibility to use for an attribute, if no further customization is done.
		 */
		FormVisibility getDefaultVisibility();

		/**
		 * Customizations for attributes displayed in the default from of the process type.
		 */
		@Key(AnnotationOverlay.ATTRIBUTE)
		List<AnnotationOverlay> getOverlays();

		/**
		 * Customization of an attribute in the default from defined for the process type.
		 */
		interface AnnotationOverlay extends ConfigurationItem {
			/**
			 * @see #getAttribute()
			 */
			String ATTRIBUTE = "attribute";

			/**
			 * Name of the attribute to customize.
			 */
			@Name(ATTRIBUTE)
			@Options(fun = AttributesOfType.class, mapping = AttributeName.class)
			String getAttribute();

			/**
			 * The visibility to apply to the field for the given {@link #getAttribute()} in the
			 * default form.
			 */
			FormVisibility getVisibility();

			/**
			 * Option provider function for {@link AnnotationOverlay#getAttribute()} resolving all
			 * attributes of the process model type.
			 */
			class AttributesOfType extends Function0<List<? extends TLStructuredTypePart>> {
				private final EditContext _editContext;

				/**
				 * Creates a {@link StandardForm.Config.AnnotationOverlay.AttributesOfType}.
				 */
				public AttributesOfType(DeclarativeFormOptions options) {
					_editContext = ConfigurationFieldProvider.editContext(options);
				}

				@Override
				public List<? extends TLStructuredTypePart> apply() {
					TLObject object = _editContext.getObject();
					if (object == null) {
						return Collections.emptyList();
					}

					TLFormType typeAnnotation = _editContext.getAnnotation(TLFormType.class);
					TLStructuredType formType =
						TLFormType.resolve(typeAnnotation, object, _editContext.getDescriptionKey());
					if (formType == null) {
						return Collections.emptyList();
					}

					return formType.getAllParts();
				}
			}

			/**
			 * {@link OptionMapping} for {@link AnnotationOverlay#getAttribute()} just storing the
			 * attribute name.
			 */
			class AttributeName implements OptionMapping {
				@Override
				public Object toSelection(Object option) {
					if (option == null) {
						return null;
					}
					return ((TLStructuredTypePart) option).getName();
				}
			}
		}
	}

	/**
	 * Creates a {@link StandardForm} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StandardForm(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public FormDefinition getFormDefinition(TLStructuredType modelType) {
		if (modelType == null) {
			return null;
		}
		
		TLFormDefinition annotation = modelType.getAnnotation(TLFormDefinition.class);
		if (annotation == null) {
			return null;
		}
		
		FormDefinition defaultForm = annotation.getForm();
		FormDefinition form = TypedConfiguration.copy(defaultForm);
		
		Map<String, FormVisibility> customization =
			getConfig().getOverlays().stream().collect(Collectors.toMap(o -> o.getAttribute(), o -> o.getVisibility()));
		
		updateForm(form, getConfig().getDefaultVisibility(), customization);
		return form;
	}

	private void updateForm(FormElement<?> form, FormVisibility defaultVisibility,
			Map<String, FormVisibility> customization) {
		if (form instanceof ContainerDefinition<?> container) {
			for (PolymorphicConfiguration<?> content : container.getContent()) {
				updateForm((FormElement<?>) content, defaultVisibility, customization);
			}
		}
		if (form instanceof FieldDefinition field) {
			FormVisibility customVisibility = customization.get(field.getAttribute());
			if (customVisibility != null) {
				field.setVisibility(customVisibility);
			} else if (defaultVisibility != FormVisibility.DEFAULT) {
				field.setVisibility(defaultVisibility);
			}
		}
	}
}
