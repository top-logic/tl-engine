/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dev.tools.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.WithResultActions;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLSupportsMultiple;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link ViewAction} that mutates the application model from the model editor: creates a module,
 * type (class or enumeration) or attribute, adds or removes a generalization of a class, or deletes a
 * model part or generalization link.
 *
 * <p>
 * The mutating operations create their result through the {@link TLModelUtil} primitives and return
 * the newly created part to its follow-up actions, so it can be written to the
 * editor's selection channel. The created element's name (and, for an attribute, its value type and
 * cardinality) come from the transient object the action processes (the result of an enclosing
 * {@code <store-form-state>} in the dialog form); the container to create the element in comes
 * from the configured {@link Config#getContainer() container channel}. All operations must run
 * inside a transaction (wrap the action in {@code <with-transaction>}).
 * </p>
 */
public class ModelEditAction implements ViewAction {

	/** Classifier name of {@code tl.devtools:TypeKind} selecting an enumeration over a class. */
	private static final String KIND_ENUMERATION = "enumeration";

	/** Transient input attribute holding the name of the element to create. */
	private static final String NAME = "name";

	/** Transient input attribute holding the kind ({@code class}/{@code enumeration}) of a new type. */
	private static final String KIND = "kind";

	/** Transient input attribute holding the value type of a new attribute. */
	private static final String TYPE = "type";

	/** Transient input attribute marking a new attribute mandatory. */
	private static final String MANDATORY = "mandatory";

	/** Transient input attribute marking a new attribute multi-valued. */
	private static final String MULTIPLE = "multiple";

	/**
	 * Transient input attribute holding the {@code tl.devtools:ReferenceKind} of a new reference.
	 */
	private static final String REFERENCE_KIND = "referenceKind";

	/**
	 * Transient input attribute holding the name of the opposite reference to create in the target
	 * class of a new reference.
	 */
	private static final String OPPOSITE = "opposite";

	/** Transient input attribute holding the class to add as generalization. */
	private static final String GENERALIZATION = "generalization";

	/**
	 * Qualified name of the transient type standing for a generalization link between two classes,
	 * with the attributes {@link #SPECIALIZATION} and {@link #GENERALIZATION}.
	 */
	private static final String GENERALIZATION_TYPE = "tl.devtools:Generalization";

	/** Attribute of a {@link #GENERALIZATION_TYPE} holding the specialized class. */
	private static final String SPECIALIZATION = "specialization";

	/** Classifier name of {@code tl.devtools:ReferenceKind} selecting a composition. */
	private static final String KIND_COMPOSITION = "composition";

	/** Classifier name of {@code tl.devtools:ReferenceKind} selecting an aggregation. */
	private static final String KIND_AGGREGATION = "aggregation";

	/**
	 * The model mutation a {@link ModelEditAction} performs.
	 */
	public enum Mode {
		/** Create a new module named by the input. */
		CREATE_MODULE,

		/** Create a new class or enumeration named by the input in the container module. */
		CREATE_TYPE,

		/** Add a new property described by the input to the container type. */
		CREATE_ATTRIBUTE,

		/** Add a new classifier named by the input to the container enumeration. */
		CREATE_CLASSIFIER,

		/** Add the class selected in the input as generalization of the container class. */
		ADD_GENERALIZATION,

		/** Remove the class passed as the action input from the generalizations of the container class. */
		REMOVE_GENERALIZATION,

		/**
		 * Delete the model part passed as the action input, or remove the generalization link a
		 * {@code tl.devtools:Generalization} input stands for.
		 *
		 * <p>
		 * The result is what the deleted element belonged to, so that a chain writing it to the
		 * selection selects it in place of the deleted element: the module of a type, the owner
		 * type of an attribute or reference, the enumeration of a classifier, the specialization
		 * of a generalization link, and nothing for a module.
		 * </p>
		 */
		DELETE;
	}

	/**
	 * Configuration for {@link ModelEditAction}.
	 *
	 * <p>
	 * App-specific action referenced by {@code class=} in the model-editor views rather than claiming a
	 * global {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<ModelEditAction>, WithResultActions {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getContainer()}. */
		String CONTAINER = "container";

		@Override
		@ClassDefault(ModelEditAction.class)
		Class<? extends ModelEditAction> getImplementationClass();

		/**
		 * The model mutation to perform.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();

		/**
		 * Name of the channel holding the container the new element is created in: the target module for
		 * {@link Mode#CREATE_TYPE}, the owner type (or one of its parts) for
		 * {@link Mode#CREATE_ATTRIBUTE}, the enumeration for {@link Mode#CREATE_CLASSIFIER}, the
		 * specialized class for {@link Mode#ADD_GENERALIZATION} and {@link Mode#REMOVE_GENERALIZATION}.
		 * Unused by {@link Mode#CREATE_MODULE} and {@link Mode#DELETE}.
		 */
		@Name(CONTAINER)
		@Nullable
		String getContainer();
	}

	private final Mode _mode;

	private final String _containerChannel;

	/**
	 * Creates a new {@link ModelEditAction} from configuration.
	 */
	@CalledByReflection
	public ModelEditAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
		_containerChannel = config.getContainer();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		switch (_mode) {
			case CREATE_MODULE:
				return createModule(input);
			case CREATE_TYPE:
				return createType(context, input);
			case CREATE_ATTRIBUTE:
				return createAttribute(context, input);
			case CREATE_CLASSIFIER:
				return createClassifier(context, input);
			case ADD_GENERALIZATION:
				return addGeneralization(context, input);
			case REMOVE_GENERALIZATION:
				return removeGeneralization(context, input);
			case DELETE:
				return delete(input);
		}
		return input;
	}

	/**
	 * Creates a new module named by the input in the application model.
	 */
	private TLModule createModule(Object input) {
		String name = requireName(input);
		return TLModelUtil.addModule(ModelService.getApplicationModel(), name);
	}

	/**
	 * Creates a new class or enumeration named by the input in the container module.
	 */
	private TLType createType(ReactContext context, Object input) {
		TLObject model = asTransient(input);
		String name = requireName(model);
		Object container = container(context);
		if (!(container instanceof TLModule module)) {
			throw new TopLogicException(I18NConstants.ERROR_NO_TARGET);
		}
		Object kind = model.tValueByName(KIND);
		boolean enumeration = kind instanceof TLClassifier classifier && KIND_ENUMERATION.equals(classifier.getName());
		return enumeration ? TLModelUtil.addEnumeration(module, name) : TLModelUtil.addClass(module, name);
	}

	/**
	 * Adds a new attribute described by the input to the container type.
	 *
	 * <p>
	 * An attribute with a {@link TLPrimitive} value type becomes a {@link TLProperty}; an attribute
	 * referring to a class or an enumeration becomes a navigable {@link TLReference} (see
	 * {@link TLModelUtil#addForwardReferenceEnd(TLClass, String, TLType, boolean)}). A reference to a
	 * class may be a composition or an aggregation (see {@link #REFERENCE_KIND}) and may get a
	 * navigable opposite reference in the target class (see {@link #OPPOSITE}). All input is checked
	 * before the model is changed.
	 * </p>
	 */
	private TLStructuredTypePart createAttribute(ReactContext context, Object input) {
		TLObject model = asTransient(input);
		String name = requireName(model);
		TLStructuredType owner = ownerType(container(context));
		if (!(model.tValueByName(TYPE) instanceof TLType valueType)) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_TYPE);
		}
		boolean multiple = Boolean.TRUE.equals(model.tValueByName(MULTIPLE));
		if (multiple && !supportsMultiple(valueType)) {
			throw new TopLogicException(I18NConstants.ERROR_MULTIPLE_NOT_SUPPORTED__TYPE.fill(valueType));
		}
		requireUnusedName(owner, name);

		TLStructuredTypePart part;
		if (valueType instanceof TLPrimitive) {
			part = TLModelUtil.addProperty(owner, name, valueType);
		} else if (owner instanceof TLClass ownerClass) {
			part = createReference(ownerClass, name, valueType, model);
		} else {
			throw new TopLogicException(I18NConstants.ERROR_REFERENCE_NOT_SUPPORTED__OWNER.fill(owner));
		}
		if (Boolean.TRUE.equals(model.tValueByName(MANDATORY))) {
			part.setMandatory(true);
		}
		if (multiple) {
			part.setMultiple(true);
		}
		return part;
	}

	/**
	 * Adds a reference of the {@link #REFERENCE_KIND kind} given in the input to the owner class, and
	 * the {@link #OPPOSITE opposite} reference to the target class if the input names one.
	 */
	private static TLReference createReference(TLClass owner, String name, TLType targetType, TLObject model) {
		String kind = model.tValueByName(REFERENCE_KIND) instanceof TLClassifier classifier ? classifier.getName() : null;
		boolean composite = KIND_COMPOSITION.equals(kind);
		boolean aggregate = KIND_AGGREGATION.equals(kind);
		if ((composite || aggregate) && !(targetType instanceof TLClass)) {
			throw new TopLogicException(I18NConstants.ERROR_KIND_REQUIRES_CLASS__TYPE.fill(targetType));
		}
		String opposite = trimmed(model.tValueByName(OPPOSITE));
		TLClass targetClass = null;
		if (opposite != null) {
			if (!(targetType instanceof TLClass c)) {
				throw new TopLogicException(I18NConstants.ERROR_REFERENCE_NOT_SUPPORTED__OWNER.fill(targetType));
			}
			targetClass = c;
			if (targetClass == owner && opposite.equals(name)) {
				throw new TopLogicException(I18NConstants.ERROR_PART_EXISTS__TYPE_NAME.fill(owner, name));
			}
			requireUnusedName(targetClass, opposite);
		}

		TLAssociationEnd end = TLModelUtil.addForwardReferenceEnd(owner, name, targetType, composite);
		TLAssociationEnd ownerEnd = TLModelUtil.getOtherEnd(end);
		if (aggregate) {
			// The owner end points to the whole of the aggregation.
			ownerEnd.setAggregate(true);
		}
		TLReference reference = TLModelUtil.addReference(owner, name, end);
		if (targetClass != null) {
			TLModelUtil.addReference(targetClass, opposite, ownerEnd);
			ownerEnd.setNavigate(true);
		}
		return reference;
	}

	/**
	 * Rejects the given name for a new part of the given type, if the type already has a part of
	 * that name.
	 */
	private static void requireUnusedName(TLStructuredType type, String name) {
		if (type.getPart(name) != null) {
			throw new TopLogicException(I18NConstants.ERROR_PART_EXISTS__TYPE_NAME.fill(type, name));
		}
	}

	/**
	 * Whether an attribute of the given value type may hold several values.
	 *
	 * <p>
	 * A primitive type supports multiple values only if it is annotated with
	 * {@link TLSupportsMultiple}; every other type (class, enumeration) does.
	 * </p>
	 */
	private static boolean supportsMultiple(TLType valueType) {
		if (!(valueType instanceof TLPrimitive primitive)) {
			return true;
		}
		TLSupportsMultiple annotation = primitive.getAnnotation(TLSupportsMultiple.class);
		return annotation != null && annotation.getValue();
	}

	/**
	 * Adds a new classifier named by the input to the container enumeration.
	 */
	private TLClassifier createClassifier(ReactContext context, Object input) {
		String name = requireName(asTransient(input));
		Object container = container(context);
		TLEnumeration enumeration;
		if (container instanceof TLEnumeration e) {
			enumeration = e;
		} else if (container instanceof TLClassifier classifier) {
			enumeration = classifier.getOwner();
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NO_TARGET);
		}
		return TLModelUtil.addClassifier(enumeration, name);
	}

	/**
	 * Adds the class selected in the input (see {@link #GENERALIZATION}) as generalization of the
	 * container class.
	 *
	 * <p>
	 * A class that is already a direct generalization is not added twice. A generalization is
	 * rejected if it is {@link TLClass#isFinal() final} or if it would make the class its own
	 * transitive generalization.
	 * </p>
	 */
	private TLClass addGeneralization(ReactContext context, Object input) {
		TLClass specialization = containerClass(context);
		if (!(asTransient(input).tValueByName(GENERALIZATION) instanceof TLClass generalization)) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_GENERALIZATION);
		}
		if (specialization.getGeneralizations().contains(generalization)) {
			return specialization;
		}
		if (generalization.isFinal()) {
			throw new TopLogicException(I18NConstants.ERROR_FINAL_GENERALIZATION__CLASS.fill(generalization));
		}
		if (TLModelUtil.isGeneralization(specialization, generalization)) {
			throw new TopLogicException(
				I18NConstants.ERROR_CYCLIC_GENERALIZATION__CLASS_GENERALIZATION.fill(specialization, generalization));
		}
		specialization.getGeneralizations().add(generalization);
		return specialization;
	}

	/**
	 * Removes the class passed as the action input from the generalizations of the container class.
	 */
	private TLClass removeGeneralization(ReactContext context, Object input) {
		TLClass specialization = containerClass(context);
		specialization.getGeneralizations().remove(input);
		return specialization;
	}

	/**
	 * The class held by the configured container channel, or a {@link TopLogicException} when it
	 * holds none.
	 */
	private TLClass containerClass(ReactContext context) {
		if (container(context) instanceof TLClass result) {
			return result;
		}
		throw new TopLogicException(I18NConstants.ERROR_NO_TARGET);
	}

	/**
	 * Deletes the model part passed as the action input (recursively, with its parts), or removes
	 * the generalization link an input of the type {@link #GENERALIZATION_TYPE} stands for.
	 *
	 * @return What the deleted element belonged to, see {@link Mode#DELETE}.
	 */
	private Object delete(Object input) {
		if (input instanceof TLModelPart part) {
			Object container = containerOf(part);
			TLModelUtil.deleteRecursive(part);
			return container;
		} else if (input instanceof TLObject link
				&& GENERALIZATION_TYPE.equals(TLModelUtil.qualifiedName(link.tType()))) {
			if (link.tValueByName(SPECIALIZATION) instanceof TLClass specialization) {
				specialization.getGeneralizations().remove(link.tValueByName(GENERALIZATION));
				return specialization;
			}
		}
		return null;
	}

	/**
	 * The model part the given one belongs to: the module of a type, the owner of an attribute or a
	 * classifier, nothing for a module or any other part.
	 */
	private static Object containerOf(TLModelPart part) {
		if (part instanceof TLType type) {
			return type.getModule();
		}
		if (part instanceof TLStructuredTypePart attribute) {
			return attribute.getOwner();
		}
		if (part instanceof TLClassifier classifier) {
			return classifier.getOwner();
		}
		return null;
	}

	/**
	 * The owner type to add an attribute to, derived from the selected model part (the type itself or
	 * one of its parts).
	 */
	private static TLStructuredType ownerType(Object container) {
		if (container instanceof TLStructuredType type) {
			return type;
		}
		if (container instanceof TLStructuredTypePart part) {
			return part.getOwner();
		}
		throw new TopLogicException(I18NConstants.ERROR_NO_TARGET);
	}

	/**
	 * The current value of the configured container channel, or {@code null} when unavailable.
	 */
	private Object container(ReactContext context) {
		if (_containerChannel == null || !(context instanceof ViewContext viewContext)
				|| !viewContext.hasChannel(_containerChannel)) {
			return null;
		}
		return viewContext.resolveChannel(new ChannelRef(_containerChannel)).get();
	}

	/**
	 * The trimmed, non-empty {@code name} attribute of the given input, or a {@link TopLogicException}
	 * when missing.
	 */
	private static String requireName(Object input) {
		String name = trimmed(input instanceof TLObject object ? object.tValueByName(NAME) : null);
		if (name == null) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_NAME);
		}
		return name;
	}

	/**
	 * The trimmed string value of the given input value, or {@code null} when it is empty.
	 */
	private static String trimmed(Object value) {
		String result = value == null ? null : value.toString().trim();
		return result == null || result.isEmpty() ? null : result;
	}

	/**
	 * The input as a transient model object, or a {@link TopLogicException} when it is none.
	 */
	private static TLObject asTransient(Object input) {
		if (input instanceof TLObject object) {
			return object;
		}
		throw new TopLogicException(I18NConstants.ERROR_NO_TARGET);
	}
}
