/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} offering a command only where the model access rights allow the
 * current user the operation the command performs.
 *
 * <p>
 * The {@link Config#getOperation() operation} is a command group: {@code Read}, {@code Write},
 * {@code Create}, {@code Delete}, or a custom business operation such as {@code Approve}. What it is
 * checked on depends on the configuration:
 * </p>
 * <ul>
 * <li>An object: the value of the {@link Config#getObject() object channel}, or the command input
 * when none is given. With an {@link Config#getAttribute() attribute}, the operation is checked on
 * that attribute of the object.</li>
 * <li>A type: with a {@link Config#getType() type} and no {@link Config#getContainer() container},
 * the operation is checked on the objects of the type, against the security root. For
 * {@code Create}, that is whether the user may create objects of the type at all.</li>
 * <li>A creation in a container: with a {@link Config#getContainer() container}, the operation is
 * {@code Create}, checked in the context of the container and, with a
 * {@link Config#getReference() reference}, together with the write right on that reference of the
 * container.</li>
 * </ul>
 *
 * <p>
 * The operation {@code Create} is never checked on an object: an object that exists is not created.
 * Without a {@link Config#getType() type} and a {@link Config#getReference() reference} to take the
 * created type from, the object - the value of the {@link Config#getObject() object channel}, or the
 * command input - is the (transient) object to be created, and the check is the creation of an object
 * of its type: against the security root, or in the context of the container.
 * </p>
 *
 * <p>
 * An empty object or container is nothing to check: the rule answers executable, so that it
 * composes with a rule like {@link NullInputDisabled} that decides about a missing input.
 * </p>
 *
 * <p>
 * A refused command is hidden or disabled as {@link Config#getDenied() configured}; by default, as
 * derived by {@link ModelAccessPolicy}.
 * </p>
 *
 * <p>
 * Example: offer an approve command only for the objects the user may approve.
 * </p>
 *
 * <pre>
 * &lt;executability&gt;
 *   &lt;null-input-disabled/&gt;
 *   &lt;model-access operation="Approve"/&gt;
 * &lt;/executability&gt;
 * </pre>
 */
public class ModelAccessRule implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

	/**
	 * Configuration for {@link ModelAccessRule}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends ViewExecutabilityRule.Config {

		/** Tag name of a {@link ModelAccessRule} in a list of executability rules. */
		String TAG_NAME = "model-access";

		/** Configuration name for {@link #getOperation()}. */
		String OPERATION = "operation";

		/** Configuration name for {@link #getObject()}. */
		String OBJECT = "object";

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getContainer()}. */
		String CONTAINER = "container";

		/** Configuration name for {@link #getReference()}. */
		String REFERENCE = "reference";

		/** Configuration name for {@link #getDenied()}. */
		String DENIED = "denied";

		@Override
		@ClassDefault(ModelAccessRule.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();

		/**
		 * The operation the command performs.
		 *
		 * <p>
		 * The name of a command group: {@code Read}, {@code Write}, {@code Create}, {@code Delete},
		 * or a custom command group registered for business operations.
		 * </p>
		 */
		@Name(OPERATION)
		@Mandatory
		CommandGroupReference getOperation();

		/**
		 * @see #getOperation()
		 */
		void setOperation(CommandGroupReference value);

		/**
		 * Channel holding the object the operation is performed on.
		 *
		 * <p>
		 * When unset, the operation is performed on the command input.
		 * </p>
		 *
		 * <p>
		 * For the operation {@code Create}, this is the (transient) object to be created: the check
		 * is the creation of an object of its type, unless a {@link #getType() type} or a
		 * {@link #getReference() reference} gives the created type.
		 * </p>
		 */
		@Name(OBJECT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getObject();

		/**
		 * @see #getObject()
		 */
		void setObject(ChannelRef value);

		/**
		 * Name of the attribute of the object the operation is performed on.
		 *
		 * <p>
		 * When set, the operation is checked on this attribute of the object (e.g. {@code Write} on
		 * a single attribute). The name is looked up in the type of the object.
		 * </p>
		 */
		@Name(ATTRIBUTE)
		@Nullable
		String getAttribute();

		/**
		 * @see #getAttribute()
		 */
		void setAttribute(String value);

		/**
		 * The type of the objects the operation is performed on.
		 *
		 * <p>
		 * Without a {@link #getContainer() container}, the operation is checked on the type instead
		 * of an object, against the security root. In a creation in a container, it is the type of
		 * the created object; there it defaults to the type of the {@link #getReference()
		 * reference}. For the operation {@code Create} without a type, the created type is taken
		 * from the {@link #getObject() object} to be created.
		 * </p>
		 */
		@Name(TYPE)
		@Nullable
		TLModelPartRef getType();

		/**
		 * @see #getType()
		 */
		void setType(TLModelPartRef value);

		/**
		 * Channel holding the object in whose context an object is created.
		 *
		 * <p>
		 * Only for the operation {@code Create}. The created type is the {@link #getType() type},
		 * the type of the {@link #getReference() reference}, or the type of the
		 * {@link #getObject() object} to be created, in this order.
		 * </p>
		 */
		@Name(CONTAINER)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getContainer();

		/**
		 * @see #getContainer()
		 */
		void setContainer(ChannelRef value);

		/**
		 * Name of the (composition) reference of the {@link #getContainer() container} the created
		 * object is added to.
		 *
		 * <p>
		 * Creating an object there requires the right to write this reference of the container, in
		 * addition to the right to create the object. The name is looked up in the type of the
		 * container.
		 * </p>
		 */
		@Name(REFERENCE)
		@Nullable
		String getReference();

		/**
		 * @see #getReference()
		 */
		void setReference(String value);

		/**
		 * How the command is displayed when the operation is refused.
		 *
		 * <p>
		 * When unset, a command whose refusal depends on a concrete object (the object operated on,
		 * or the container) is disabled and gives the refusal as its reason. A command the user may
		 * never execute - the check runs against the security root, the type grants the operation to
		 * no role, or the user is restricted - is hidden.
		 * </p>
		 */
		@Name(DENIED)
		@Nullable
		@NullDefault
		DeniedDisplay getDenied();

		/**
		 * @see #getDenied()
		 */
		void setDenied(DeniedDisplay value);
	}

	private final CommandGroupReference _operationRef;

	private final ChannelRef _objectRef;

	private final String _attribute;

	private final TLModelPartRef _typeRef;

	private final ChannelRef _containerRef;

	private final String _reference;

	private final DeniedDisplay _denied;

	private ViewChannel _objectChannel;

	private ViewChannel _containerChannel;

	/**
	 * Creates a {@link ModelAccessRule} from configuration.
	 */
	@CalledByReflection
	public ModelAccessRule(InstantiationContext context, Config config) {
		this(config);
		String problem = checkConfig(config);
		if (problem != null) {
			context.error(problem);
		}
	}

	private ModelAccessRule(Config config) {
		_operationRef = config.getOperation();
		_objectRef = config.getObject();
		_attribute = config.getAttribute();
		_typeRef = config.getType();
		_containerRef = config.getContainer();
		_reference = config.getReference();
		_denied = config.getDenied();
	}

	/**
	 * Creates a {@link ModelAccessRule} for the given configuration, built in code.
	 *
	 * <p>
	 * An action performing a model operation uses this to bring the matching check as its own
	 * executability rule.
	 * </p>
	 *
	 * @throws IllegalArgumentException
	 *         If the configuration does not describe a check.
	 */
	public static ModelAccessRule create(Config config) {
		String problem = checkConfig(config);
		if (problem != null) {
			throw new IllegalArgumentException(problem);
		}
		return new ModelAccessRule(config);
	}

	/**
	 * Creates a new {@link Config} for the given operation, to be completed and passed to
	 * {@link #create(Config)}.
	 */
	public static Config newConfig(BoundCommandGroup operation) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setOperation(new CommandGroupReference(operation.getID()));
		return config;
	}

	/**
	 * The rule checking the given operation on the command input.
	 */
	public static ModelAccessRule onInput(BoundCommandGroup operation) {
		return create(newConfig(operation));
	}

	/**
	 * The rule checking the creation of an object.
	 *
	 * @param type
	 *        The type of the created object, {@code null} for the type of the given reference or,
	 *        without a reference, the type of the (transient) command input to be created.
	 * @param container
	 *        The channel holding the object in whose context the object is created, {@code null} for
	 *        a creation without context (checked against the security root).
	 * @param reference
	 *        The name of the reference of the container the created object is added to,
	 *        {@code null} for none.
	 */
	public static ModelAccessRule creation(TLModelPartRef type, ChannelRef container, String reference) {
		Config config = newConfig(SimpleBoundCommandGroup.CREATE);
		config.setType(type);
		config.setContainer(container);
		config.setReference(reference);
		return create(config);
	}

	/**
	 * The problem with the given configuration, {@code null} if it describes a check.
	 */
	private static String checkConfig(Config config) {
		boolean creationInContainer = config.getContainer() != null;
		if (config.getReference() != null && !creationInContainer) {
			return "The '" + Config.REFERENCE + "' of a '" + Config.TAG_NAME + "' rule requires a '"
				+ Config.CONTAINER + "'.";
		}
		if (creationInContainer) {
			if (!SimpleBoundCommandGroup.CREATE.getID().equals(config.getOperation().id())) {
				return "A '" + Config.TAG_NAME + "' rule with a '" + Config.CONTAINER + "' checks a creation; its '"
					+ Config.OPERATION + "' must be '" + SimpleBoundCommandGroup.CREATE.getID() + "'.";
			}
		}
		boolean create = SimpleBoundCommandGroup.CREATE.getID().equals(config.getOperation().id());
		if (config.getAttribute() != null && (create || config.getType() != null)) {
			return "The '" + Config.ATTRIBUTE + "' of a '" + Config.TAG_NAME
				+ "' rule applies to a check on an object, not on a '" + Config.TYPE + "' or a creation.";
		}
		if (config.getObject() != null
			&& (config.getType() != null || config.getReference() != null)) {
			return "The '" + Config.OBJECT + "' of a '" + Config.TAG_NAME
				+ "' rule is not checked when a '" + Config.TYPE + "' or a '" + Config.REFERENCE
				+ "' gives what is checked.";
		}
		return null;
	}

	@Override
	public void bind(ViewContext context) {
		_objectChannel = _objectRef != null ? context.resolveChannel(_objectRef) : null;
		_containerChannel = _containerRef != null ? context.resolveChannel(_containerRef) : null;
		if (operation() == null) {
			Logger.error("Command group '" + _operationRef.id() + "' of a '" + Config.TAG_NAME
				+ "' rule is not registered; the command is hidden.", ModelAccessRule.class);
		}
	}

	/**
	 * Follows the channels holding the object and the container: a new value is a new check.
	 */
	@Override
	public Runnable observe(Runnable revalidate) {
		List<Runnable> stops = new ArrayList<>();
		follow(_objectChannel, revalidate, stops);
		follow(_containerChannel, revalidate, stops);
		return () -> stops.forEach(Runnable::run);
	}

	private static void follow(ViewChannel channel, Runnable revalidate, List<Runnable> stops) {
		if (channel == null) {
			return;
		}
		ChannelListener listener = (sender, oldValue, newValue) -> revalidate.run();
		channel.addListener(listener);
		stops.add(() -> channel.removeListener(listener));
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		BoundCommandGroup operation = operation();
		if (operation == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		if (_containerRef != null) {
			return creationInContainer(input);
		}
		if (_typeRef != null) {
			TLClass type = type();
			if (type == null) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			return ModelAccessPolicy.onType(operation, type, _denied);
		}
		Object value = objectValue(input);
		if (!(value instanceof TLObject object)) {
			// Nothing to check.
			return ExecutableState.EXECUTABLE;
		}
		if (ModelAccessPolicy.isCreate(operation)) {
			TLClass createdType = classOf(object);
			if (createdType == null) {
				// Not an object of a class, nothing to check.
				return ExecutableState.EXECUTABLE;
			}
			return ModelAccessPolicy.onType(operation, createdType, _denied);
		}
		TLStructuredTypePart attribute = null;
		if (_attribute != null) {
			attribute = object.tType().getPart(_attribute);
			if (attribute == null) {
				Logger.error("Type '" + object.tType() + "' has no attribute '" + _attribute + "' checked by a '"
					+ Config.TAG_NAME + "' rule; the command is hidden.", ModelAccessRule.class);
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
		}
		return ModelAccessPolicy.onObject(operation, object, attribute, _denied);
	}

	private ExecutableState creationInContainer(Object input) {
		if (!(valueOf(_containerChannel) instanceof TLObject container)) {
			// Nothing to check.
			return ExecutableState.EXECUTABLE;
		}
		TLStructuredTypePart reference = null;
		if (_reference != null) {
			reference = container.tType().getPart(_reference);
			if (reference == null) {
				Logger.error("Type '" + container.tType() + "' has no reference '" + _reference + "' checked by a '"
					+ Config.TAG_NAME + "' rule; the command is hidden.", ModelAccessRule.class);
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
		}
		TLClass type = null;
		if (_typeRef != null) {
			type = type();
			if (type == null) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
		} else if (reference == null) {
			if (!(objectValue(input) instanceof TLObject created) || (type = classOf(created)) == null) {
				// No object to be created, nothing to check.
				return ExecutableState.EXECUTABLE;
			}
		}
		return ModelAccessPolicy.createIn(container, reference, type, _denied);
	}

	/**
	 * The object the operation is performed on: the value of the object channel, or the command
	 * input.
	 */
	private Object objectValue(Object input) {
		return _objectRef != null ? valueOf(_objectChannel) : input;
	}

	private static Object valueOf(ViewChannel channel) {
		return channel != null ? channel.get() : null;
	}

	private static TLClass classOf(TLObject object) {
		return object.tType() instanceof TLClass clazz ? clazz : null;
	}

	private BoundCommandGroup operation() {
		return _operationRef.resolve();
	}

	/**
	 * The configured type, {@code null} (reported) if it does not denote a class.
	 */
	private TLClass type() {
		TLType type = _typeRef.resolveType();
		if (type instanceof TLClass clazz) {
			return clazz;
		}
		Logger.error("Type '" + _typeRef.qualifiedName() + "' of a '" + Config.TAG_NAME
			+ "' rule is not a class; the command is hidden.", ModelAccessRule.class);
		return null;
	}
}
