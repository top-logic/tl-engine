/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.model.TLClass;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.CheckedObjects.Decision;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * The access check of a UI unit, instantiated from its {@link AccessControl}.
 *
 * <p>
 * The decision for the current user, as {@link ModelAccessPolicy} decides for a command:
 * </p>
 * <ul>
 * <li>{@link Display#HIDDEN}: the user can never hold the operation - they hold none of the roles
 * granted it on the type on any object (see
 * {@link ModelAccessPolicy#isOffered(BoundCommandGroup, TLClass)}), or, for a unit without
 * object, it is denied on the security root. The unit is not built at all.</li>
 * <li>{@link Display#EMPTY}: there is no object to check ({@code null} or an empty collection). The
 * unit is shown without content.</li>
 * <li>{@link Display#DENIED}: the operation is refused on a checked object, or a checked value is no
 * object of the declared type. Depending on {@link #getDenied()}, the unit is not displayed (see
 * {@link UnitHiding}), or it shows a "no access" placeholder instead of its content.</li>
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

	/** Description of a unit's access check in messages. */
	private static final String OWNER = "an '" + WithAccessControl.ACCESS_CONTROL + "'";

	private final OperationCheck _check;

	private final List<InputRef> _inputs;

	private final QueryExecutor _object;

	private final DeniedUnitDisplay _denied;

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
		_check = new OperationCheck(OWNER, config.getOperation(), config.getType());
		_inputs = config.getInputs();
		_denied = config.getDenied();
		_object = InputScript.compile(config, AccessControl.OBJECT);
		if (_object != null) {
			// The checked objects are decided by the check, not filtered by the user's rights before:
			// an object the user may not even discover is refused instead of vanishing.
			_object.disableSecurity();
		}
		if (_check.checkDeclaration(context, _object != null, AccessControl.OBJECT, AccessControl.TYPE)) {
			_check.checkApplies(context, null);
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

	/**
	 * Whether the unit is displayed to the current user at all.
	 *
	 * <p>
	 * A unit is withheld when the user can never hold the operation, independent of the object
	 * displayed: see {@link Display#HIDDEN}.
	 * </p>
	 */
	public boolean isOffered() {
		BoundCommandGroup operation = _check.operation();
		if (operation == null) {
			return false;
		}
		if (_object == null) {
			return OperationCheck.isOffered(operation, null);
		}
		TLClass type = _check.type();
		return type != null && OperationCheck.isOffered(operation, type);
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
		return new SecuredObject(_check.type(), _object, ChannelInputs.resolve(context, _inputs));
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
		BoundCommandGroup operation = _check.operation();
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
		Decision decision = _check.check(operation, type, secured.getValue());
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

	/**
	 * How the unit is displayed while its operation is refused on its object.
	 *
	 * @see AccessControl#getDenied()
	 */
	public DeniedUnitDisplay getDenied() {
		return _denied;
	}

	/**
	 * The reason a {@link Display#DENIED denied} unit gives in its placeholder.
	 */
	public ResKey deniedReason() {
		return _check.deniedReason();
	}

}
