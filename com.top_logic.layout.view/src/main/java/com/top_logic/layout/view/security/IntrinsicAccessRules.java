/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;

/**
 * The {@link ViewExecutabilityRule}s an action performing a model operation brings of its own (see
 * {@link ViewAction#getIntrinsicRule()}), so that the command offering the action is hidden or
 * disabled before the operation would fail.
 *
 * <p>
 * The rules decide on the main argument of the command, and display a refusal as derived by
 * {@link ModelAccessPolicy}. An operation on the processed object(s) is checked on every element of
 * a collection, and without an object the command is disabled. A creation whose container channel
 * is empty, or without a created type, is nothing to check: the rule answers executable, so that it
 * composes with a rule like {@link NullInputDisabled} deciding about a missing input. A container
 * that is no single object is refused, the action creates in one container only.
 * </p>
 */
public final class IntrinsicAccessRules {

	private IntrinsicAccessRules() {
		// Static factories only.
	}

	/**
	 * The rule checking the given operation on the main argument of the command, see
	 * {@link ModelAccessPolicy#onObject(BoundCommandGroup, TLObject, TLStructuredTypePart)}.
	 *
	 * <p>
	 * A collection is checked on every element, like the action processes every element: a
	 * refusal that hides the command on one element hides it, otherwise the first refusal disables
	 * it. {@code null} or an empty collection is no object, which disables the command. An element
	 * that is no object is refused.
	 * </p>
	 *
	 * @param operation
	 *        The operation the action performs on the object(s) it receives.
	 */
	public static ViewExecutabilityRule onInput(BoundCommandGroup operation) {
		return input -> {
			Collection<?> elements;
			if (input == null) {
				elements = List.of();
			} else if (input instanceof Collection<?> collection) {
				elements = collection;
			} else {
				elements = List.of(input);
			}
			if (elements.isEmpty()) {
				return ExecutableState.NO_EXEC_NO_MODEL;
			}
			ExecutableState firstRefusal = null;
			for (Object element : elements) {
				ExecutableState state = element instanceof TLObject object
					? ModelAccessPolicy.onObject(operation, object, null)
					: ExecutableState.createDisabledState(ModelAccessPolicy.deniedReason(operation));
				if (state.isHidden()) {
					return state;
				}
				if (!state.isExecutable() && firstRefusal == null) {
					firstRefusal = state;
				}
			}
			return firstRefusal != null ? firstRefusal : ExecutableState.EXECUTABLE;
		};
	}

	/**
	 * The rule checking the creation of an object.
	 *
	 * <ul>
	 * <li>With a container, the creation is checked in the context of the container and, with a
	 * reference, together with the right to write that reference of the container, see
	 * {@link ModelAccessPolicy#onCreate(TLClass, TLStructuredTypePart, Object)}, the check of a
	 * {@link CreateAccess} as well. An empty container channel is nothing to check.</li>
	 * <li>Without a container, the creation is checked against the security root, see
	 * {@link ModelAccessPolicy#onType(BoundCommandGroup, TLClass)}.</li>
	 * </ul>
	 *
	 * <p>
	 * The created type is the given type, the type of the reference, or the type of the
	 * (transient) main argument of the command to be created, in this order.
	 * </p>
	 *
	 * @param type
	 *        The type of the created object, {@code null} for the type of the reference or of the
	 *        main argument.
	 * @param container
	 *        The channel holding the object in whose context the object is created, {@code null} for
	 *        a creation without context.
	 * @param reference
	 *        The name of the reference of the container the created object is added to,
	 *        {@code null} for none.
	 * @throws IllegalArgumentException
	 *         If a reference is given without a container.
	 */
	public static ViewExecutabilityRule creation(TLModelPartRef type, ChannelRef container, String reference) {
		if (reference != null && container == null) {
			throw new IllegalArgumentException("The reference '" + reference + "' of a creation requires a container.");
		}
		return new CreationRule(type, container, reference);
	}

	/**
	 * {@link ViewExecutabilityRule} checking the creation of an object, see
	 * {@link IntrinsicAccessRules#creation(TLModelPartRef, ChannelRef, String)}.
	 */
	private static final class CreationRule implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

		private final TLModelPartRef _typeRef;

		private final ChannelRef _containerRef;

		private final String _reference;

		private ViewChannel _containerChannel;

		CreationRule(TLModelPartRef type, ChannelRef container, String reference) {
			_typeRef = type;
			_containerRef = container;
			_reference = reference;
		}

		@Override
		public void bind(ViewContext context) {
			_containerChannel = _containerRef != null ? context.resolveChannel(_containerRef) : null;
		}

		/**
		 * Follows the channel holding the container: a new container is a new check.
		 */
		@Override
		public Runnable observe(Runnable revalidate) {
			if (_containerChannel == null) {
				return () -> {
					// Nothing observed.
				};
			}
			Registration registration =
				_containerChannel.addListener((sender, oldValue, newValue) -> revalidate.run());
			return registration::dispose;
		}

		@Override
		public ExecutableState isExecutable(Object input) {
			if (_containerRef != null) {
				return inContainer(input);
			}
			TLClass type;
			if (_typeRef != null) {
				type = type();
				if (type == null) {
					return ExecutableState.NOT_EXEC_HIDDEN;
				}
			} else if (!(input instanceof TLObject created) || (type = classOf(created)) == null) {
				// No object to be created, nothing to check.
				return ExecutableState.EXECUTABLE;
			}
			return ModelAccessPolicy.onType(SimpleBoundCommandGroup.CREATE, type);
		}

		private ExecutableState inContainer(Object input) {
			Object containerValue = _containerChannel != null ? _containerChannel.get() : null;
			if (containerValue == null) {
				// Nothing to check.
				return ExecutableState.EXECUTABLE;
			}
			if (!(containerValue instanceof TLObject container)) {
				// The action creates in a single container only.
				return ExecutableState.createDisabledState(I18NConstants.ERROR_CREATE_DENIED);
			}
			TLStructuredTypePart reference = null;
			if (_reference != null) {
				reference = container.tType().getPart(_reference);
				if (reference == null) {
					Logger.error("Type '" + container.tType() + "' has no reference '" + _reference
						+ "' to create an object in; the command is hidden.", IntrinsicAccessRules.class);
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
				if (!(input instanceof TLObject created) || (type = classOf(created)) == null) {
					// No object to be created, nothing to check.
					return ExecutableState.EXECUTABLE;
				}
			}
			return ModelAccessPolicy.onCreate(type, reference, container);
		}

		/**
		 * The configured type, {@code null} (reported) if it does not denote a class.
		 */
		private TLClass type() {
			TLType type = _typeRef.resolveType();
			if (type instanceof TLClass clazz) {
				return clazz;
			}
			Logger.error("Type '" + _typeRef.qualifiedName() + "' of a creation is not a class; the command is hidden.",
				IntrinsicAccessRules.class);
			return null;
		}

		private static TLClass classOf(TLObject object) {
			return object.tType() instanceof TLClass clazz ? clazz : null;
		}
	}

}
