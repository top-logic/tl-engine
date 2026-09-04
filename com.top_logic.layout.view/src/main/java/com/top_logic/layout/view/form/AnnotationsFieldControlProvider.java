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
import com.top_logic.element.layout.meta.TLEnumerationFormBuilder;
import com.top_logic.element.layout.meta.TLModuleFormBuilder;
import com.top_logic.element.layout.meta.TLPropertyFormBuilder;
import com.top_logic.element.layout.meta.TLReferenceFormBuilder;
import com.top_logic.element.layout.meta.TLStructuredTypeFormBuilder;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.PartModel;
import com.top_logic.layout.configedit.ConfigCollectionValue;
import com.top_logic.layout.configedit.ConfigFieldIndex;
import com.top_logic.layout.configedit.ConfigListEditorControl;
import com.top_logic.layout.configedit.ConfigValidation;
import com.top_logic.layout.configedit.PolymorphicOptions;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.FullQualifiedName;
import com.top_logic.model.config.NamedPartConfig;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

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
 * (an option function, a derived property, a constraint) would find nothing. So the surroundings are
 * built: legacy's own {@code EditModel} interfaces, one per kind of element -
 * {@link TLStructuredTypeFormBuilder.EditModel} for a type, {@link TLModuleFormBuilder.EditModel}
 * for a module, and so on.
 * </p>
 *
 * <p>
 * Legacy's interfaces rather than the plain configurations they extend, because what they add is not
 * form furniture. {@code TLStructuredTypeFormBuilder.EditModel} is also a
 * {@link com.top_logic.model.config.FullQualifiedName} and a
 * {@link com.top_logic.model.config.TypeRef}, and that is exactly what an option function asks it
 * for: {@code PartNamesOptionProvider}, behind the {@code main-properties} annotation, casts the
 * form model to both. A plain {@code ClassConfig} is neither, and the whole field is then refused
 * with "Error during instantiation". The values those two carry are filled in the same way legacy
 * fills them, from the edited element (see {@link #surroundingsFor(TLObject)}).
 * </p>
 *
 * <p>
 * For an attribute the annotations do not sit in the form model at all but in the {@code PartModel}
 * nested inside it, so the two are kept apart; see {@link Surroundings}.
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
		return build(context, model, () -> surroundingsFor(ownerOf(model)), () -> ownerOf(model));
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
		return build(context, model, () -> {
			AnnotatedConfig<? extends TLAnnotation> container = containers.get();
			return new Surroundings(container, container);
		}, null);
	}

	private static ReactControl build(ReactContext context, FieldModel model,
			Supplier<Surroundings> surroundings, Supplier<Object> identity) {
		ConfigFieldBinding binding = new ConfigFieldBinding(context, model, identity);
		binding.start(() -> buildEditor(context, model, surroundings.get(), binding));
		return binding.holder();
	}

	/**
	 * What an annotation is edited inside of: the configuration holding it, and the form model an
	 * option function reaches for.
	 *
	 * <p>
	 * Two objects, because they are not always the same one. For a type they are - legacy's
	 * {@code TLStructuredTypeFormBuilder.EditModel} is itself the {@code ClassConfig} carrying the
	 * annotations. For an attribute they are not: the annotations sit in the nested
	 * {@code PartModel}, while what the form is built over, and what an option function is handed,
	 * is the {@code EditModel} around it.
	 * </p>
	 *
	 * @param formModel
	 *        Handed to the editor as {@code DeclarativeFormBuilder.FORM_MODEL}.
	 * @param annotations
	 *        The configuration whose {@code annotations} property is edited.
	 */
	private record Surroundings(ConfigurationItem formModel,
			AnnotatedConfig<? extends TLAnnotation> annotations) {
		// Fields only.
	}

	private static ReactControl buildEditor(ReactContext context, FieldModel model,
			Surroundings surroundings, ConfigFieldBinding binding) {
		AnnotatedConfig<? extends TLAnnotation> container = surroundings.annotations();
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
		ConfigFieldPush push = new ConfigFieldPush(() -> {
			binding.push(new ArrayList<>(container.getAnnotations()));
			validate(model, container, index);
		});
		index.observeFields(push::watch);

		// The container is the form model: an option function or mapping inside an annotation looks
		// outwards for it - SingletonConfig's type mapping reads the module's name off it - and
		// there is no finding it by walking up, since not every configuration on the way offers a
		// way up.
		ReactControl editor =
			new ConfigListEditorControl(context, value, choices, index, model.isEditable(),
				surroundings.formModel());
		push.armed();
		// Also once up front, not only on every later change: a form entered on annotations that
		// are already incomplete would otherwise be saved untouched, since nothing was edited and
		// nothing therefore checked.
		validate(model, container, index);
		return editor;
	}

	/**
	 * Reports at the edited field whatever keeps the annotations from being handed over.
	 *
	 * <p>
	 * The annotations are edited as one field of a surrounding form, and that form decides whether
	 * it may be saved by asking its fields ({@link FieldModel#hasError()}). So the verdict
	 * {@link ConfigFormControl} reaches by refusing its own Apply has to arrive here as an error on
	 * the field instead - otherwise the rules a configuration is displayed under would hold in the
	 * standalone configuration form and nowhere else: the mandatory marker would be drawn on a
	 * value that may be left empty, an unconfirmed entry would be silently dropped, and an input
	 * the field could not read would be saved as the value it had before.
	 * </p>
	 *
	 * <p>
	 * Only while editable. In view mode there is nothing to save and nothing to keep from being
	 * saved, and marking what is merely being looked at would be noise.
	 * </p>
	 */
	private static void validate(FieldModel model, AnnotatedConfig<? extends TLAnnotation> container,
			ConfigFieldIndex index) {
		if (!model.isEditable()) {
			return;
		}
		ConfigValidation.Refusal refusal = ConfigValidation.refusalFor(container.getAnnotations(), index);
		model.setModelValidationError(refusal == null ? null : refusal.message());
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
	static Surroundings surroundingsFor(TLObject owner) {
		Surroundings surroundings = emptySurroundingsFor(owner);
		AnnotatedConfig<? extends TLAnnotation> container = surroundings.annotations();
		// The surroundings are of no use while they are blank. An option function reaching outwards
		// asks the container real questions - SingletonConfig.LocalTypeMapping reads the module's
		// name off it and fails outright on a nameless one - so what the element is called is
		// carried over, the same way the legacy form builders fill their EditModel from the part.
		if (container instanceof NamedPartConfig named && owner instanceof TLNamedPart namedOwner) {
			named.setName(namedOwner.getName());
		}
		// ... and so is where it sits in the model. PartNamesOptionProvider resolves the edited part
		// from the form model's FullQualifiedName and its target type from its TypeRef; neither
		// answers anything on a blank container, and the whole field is then refused with "Error
		// during instantiation". Filled the way the legacy form builders fill them in
		// fillFormModel() - see TLStructuredTypePartFormBuilder, which writes the part's own
		// qualified name and, as the type spec, the qualified name of the part's value type.
		fill(surroundings.formModel(), surroundings.annotations(), qualifiedNameOf(owner),
			typeSpecOf(owner));
		return surroundings;
	}

	/**
	 * Writes what an option function reads off the surroundings, wherever it can be written.
	 *
	 * <p>
	 * Public so that a test can hold every container this class builds against it: which of the two
	 * properties a container declares, and which of them it merely derives, differs per container
	 * and is exactly what this has to get right.
	 * </p>
	 *
	 * @param qualifiedName
	 *        The edited element's own qualified name, or {@code null} if it has none.
	 * @param typeSpec
	 *        The qualified name of the type an option function should offer the parts of: the
	 *        edited type itself, or an attribute's value type. {@code null} where there is none.
	 */
	public static void fill(ConfigurationItem formModel, ConfigurationItem annotations,
			String qualifiedName, String typeSpec) {
		for (ConfigurationItem each : new ConfigurationItem[] { formModel, annotations }) {
			set(each, FullQualifiedName.FULL_QUALIFIED_NAME, qualifiedName);
			set(each, TypeRef.TYPE_SPEC, typeSpec);
		}
	}

	/**
	 * Writes the given property of the given configuration, where it has one that can be written.
	 *
	 * <p>
	 * Not every container declares both properties, and an attribute's form model
	 * <em>derives</em> both of them from the part model nested in it - writing one there throws
	 * outright ("Derived property ... cannot be set"), and taking the whole field down with it.
	 * Setting the part model alone is also enough: the form model above then answers with what was
	 * written there.
	 * </p>
	 */
	private static void set(ConfigurationItem item, String propertyName, String value) {
		if (value == null) {
			return;
		}
		PropertyDescriptor property = item.descriptor().getProperty(propertyName);
		if (property != null && !property.isDerived()) {
			item.update(property, value);
		}
	}

	/**
	 * The qualified name of the type whose parts an option function should offer: the edited type
	 * itself, or the value type of the edited attribute.
	 */
	private static String typeSpecOf(TLObject owner) {
		if (owner instanceof TLType type) {
			return qualifiedNameOf(type);
		}
		if (owner instanceof TLStructuredTypePart part) {
			return qualifiedNameOf(part.getType());
		}
		return null;
	}

	/**
	 * The qualified name of the given model element, or {@code null} if it has none.
	 */
	private static String qualifiedNameOf(TLObject owner) {
		if (owner instanceof TLModelPart part) {
			try {
				return TLModelUtil.qualifiedName(part);
			} catch (RuntimeException ex) {
				// Not every model part has a qualified name - an unnamed or detached one has not.
				return null;
			}
		}
		return null;
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
		new ContainerKind("tl.model:TLModule", TLModuleFormBuilder.EditModel.class, null),
		new ContainerKind("tl.model:TLClassifier", EnumConfig.ClassifierConfig.class, null),
		new ContainerKind("tl.model:TLReference", TLReferenceFormBuilder.EditModel.class,
			TLReferenceFormBuilder.ReferenceModel.class),
		new ContainerKind("tl.model:TLStructuredTypePart", TLPropertyFormBuilder.EditModel.class,
			TLPropertyFormBuilder.PropertyModel.class),
		new ContainerKind("tl.model:TLEnumeration", TLEnumerationFormBuilder.EditModel.class, null),
		new ContainerKind("tl.model:TLClass", TLStructuredTypeFormBuilder.EditModel.class, null),
		new ContainerKind("tl.model:TLType", TLStructuredTypeFormBuilder.EditModel.class, null));

	/**
	 * One entry of {@link #CONTAINERS}: the meta type and the configuration standing in for it.
	 */
	private record ContainerKind(String typeName, Class<? extends ConfigurationItem> formModel,
			Class<? extends AnnotatedConfig<? extends TLAnnotation>> partModel) {

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

	@SuppressWarnings("unchecked")
	private static Surroundings emptySurroundingsFor(TLObject owner) {
		TLType type = owner.tType();
		if (type != null) {
			for (ContainerKind kind : CONTAINERS) {
				if (kind.matches(type)) {
					ConfigurationItem formModel = TypedConfiguration.newConfigItem(kind.formModel());
					if (kind.partModel() == null) {
						return new Surroundings(formModel,
							(AnnotatedConfig<? extends TLAnnotation>) formModel);
					}
					AnnotatedConfig<? extends TLAnnotation> part =
						TypedConfiguration.newConfigItem(kind.partModel());
					((TLStructuredTypePartFormBuilder.EditModel) formModel)
						.setPartModel((PartModel) part);
					return new Surroundings(formModel, part);
				}
			}
		}
		throw new IllegalArgumentException("No annotation container known for " + owner
			+ (type == null ? " (no type)" : " of type " + TLModelUtil.qualifiedName(type)));
	}

}
