/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.layout.configedit.ConfigCollectionValue;
import com.top_logic.layout.configedit.ConfigFieldIndex;
import com.top_logic.layout.configedit.ConfigListEditorControl;
import com.top_logic.layout.configedit.PolymorphicOptions;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.EnumConfig;

/**
 * {@link ReactFieldControlProvider} for the {@code annotations} attribute of a
 * {@link com.top_logic.model.TLModelPart}.
 *
 * <p>
 * An annotation is a configuration, so it is edited with the configuration editor - but not the way
 * {@link ConfigFieldControlProvider} edits a plain configuration-valued attribute. An annotation
 * needs something around it, and what that is depends on the kind of model element it belongs to.
 * </p>
 *
 * <p>
 * <b>The surroundings have to be built, not preserved.</b> The owner of an annotation is a model
 * element, not a configuration, so there is no configuration tree above it to copy along - and an
 * annotation copied on its own has no container at all. Anything inside it that navigates outwards
 * (an option function, a derived property, a constraint) would find nothing. So a container of the
 * shape the annotation expects is created for it: {@link ModuleConfig} for a module,
 * {@link ClassConfig} for a class, and so on. This is what the legacy form builders do with their
 * {@code EditModel} interfaces, which extend exactly these configurations; what they add on top is
 * form furniture - a name, a label, a create flag - that nobody edits here.
 * </p>
 *
 * <p>
 * The container earns its keep twice over. Its {@code annotations} property already declares
 * {@code @Options(AllInAppImplementations)} and {@code @Key(CONFIGURATION_INTERFACE_NAME)}, so the
 * admissible annotation types - narrowed to the ones that apply to this kind of element by the
 * property's own element type - and the rule that each type may appear once both come from the
 * property, with nothing to compute here.
 * </p>
 *
 * <p>
 * The consequence, which is legacy's too: an annotation's upward navigation reaches this container
 * rather than the real module or type. Nothing that works in the legacy model editor breaks here,
 * and nothing that does not work there starts working.
 * </p>
 */
public class AnnotationsFieldControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return createControl(context, model, containerFor(ownerOf(model)));
	}

	/**
	 * The editor over the annotations of the given container.
	 *
	 * <p>
	 * Free of the model, so what this decides - copy into the container, offer the container's own
	 * options, hand the result back on a change - is tested without a model to build a part from.
	 * </p>
	 *
	 * @param model
	 *        The field holding the annotations of the edited element.
	 * @param container
	 *        The configuration standing in for the element's surroundings. Its
	 *        {@code annotations} property is what is edited.
	 */
	public static ReactControl createControl(ReactContext context, FieldModel model,
			AnnotatedConfig<? extends TLAnnotation> container) {
		PropertyDescriptor annotations =
			container.descriptor().getProperty(AnnotatedConfig.ANNOTATIONS);

		// Copied one by one, as legacy does: what the model holds must not change before the form
		// is saved, and must survive Cancel unchanged.
		for (TLAnnotation annotation : annotationsOf(model)) {
			addCopy(container, annotations, annotation);
		}

		ConfigCollectionValue value = new ConfigCollectionValue(container, annotations);
		PolymorphicOptions.Choices choices = PolymorphicOptions.compute(container, annotations);

		ConfigFieldIndex index = new ConfigFieldIndex();
		ConfigFieldPush push =
			new ConfigFieldPush(() -> model.setValue(new ArrayList<>(container.getAnnotations())));
		index.observeFields(push::watch);

		ReactControl editor =
			new ConfigListEditorControl(context, value, choices, index, model.isEditable());
		push.armed();
		return editor;
	}

	@SuppressWarnings("unchecked")
	private static void addCopy(AnnotatedConfig<? extends TLAnnotation> container,
			PropertyDescriptor annotations, TLAnnotation annotation) {
		Collection<ConfigurationItem> target =
			(Collection<ConfigurationItem>) container.value(annotations);
		target.add(TypedConfiguration.copy(annotation));
	}

	private static List<TLAnnotation> annotationsOf(FieldModel model) {
		List<TLAnnotation> result = new ArrayList<>();
		if (model.getValue() instanceof Collection<?> collection) {
			for (Object each : collection) {
				result.add((TLAnnotation) each);
			}
		}
		return result;
	}

	/**
	 * The element whose annotations are edited.
	 */
	private static TLObject ownerOf(FieldModel model) {
		if (model instanceof AttributeFieldModel attributeField) {
			return attributeField.getObject();
		}
		throw new IllegalArgumentException(
			"Annotations can only be edited on a model element, but the field is a " + model.getClass().getName());
	}

	/**
	 * The configuration standing in for the given element's surroundings.
	 *
	 * <p>
	 * One per kind of annotation there is - module, type, attribute and classifier - since it is the
	 * container's {@code annotations} property that decides which annotations apply.
	 * </p>
	 */
	static AnnotatedConfig<? extends TLAnnotation> containerFor(TLObject owner) {
		if (owner instanceof TLModule) {
			return TypedConfiguration.newConfigItem(ModuleConfig.class);
		}
		if (owner instanceof TLEnumeration) {
			return TypedConfiguration.newConfigItem(EnumConfig.class);
		}
		if (owner instanceof TLClassifier) {
			return TypedConfiguration.newConfigItem(EnumConfig.ClassifierConfig.class);
		}
		if (owner instanceof TLReference) {
			return TypedConfiguration.newConfigItem(ReferenceConfig.class);
		}
		if (owner instanceof TLStructuredTypePart) {
			return TypedConfiguration.newConfigItem(AttributeConfig.class);
		}
		if (owner instanceof TLClass) {
			return TypedConfiguration.newConfigItem(ClassConfig.class);
		}
		throw new IllegalArgumentException("No annotation container known for " + owner);
	}

}
