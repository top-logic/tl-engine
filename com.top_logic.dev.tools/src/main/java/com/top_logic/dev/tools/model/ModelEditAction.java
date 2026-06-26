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
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link ViewAction} that mutates the application model from the model editor: creates a module,
 * type (class or enumeration) or attribute, or deletes a model part.
 *
 * <p>
 * The mutating operations create their result through the {@link TLModelUtil} primitives and return
 * the newly created part, so it can be written to the editor's selection channel. The created
 * element's name (and, for an attribute, its value type and cardinality) come from the transient
 * input object passed as the action input (the model of the dialog form after
 * {@code <store-form-state/>}); the container to create the element in comes from the configured
 * {@link Config#getContainer() container channel}. All operations must run inside a transaction
 * (wrap the action in {@code <with-transaction>}).
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
	 * The model mutation a {@link ModelEditAction} performs.
	 */
	public enum Mode {
		/** Create a new module named by the input. */
		CREATE_MODULE,

		/** Create a new class or enumeration named by the input in the container module. */
		CREATE_TYPE,

		/** Add a new property described by the input to the container type. */
		CREATE_ATTRIBUTE,

		/** Rename the container module or part to the name given by the input. */
		RENAME,

		/** Delete the model part passed as the action input. */
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
	public interface Config extends PolymorphicConfiguration<ModelEditAction> {

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
		 * {@link Mode#CREATE_ATTRIBUTE}. Unused by {@link Mode#CREATE_MODULE} and {@link Mode#DELETE}.
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
			case RENAME:
				return rename(context, input);
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
	 * Adds a new property described by the input to the container type.
	 */
	private TLStructuredTypePart createAttribute(ReactContext context, Object input) {
		TLObject model = asTransient(input);
		String name = requireName(model);
		TLStructuredType owner = ownerType(container(context));
		if (!(model.tValueByName(TYPE) instanceof TLType valueType)) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_TYPE);
		}
		TLProperty property = TLModelUtil.addProperty(owner, name, valueType);
		if (Boolean.TRUE.equals(model.tValueByName(MANDATORY))) {
			property.setMandatory(true);
		}
		if (Boolean.TRUE.equals(model.tValueByName(MULTIPLE))) {
			property.setMultiple(true);
		}
		return property;
	}

	/**
	 * Renames the container module or part to the name given by the input.
	 */
	private Object rename(ReactContext context, Object input) {
		String name = requireName(input);
		Object container = container(context);
		if (!(container instanceof TLNamed named)) {
			throw new TopLogicException(I18NConstants.ERROR_NO_TARGET);
		}
		named.setName(name);
		return container;
	}

	/**
	 * Deletes the model part passed as the action input (recursively, with its parts).
	 */
	private Object delete(Object input) {
		if (input instanceof TLModelPart part) {
			TLModelUtil.deleteRecursive(part);
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
		Object value = input instanceof TLObject object ? object.tValueByName(NAME) : null;
		String name = value == null ? null : value.toString().trim();
		if (name == null || name.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_NAME);
		}
		return name;
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
