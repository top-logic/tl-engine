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
import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.bpe.bpml.display.StandardForm.Config.AnnotationOverlay;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Task;
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
 * {@link FormProvider} using the standard form of the {@link ProcessExecution} object or the form
 * of another task customized with visibility annotations.
 */
@Label("Other form")
public class StandardForm extends AbstractConfiguredInstance<StandardForm.Config<?>> implements FormProvider {

	/**
	 * Configuration options for {@link StandardForm}.
	 */
	@DisplayOrder({
		Config.SOURCE_TASK,
		Config.DEFAULT_VISIBILITY,
		Config.OVERLAYS,
	})
	public interface Config<I extends StandardForm> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getSourceTask()
		 */
		String SOURCE_TASK = "source-task";

		/**
		 * @see #getDefaultVisibility()
		 */
		String DEFAULT_VISIBILITY = "default-visibility";

		/**
		 * @see #getOverlays()
		 */
		String OVERLAYS = "overlays";

		/**
		 * ID of another task to take the form definition from.
		 * 
		 * <p>
		 * If not given, the default form of the process model type is used as source for the
		 * customization.
		 * </p>
		 * 
		 * <p>
		 * Make sure, not define cycles in this reference.
		 * </p>
		 */
		@Nullable
		@Name(SOURCE_TASK)
		@Options(fun = OtherTasks.class, mapping = TaskId.class)
		String getSourceTask();

		/**
		 * The visibility to use for an attribute, if no further customization is done.
		 */
		@Name(DEFAULT_VISIBILITY)
		FormVisibility getDefaultVisibility();

		/**
		 * Customizations for attributes displayed in the default from of the process type.
		 */
		@Name(OVERLAYS)
		@Label("Attribute customizations")
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
			 * @see #getVisibility()
			 */
			String VISIBILITY = "visibility";

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
			@Name(VISIBILITY)
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

		/**
		 * Option mapping for {@link Config#getSourceTask()}.
		 */
		class TaskId implements OptionMapping {
			@Override
			public Object toSelection(Object option) {
				if (option == null) {
					return null;
				}
				return ((Node) option).getExtId();
			}
		}

		/**
		 * Option provider function for {@link Config#getSourceTask()}.
		 */
		class OtherTasks extends Function0<List<ManualTask>> {
			private EditContext _editContext;

			public OtherTasks(DeclarativeFormOptions options) {
				_editContext = ConfigurationFieldProvider.editContext(options);
			}

			@Override
			public List<ManualTask> apply() {
				Node self = (Node) _editContext.getObject();
				return getOtherTasks(self).toList();
			}

			/**
			 * All {@link ManualTask}s of process the given node is defined in.
			 */
			public static Stream<ManualTask> getOtherTasks(Node self) {
				return self.getProcess().getLanes().stream()
					.flatMap(l -> l.getNodes().stream())
					.filter(n -> n != self)
					.filter(n -> n instanceof ManualTask)
					.map(n -> (ManualTask) n)
					.filter(t -> t.getFormDefinition() != null);
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
	public FormDefinition getFormDefinition(Task self, TLStructuredType modelType) {
		if (modelType == null) {
			return null;
		}
		
		FormDefinition defaultForm;

		Config<?> config = getConfig();
		String sourceId = config.getSourceTask();
		if (sourceId != null) {
			ManualTask sourceTask =
				Config.OtherTasks.getOtherTasks(self).filter(t -> sourceId.equals(t.getExtId())).findFirst().get();

			defaultForm = sourceTask.getDisplayDescription();
		} else {
			TLFormDefinition annotation = modelType.getAnnotation(TLFormDefinition.class);
			if (annotation == null) {
				return null;
			}

			defaultForm = annotation.getForm();
		}
		
		List<AnnotationOverlay> overlays = config.getOverlays();
		FormVisibility defaultVisibility = config.getDefaultVisibility();

		if (!overlays.isEmpty() || defaultVisibility != FormVisibility.DEFAULT) {
			FormDefinition form = TypedConfiguration.copy(defaultForm);
			Map<String, FormVisibility> customization =
				overlays.stream().collect(Collectors.toMap(o -> o.getAttribute(), o -> o.getVisibility()));

			updateForm(form, defaultVisibility, customization);
			return form;
		}
		
		return defaultForm;
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
