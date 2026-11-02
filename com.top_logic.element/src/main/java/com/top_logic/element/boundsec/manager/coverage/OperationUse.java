/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.coverage;

import java.util.Objects;

import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * A place of the application that checks an operation on an object: a view unit, a command, a
 * form, a privileged script.
 *
 * <p>
 * The user does not grant the operation, it only names it. Whether the current user may perform
 * it is decided by the grants of the model access rights on the type of the checked object. Which
 * type that is, is known statically only where the user declares it, see {@link #target()}.
 * </p>
 *
 * @param kind
 *        What kind of user checks the operation.
 * @param operation
 *        The name of the checked operation, a command group. It need not name a registered command
 *        group, see {@link #resolveOperation()}.
 * @param target
 *        What the operation is checked on.
 * @param type
 *        The type of the checked object for a {@link Target#TYPE} target, <code>null</code>
 *        otherwise.
 * @param user
 *        A description of the user that a developer finds it by, e.g. the path of a view followed
 *        by the element checking the operation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public record OperationUse(Kind kind, String operation, Target target, TLClass type, String user) {

	/**
	 * What kind of place checks an operation.
	 */
	public enum Kind {
		/** A UI unit displayed only if the operation is allowed. */
		VIEW,

		/** A command executable only if the operation is allowed. */
		COMMAND,

		/** A form whose edit mode requires the operation. */
		FORM,

		/** A TL-Script function callable only if the operation is allowed. */
		SCRIPT;
	}

	/**
	 * What an operation is checked on.
	 */
	public enum Target {
		/** An object of the {@link OperationUse#type() declared type}. */
		TYPE,

		/** The security root. */
		ROOT,

		/** An object whose type is not declared. */
		UNKNOWN;
	}

	/**
	 * Creates a {@link OperationUse}.
	 */
	public OperationUse {
		Objects.requireNonNull(kind);
		Objects.requireNonNull(operation);
		Objects.requireNonNull(target);
		Objects.requireNonNull(user);
		if ((target == Target.TYPE) != (type != null)) {
			throw new IllegalArgumentException("A type is given exactly for a target of a type: " + target + ", " + type);
		}
	}

	/**
	 * A use checking the given operation on objects of the given type.
	 */
	public static OperationUse onType(Kind kind, String operation, TLClass type, String user) {
		return new OperationUse(kind, operation, Target.TYPE, Objects.requireNonNull(type), user);
	}

	/**
	 * A use checking the given operation on the security root.
	 */
	public static OperationUse onRoot(Kind kind, String operation, String user) {
		return new OperationUse(kind, operation, Target.ROOT, null, user);
	}

	/**
	 * A use checking the given operation on objects of a type that is not declared.
	 */
	public static OperationUse onUnknown(Kind kind, String operation, String user) {
		return new OperationUse(kind, operation, Target.UNKNOWN, null, user);
	}

	/**
	 * The registered command group the {@link #operation()} names.
	 *
	 * @return <code>null</code> for an unknown operation.
	 */
	public BoundCommandGroup resolveOperation() {
		return CommandGroupRegistry.getInstance().getGroup(operation);
	}

	/**
	 * Whether this use may check its operation on an object of the given type.
	 *
	 * <p>
	 * A use of a type checks objects of that type and of its specializations. A use with an
	 * {@link Target#UNKNOWN unknown} target may check an object of any type, a use of the security
	 * root an object of none.
	 * </p>
	 */
	public boolean mayCheck(TLClass objectType) {
		return switch (target) {
			case TYPE -> TLModelUtil.isGeneralization(type, objectType);
			case ROOT -> false;
			case UNKNOWN -> true;
		};
	}

	@Override
	public String toString() {
		String checked = switch (target) {
			case TYPE -> TLModelUtil.qualifiedName(type);
			case ROOT -> "root";
			case UNKNOWN -> "?";
		};
		return kind + " " + user + ": " + operation + " on " + checked;
	}

}
