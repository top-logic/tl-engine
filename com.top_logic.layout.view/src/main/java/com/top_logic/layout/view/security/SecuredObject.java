/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.ApplicableOperations;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;

/**
 * The object a UI unit with an {@link AccessControl} checks its access on, published to the
 * content of the unit through the {@link ViewContext}.
 *
 * <p>
 * A command inside the unit that names an operation but no object of its own is checked on this
 * object, and its operation must apply to the {@link #getType() type} of the object. The value is
 * independent of the operation of the unit: it describes <em>what</em> is checked, not
 * <em>which</em> operation.
 * </p>
 *
 * <p>
 * The {@link #getValue() value} is computed anew from the current values of the {@link #getInputs()
 * input channels} of the unit on each call; a user of the value follows it by
 * {@link #observe(Runnable) observing} these channels.
 * </p>
 *
 * @see #of(ViewContext)
 * @see AccessChecks#contentContext(UnitAccess, ViewContext)
 */
public final class SecuredObject {

	private final TLClass _type;

	private final QueryExecutor _object;

	private final List<ViewChannel> _inputs;

	/**
	 * Creates a {@link SecuredObject}.
	 *
	 * @param type
	 *        See {@link #getType()}, {@code null} for the security root.
	 * @param object
	 *        The function computing the checked value from the current values of the given inputs,
	 *        {@code null} for the security root.
	 * @param inputs
	 *        See {@link #getInputs()}.
	 */
	SecuredObject(TLClass type, QueryExecutor object, List<ViewChannel> inputs) {
		_type = type;
		_object = object;
		_inputs = Collections.unmodifiableList(new ArrayList<>(inputs));
	}

	/**
	 * The {@link SecuredObject} of the security root.
	 */
	public static SecuredObject securityRoot() {
		return new SecuredObject(null, null, Collections.emptyList());
	}

	/**
	 * The {@link SecuredObject} of the innermost UI unit with an {@link AccessControl} enclosing the
	 * content built in the given context.
	 *
	 * @return {@code null} when no such unit encloses the content.
	 */
	public static SecuredObject of(ViewContext context) {
		return context.getScope(SecuredObject.class);
	}

	/**
	 * Whether the checked object is the security root: the unit names no object.
	 */
	public boolean isSecurityRoot() {
		return _object == null;
	}

	/**
	 * The static type of the checked object(s), {@code null} for the {@link #isSecurityRoot()
	 * security root}.
	 */
	public TLClass getType() {
		return _type;
	}

	/**
	 * The channels the {@link #getValue() value} is computed from, in declaration order; empty for
	 * the {@link #isSecurityRoot() security root}.
	 */
	public List<ViewChannel> getInputs() {
		return _inputs;
	}

	/**
	 * The current checked value: the security root, or what the object expression of the unit
	 * yields from the current values of its {@link #getInputs() inputs} - a single object, a
	 * collection, or {@code null}.
	 *
	 * <p>
	 * The expression is evaluated without the access rights of the current user, so that an object
	 * the user may not see is checked (and refused) rather than silently dropped. The value is meant
	 * for access checks, not for display.
	 * </p>
	 */
	public Object getValue() {
		if (_object == null) {
			return securityRootObject();
		}
		return _object.execute(ChannelInputs.arguments(_inputs));
	}

	/**
	 * The operations applying to the checked object(s): those of the {@link #getType() type}, or
	 * those of the security root.
	 */
	public ApplicableOperations getOperations() {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		if (_type == null) {
			return rights.getObjectOperations(securityRootObject());
		}
		return rights.getOperations(_type);
	}

	/**
	 * Runs the given callback whenever one of the {@link #getInputs() inputs} takes a new value.
	 *
	 * @return The registration that stops the observation.
	 */
	public Registration observe(Runnable onChange) {
		List<Registration> registrations = new ArrayList<>(_inputs.size());
		for (ViewChannel input : _inputs) {
			registrations.add(input.addListener((sender, oldValue, newValue) -> onChange.run()));
		}
		return Registration.onDispose(() -> registrations.forEach(Registration::dispose));
	}

	/**
	 * The security root object.
	 */
	static TLObject securityRootObject() {
		return SecurityRootObjectProvider.INSTANCE.getSecurityRoot();
	}

}
