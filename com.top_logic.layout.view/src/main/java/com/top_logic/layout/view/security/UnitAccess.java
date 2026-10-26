/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.ApplicableOperations;
import com.top_logic.model.security.CheckedObjects;
import com.top_logic.model.security.CheckedObjects.Decision;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.util.TLContext;

/**
 * The access check of a UI unit, instantiated from its {@link AccessControl}.
 *
 * <p>
 * The decision for the current user, as {@link ModelAccessPolicy} decides for a command:
 * </p>
 * <ul>
 * <li>{@link Display#HIDDEN}: the user can never hold the operation - no role is granted it on the
 * type (see {@link ModelAccessPolicy#isOffered(BoundCommandGroup, TLClass)}), or, for a unit without
 * object, it is denied on the security root. The unit is not built at all.</li>
 * <li>{@link Display#EMPTY}: there is no object to check ({@code null} or an empty collection). The
 * unit is shown without content.</li>
 * <li>{@link Display#DENIED}: the operation is refused on a checked object, or a checked value is no
 * object of the declared type. The unit shows a "no access" placeholder instead of its content.</li>
 * <li>{@link Display#CONTENT}: the operation is allowed on every checked object.</li>
 * </ul>
 *
 * <p>
 * Instances are created once per loaded view and shared by the sessions displaying it; the
 * per-session part is the {@link SecuredObject} {@link #bind(ViewContext) bound} to the channels of
 * a session.
 * </p>
 */
public final class UnitAccess {

	/**
	 * How a UI unit with an {@link AccessControl} is displayed.
	 */
	public enum Display {

		/** The unit is not displayed at all. */
		HIDDEN,

		/** The unit is displayed without content: there is no object to check. */
		EMPTY,

		/** The unit displays a placeholder telling that its content is not accessible. */
		DENIED,

		/** The unit displays its content. */
		CONTENT;

	}

	private final CommandGroupReference _operationRef;

	private final List<InputRef> _inputs;

	private final QueryExecutor _object;

	private final TLModelPartRef _typeRef;

	/**
	 * Creates a {@link UnitAccess} from its configuration.
	 *
	 * <p>
	 * When the services deciding about the access rights are running, the configuration is checked
	 * against the model and the security configuration: the operation must exist and apply to the
	 * declared type, or, without an object, to the security root.
	 * </p>
	 */
	private UnitAccess(InstantiationContext context, AccessControl config) {
		_operationRef = config.getOperation();
		_inputs = config.getInputs();
		_object = InputScript.compile(config, AccessControl.OBJECT);
		if (_object != null) {
			// The checked objects are decided by the check, not filtered by the user's rights before:
			// an object the user may not even discover is refused instead of vanishing.
			_object.disableSecurity();
		}
		_typeRef = config.getType();

		if (_object != null && _typeRef == null) {
			context.error("An '" + WithAccessControl.ACCESS_CONTROL + "' with an '" + AccessControl.OBJECT
				+ "' requires the '" + AccessControl.TYPE + "' of the object.");
			return;
		}
		if (_object == null && _typeRef != null) {
			context.error("The '" + AccessControl.TYPE + "' of an '" + WithAccessControl.ACCESS_CONTROL
				+ "' describes its '" + AccessControl.OBJECT + "', which is missing.");
			return;
		}
		if (SecurityConfigurationService.Module.INSTANCE.isActive()) {
			checkOperation(context);
		}
	}

	/**
	 * Creates the {@link UnitAccess} of the given configuration.
	 *
	 * @param context
	 *        The context reporting configuration errors.
	 * @param config
	 *        The access control of a unit, {@code null} for a unit without one.
	 * @return {@code null} for a unit without access control.
	 */
	public static UnitAccess create(InstantiationContext context, AccessControl config) {
		return config == null ? null : new UnitAccess(context, config);
	}

	private void checkOperation(InstantiationContext context) {
		BoundCommandGroup operation = _operationRef.resolve();
		if (operation == null) {
			context.error("The operation '" + _operationRef.id() + "' of an '" + WithAccessControl.ACCESS_CONTROL
				+ "' is no registered command group.");
			return;
		}
		ApplicableOperations operations;
		String target;
		if (_typeRef == null) {
			operations = ModelAccessRights.getInstance().getObjectOperations(SecuredObject.securityRootObject());
			target = "the security root";
		} else {
			TLClass type = resolveClass(context);
			if (type == null) {
				return;
			}
			operations = ModelAccessRights.getInstance().getOperations(type);
			target = "the type '" + _typeRef.qualifiedName() + "'";
		}
		if (!operations.appliesTo(operation)) {
			context.error("The operation '" + operation.getID() + "' of an '" + WithAccessControl.ACCESS_CONTROL
				+ "' does not apply to " + target + "; declare it in the <security-config>.");
		}
	}

	private TLClass resolveClass(InstantiationContext context) {
		TLType type;
		try {
			type = _typeRef.resolveType();
		} catch (RuntimeException ex) {
			context.error("The type '" + _typeRef.qualifiedName() + "' of an '" + WithAccessControl.ACCESS_CONTROL
				+ "' does not exist.", ex);
			return null;
		}
		if (type instanceof TLClass clazz) {
			return clazz;
		}
		context.error("The type '" + _typeRef.qualifiedName() + "' of an '" + WithAccessControl.ACCESS_CONTROL
			+ "' is no class.");
		return null;
	}

	/**
	 * The operation checked, {@code null} (reported) when no such command group is registered.
	 */
	private BoundCommandGroup operation() {
		BoundCommandGroup result = _operationRef.resolve();
		if (result == null) {
			Logger.error("The operation '" + _operationRef.id() + "' of an '" + WithAccessControl.ACCESS_CONTROL
				+ "' is no registered command group; the unit is hidden.", UnitAccess.class);
		}
		return result;
	}

	/**
	 * The checked type, {@code null} (reported) when it does not denote a class.
	 */
	private TLClass type() {
		TLType type = _typeRef.resolveType();
		if (type instanceof TLClass clazz) {
			return clazz;
		}
		Logger.error("The type '" + _typeRef.qualifiedName() + "' of an '" + WithAccessControl.ACCESS_CONTROL
			+ "' is no class; the unit is hidden.", UnitAccess.class);
		return null;
	}

	/**
	 * Whether the unit is displayed to the current user at all.
	 *
	 * <p>
	 * A unit is withheld when the user can never hold the operation, independent of the object
	 * displayed: see {@link Display#HIDDEN}.
	 * </p>
	 */
	public boolean isOffered() {
		BoundCommandGroup operation = operation();
		if (operation == null) {
			return false;
		}
		if (_object == null) {
			return ModelAccessRights.getInstance().isAllowed(TLContext.currentUser(),
				SecuredObject.securityRootObject(), operation);
		}
		TLClass type = type();
		return type != null && ModelAccessPolicy.isOffered(operation, type);
	}

	/**
	 * The {@link SecuredObject} of this unit for the session the given context belongs to.
	 *
	 * @param context
	 *        The context the input channels are resolved in.
	 */
	public SecuredObject bind(ViewContext context) {
		if (_object == null) {
			return SecuredObject.securityRoot();
		}
		return new SecuredObject(type(), _object, ChannelInputs.resolve(context, _inputs));
	}

	/**
	 * Whether the decision depends on the values of input channels, so that it has to be
	 * re-evaluated when they change.
	 *
	 * <p>
	 * A unit without object is decided on the security root, once by {@link #isOffered()}.
	 * </p>
	 */
	public boolean isReactive() {
		return _object != null;
	}

	/**
	 * Decides how the content of a unit {@link #isOffered() offered} to the current user is
	 * displayed for the current value of the given object.
	 *
	 * @param secured
	 *        The object of this unit, see {@link #bind(ViewContext)}.
	 */
	public Display decide(SecuredObject secured) {
		BoundCommandGroup operation = operation();
		if (operation == null) {
			return Display.HIDDEN;
		}
		if (secured.isSecurityRoot()) {
			return isOffered() ? Display.CONTENT : Display.HIDDEN;
		}
		TLClass type = secured.getType();
		if (type == null) {
			return Display.HIDDEN;
		}
		Person user = TLContext.currentUser();
		Decision decision = CheckedObjects.check(secured.getValue(), element -> checkElement(user, element, operation, type));
		switch (decision.getOutcome()) {
			case ALLOWED:
				return Display.CONTENT;
			case NO_OBJECT:
				return Display.EMPTY;
			case DENIED:
			case INVALID:
				return Display.DENIED;
		}
		throw new IllegalArgumentException("No such outcome: " + decision.getOutcome());
	}

	private Decision checkElement(Person user, Object element, BoundCommandGroup operation, TLClass type) {
		if (element instanceof TLObject object && !TLModelUtil.isCompatibleInstance(type, object)) {
			Logger.warn("Access to '" + object + "' denied: it is no instance of the type '"
				+ _typeRef.qualifiedName() + "' declared by the '" + WithAccessControl.ACCESS_CONTROL
				+ "' checking the operation '" + operation.getID() + "'.", UnitAccess.class);
			return Decision.denied(element);
		}
		return CheckedObjects.checkElement(user, element, operation);
	}

	/**
	 * The reason a {@link Display#DENIED denied} unit gives in its placeholder.
	 */
	public ResKey deniedReason() {
		BoundCommandGroup operation = operation();
		return operation == null ? I18NConstants.ERROR_NO_ACCESS : ModelAccessPolicy.deniedReason(operation);
	}

}
