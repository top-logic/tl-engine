/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

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
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.model.config.NamedPartConfig;

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
		// The identity is the edited element: two elements that both carry no annotations produce no
		// value change at all, yet the editor must be rebuilt - what may be added depends on the kind
		// of element, not on what is there already.
		return createControl(context, model, () -> containerFor(ownerOf(model)), () -> ownerOf(model));
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
			Supplier<AnnotatedConfig<? extends TLAnnotation>> containers) {
		return createControl(context, model, containers, null);
	}

	private static ReactControl createControl(ReactContext context, FieldModel model,
			Supplier<AnnotatedConfig<? extends TLAnnotation>> containers, Supplier<Object> identity) {
		ConfigFieldBinding binding = new ConfigFieldBinding(context, model, identity);
		binding.start(() -> buildEditor(context, model, containers.get(), binding));
		return binding.holder();
	}

	private static ReactControl buildEditor(ReactContext context, FieldModel model,
			AnnotatedConfig<? extends TLAnnotation> container, ConfigFieldBinding binding) {
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
			new ConfigFieldPush(() -> binding.push(new ArrayList<>(container.getAnnotations())));
		index.observeFields(push::watch);

		// The container is the form model: an option function or mapping inside an annotation looks
		// outwards for it - SingletonConfig's type mapping reads the module's name off it - and
		// there is no finding it by walking up, since not every configuration on the way offers a
		// way up.
		ReactControl editor =
			new ConfigListEditorControl(context, value, choices, index, model.isEditable(), container);
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
			return editedObject(attributeField.getObject());
		}
		throw new IllegalArgumentException(
			"Annotations can only be edited on a model element, but the field is a " + model.getClass().getName());
	}

	/**
	 * The model element behind the given object: what a form puts in front of it while editing is
	 * seen through.
	 *
	 * <p>
	 * {@link AttributeFieldModel#getObject()} answers the element itself in view mode, but the
	 * overlay buffering the changes in edit mode. The overlay is no model element of any kind, so
	 * asking it which surroundings an annotation needs would find no answer - and the panel could
	 * not be switched to editing at all.
	 * </p>
	 */
	public static TLObject editedObject(TLObject object) {
		if (object instanceof TLFormObjectBase form) {
			TLObject edited = form.getEditedObject();
			if (edited != null) {
				return edited;
			}
		}
		return object;
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
		AnnotatedConfig<? extends TLAnnotation> container = emptyContainerFor(owner);
		// The surroundings are of no use while they are blank. An option function reaching outwards
		// asks the container real questions - SingletonConfig.LocalTypeMapping reads the module's
		// name off it and fails outright on a nameless one - so what the element is called is
		// carried over, the same way the legacy form builders fill their EditModel from the part.
		if (container instanceof NamedPartConfig named && owner instanceof TLNamedPart namedOwner) {
			named.setName(namedOwner.getName());
		}
		return container;
	}

	/**
	 * The meta type an element must have for each container, most specific first.
	 *
	 * <p>
	 * Asked of the element's {@link TLObject#tType() type} rather than of its Java class. The two
	 * agree for a model element, but not for everything a form hands over: an overlay that cannot be
	 * unwrapped is no {@code TLModule} in Java while its type says perfectly well what it stands for.
	 * Going through the model also keeps this closed under new concrete meta types - a further kind
	 * of type or of attribute is covered by the entry for its generalization.
	 * </p>
	 *
	 * <p>
	 * There are four kinds of annotation - module, type, attribute and classifier - and this
	 * repository defines no fifth: every {@code AnnotatedConfig} in it is over one of
	 * {@code TLModuleAnnotation}, {@code TLTypeAnnotation}, {@code TLAttributeAnnotation} or
	 * {@code TLClassifierAnnotation}. The entries below are more numerous only because a class, an
	 * enumeration and a datatype are given the container of their own shape, so that an option
	 * function navigating out of an annotation finds the surroundings it would find in a model file.
	 * </p>
	 */
	private static final List<ContainerKind> CONTAINERS = List.of(
		new ContainerKind("tl.model:TLModule", ModuleConfig.class),
		new ContainerKind("tl.model:TLClassifier", EnumConfig.ClassifierConfig.class),
		new ContainerKind("tl.model:TLReference", ReferenceConfig.class),
		new ContainerKind("tl.model:TLStructuredTypePart", AttributeConfig.class),
		new ContainerKind("tl.model:TLEnumeration", EnumConfig.class),
		new ContainerKind("tl.model:TLClass", ClassConfig.class),
		new ContainerKind("tl.model:TLType", ClassConfig.class));

	/**
	 * One entry of {@link #CONTAINERS}: the meta type and the configuration standing in for it.
	 */
	private record ContainerKind(String typeName, Class<? extends AnnotatedConfig<? extends TLAnnotation>> config) {

		/** Whether the given element's type is this one or a specialization of it. */
		boolean matches(TLType type) {
			try {
				return TLModelUtil.isCompatibleType(TLModelUtil.findType(typeName()), type);
			} catch (TopLogicException ex) {
				// The meta type is not in this application's model at all, so nothing can match it.
				return false;
			}
		}
	}

	private static AnnotatedConfig<? extends TLAnnotation> emptyContainerFor(TLObject owner) {
		TLType type = owner.tType();
		if (type != null) {
			for (ContainerKind kind : CONTAINERS) {
				if (kind.matches(type)) {
					return TypedConfiguration.newConfigItem(kind.config());
				}
			}
		}
		throw new IllegalArgumentException("No annotation container known for " + owner
			+ (type == null ? " (no type)" : " of type " + TLModelUtil.qualifiedName(type)));
	}

}
