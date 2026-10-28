/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.security.ApplicableOperations;
import com.top_logic.model.security.CheckedObjects;
import com.top_logic.model.security.CheckedObjects.Decision;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;

/**
 * Which operation is checked on which object: the part of an access check shared by a UI unit (see
 * {@link UnitAccess}), an operation in the access of a command (see {@link OperationAccess}) and
 * the edit mode of a form.
 *
 * <p>
 * The declaration names an operation (a command group) and, when the checked object is computed by
 * the declaring element itself, the static {@link #getTypeRef() type} of that object. Without an
 * object of its own, the check falls back to an object given by the context: the object of the
 * enclosing unit, or the security root.
 * </p>
 *
 * <p>
 * The checked value is decided by {@link CheckedObjects}: a single object, every element of a
 * collection, and no object for {@code null} or an empty collection. An element that is no instance
 * of the checked type is denied.
 * </p>
 */
public final class OperationCheck {

	private final String _owner;

	private final CommandGroupReference _operationRef;

	private final TLModelPartRef _typeRef;

	/**
	 * Creates an {@link OperationCheck}.
	 *
	 * @param owner
	 *        Description of the declaring element in messages, e.g. {@code "an 'access-control'"}.
	 * @param operation
	 *        The checked operation.
	 * @param type
	 *        The static type of the object the declaring element computes, {@code null} when it
	 *        computes none.
	 */
	public OperationCheck(String owner, CommandGroupReference operation, TLModelPartRef type) {
		_owner = owner;
		_operationRef = operation;
		_typeRef = type;
	}

	/**
	 * The declared static type of the checked object, {@code null} when the declaring element
	 * computes no object of its own.
	 */
	public TLModelPartRef getTypeRef() {
		return _typeRef;
	}

	/**
	 * Reports an object expression without a type, or a type without an object expression.
	 *
	 * @param context
	 *        The context reporting the problem.
	 * @param hasObject
	 *        Whether the declaring element computes the checked object itself.
	 * @param objectName
	 *        The configuration name of the object expression.
	 * @param typeName
	 *        The configuration name of the type.
	 * @return Whether the declaration is consistent.
	 */
	public boolean checkDeclaration(InstantiationContext context, boolean hasObject, String objectName,
			String typeName) {
		if (hasObject && _typeRef == null) {
			context.error("The '" + objectName + "' of " + _owner + " requires the '" + typeName + "' of the object.");
			return false;
		}
		if (!hasObject && _typeRef != null) {
			context.error("The '" + typeName + "' of " + _owner + " describes its '" + objectName + "', which is missing.");
			return false;
		}
		return true;
	}

	/**
	 * Checks against the model and the security configuration that the operation exists and applies
	 * to the checked object.
	 *
	 * <p>
	 * Does nothing while the {@link SecurityConfigurationService} is not running.
	 * </p>
	 *
	 * @param context
	 *        The context reporting the problems.
	 * @param fallback
	 *        The object checked when no {@link #getTypeRef() type} is declared: the object of the
	 *        enclosing unit, {@code null} for the security root.
	 */
	public void checkApplies(InstantiationContext context, SecuredObject fallback) {
		if (!SecurityConfigurationService.Module.INSTANCE.isActive()) {
			return;
		}
		BoundCommandGroup operation = _operationRef.resolve();
		if (operation == null) {
			context.error("The operation '" + _operationRef.id() + "' of " + _owner + " is no registered command group.");
			return;
		}
		ApplicableOperations operations;
		String target;
		if (_typeRef != null) {
			TLClass type = resolveClass(context);
			if (type == null) {
				return;
			}
			operations = ModelAccessRights.getInstance().getOperations(type);
			target = "the type '" + _typeRef.qualifiedName() + "'";
		} else if (fallback != null && !fallback.isSecurityRoot()) {
			operations = fallback.getOperations();
			target = "the type '" + TLModelUtil.qualifiedName(fallback.getType()) + "' of the enclosing unit";
		} else {
			operations = ModelAccessRights.getInstance().getObjectOperations(SecuredObject.securityRootObject());
			target = "the security root";
		}
		if (!operations.appliesTo(operation)) {
			context.error("The operation '" + operation.getID() + "' of " + _owner + " does not apply to " + target
				+ "; declare it in the <security-config>.");
		}
	}

	private TLClass resolveClass(InstantiationContext context) {
		TLType type;
		try {
			type = _typeRef.resolveType();
		} catch (RuntimeException ex) {
			context.error("The type '" + _typeRef.qualifiedName() + "' of " + _owner + " does not exist.", ex);
			return null;
		}
		if (type instanceof TLClass clazz) {
			return clazz;
		}
		context.error("The type '" + _typeRef.qualifiedName() + "' of " + _owner + " is no class.");
		return null;
	}

	/**
	 * The checked operation, {@code null} (reported) when no such command group is registered.
	 */
	public BoundCommandGroup operation() {
		BoundCommandGroup result = _operationRef.resolve();
		if (result == null) {
			Logger.error("The operation '" + _operationRef.id() + "' of " + _owner
				+ " is no registered command group; access is refused.", OperationCheck.class);
		}
		return result;
	}

	/**
	 * The declared {@link #getTypeRef() type}, {@code null} (reported) when it does not denote a
	 * class.
	 */
	public TLClass type() {
		TLType type = _typeRef.resolveType();
		if (type instanceof TLClass clazz) {
			return clazz;
		}
		Logger.error("The type '" + _typeRef.qualifiedName() + "' of " + _owner + " is no class; access is refused.",
			OperationCheck.class);
		return null;
	}

	/**
	 * Whether the current user can hold the given operation on objects of the given type at all.
	 *
	 * @param operation
	 *        The checked operation.
	 * @param type
	 *        The static type of the checked object, {@code null} for the security root: then the
	 *        operation is decided on the security root itself.
	 *
	 * @see ModelAccessPolicy#isOffered(BoundCommandGroup, TLClass)
	 */
	public static boolean isOffered(BoundCommandGroup operation, TLClass type) {
		if (type == null) {
			return ModelAccessRights.getInstance().isAllowed(TLContext.currentUser(),
				SecuredObject.securityRootObject(), operation);
		}
		return ModelAccessPolicy.isOffered(operation, type);
	}

	/**
	 * Decides the given operation on the given checked value for the current user.
	 *
	 * @param operation
	 *        The checked operation.
	 * @param type
	 *        The static type of the checked object(s); an element that is no instance of it is
	 *        denied.
	 * @param value
	 *        A single object, a collection of objects, or {@code null}.
	 */
	public Decision check(BoundCommandGroup operation, TLClass type, Object value) {
		Person user = TLContext.currentUser();
		return CheckedObjects.check(value, element -> checkElement(user, element, operation, type));
	}

	/**
	 * The state of a command performing the given operation on the given checked value.
	 *
	 * @see ModelAccessPolicy#onChecked(BoundCommandGroup, TLClass, Object, java.util.function.Function)
	 */
	public ExecutableState state(BoundCommandGroup operation, TLClass type, Object value) {
		Person user = TLContext.currentUser();
		return ModelAccessPolicy.onChecked(operation, type, value,
			element -> checkElement(user, element, operation, type));
	}

	/**
	 * The state of a command performing the given operation on the security root: hidden unless
	 * the operation is allowed there.
	 */
	public static ExecutableState rootState(BoundCommandGroup operation) {
		return isOffered(operation, null) ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}

	private Decision checkElement(Person user, Object element, BoundCommandGroup operation, TLClass type) {
		if (element instanceof TLObject object && !TLModelUtil.isCompatibleInstance(type, object)) {
			Logger.warn("Access to '" + object + "' denied: it is no instance of the type '" + TLModelUtil.qualifiedName(type)
				+ "' declared by " + _owner + " checking the operation '" + operation.getID() + "'.",
				OperationCheck.class);
			return Decision.denied(element);
		}
		return CheckedObjects.checkElement(user, element, operation);
	}

	/**
	 * The reason given for refusing the operation on a concrete object.
	 */
	public ResKey deniedReason() {
		BoundCommandGroup operation = _operationRef.resolve();
		return operation == null ? I18NConstants.ERROR_NO_ACCESS : ModelAccessPolicy.deniedReason(operation);
	}

}
