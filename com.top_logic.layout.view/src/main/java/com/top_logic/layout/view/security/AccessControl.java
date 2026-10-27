/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * The access check of a UI unit (navigation item, sidebar group, tab, accordion section, tile):
 * which operation the user needs on which object to see the content of the unit.
 *
 * <p>
 * Authored as {@code <access-control operation="..."/>} in the unit. The grants deciding the
 * operation are those of the model access rights (the {@code <security-config>} of the
 * {@link com.top_logic.model.security.SecurityConfigurationService}); the unit only states what is
 * checked:
 * </p>
 *
 * <ul>
 * <li>Without an {@link #getObject() object}, the operation is checked on the security root (the
 * singleton {@code SecurityStructure#ROOT}), where it must be declared.</li>
 * <li>With an {@link #getObject() object}, the operation is checked on the object(s) the
 * expression yields from the {@link #getInputs() inputs} of the unit: on a single object, or on
 * every element of a collection. The operation must apply to the declared {@link #getType()
 * type}.</li>
 * </ul>
 *
 * <pre>
 * &lt;nav-item id="planning"&gt;
 *   &lt;access-control operation="ShowPlanning" inputs="project" object="$project"
 *     type="tl.demo.projectManagement:ProjectScope"/&gt;
 *   ...
 * &lt;/nav-item&gt;
 * </pre>
 *
 * @see WithAccessControl
 * @see UnitAccess
 */
public interface AccessControl extends Inputs {

	/** Configuration name for {@link #getOperation()}. */
	String OPERATION = "operation";

	/** Configuration name for {@link #getObject()}. */
	String OBJECT = "object";

	/** Configuration name for {@link #getType()}. */
	String TYPE = "type";

	/** Configuration name for {@link #getDenied()}. */
	String DENIED = "denied";

	/**
	 * The operation the user needs to see the content of the unit.
	 *
	 * <p>
	 * The name of a command group: a built-in operation such as {@code Read}, or an operation
	 * declared for the {@link #getType() type} (or, without an {@link #getObject() object}, for
	 * the security root) in the {@code <security-config>}.
	 * </p>
	 */
	@Name(OPERATION)
	@Mandatory
	CommandGroupReference getOperation();

	/**
	 * TL-Script expression computing the object(s) the {@link #getOperation() operation} is
	 * checked on.
	 *
	 * <p>
	 * The expression sees the {@link #getInputs() inputs} as variables named after their channels,
	 * or by the names given with {@code as}. It yields a single object, a collection of objects
	 * (the operation must be allowed on every element), or {@code null} / an empty collection: then
	 * there is no object, and the unit is shown empty.
	 * </p>
	 *
	 * <p>
	 * Without an expression, the operation is checked on the security root.
	 * </p>
	 */
	@Name(OBJECT)
	@Nullable
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getObject();

	/**
	 * The qualified name of the type of the checked object(s).
	 *
	 * <p>
	 * Mandatory with an {@link #getObject() object}. When the view is loaded, the
	 * {@link #getOperation() operation} must apply to this type. An object that is no instance of
	 * the type is denied.
	 * </p>
	 */
	@Name(TYPE)
	@Nullable
	TLModelPartRef getType();

	/**
	 * How the unit is displayed while the {@link #getOperation() operation} is refused on a checked
	 * {@link #getObject() object}.
	 *
	 * <p>
	 * By default, the unit is not displayed then ({@link DeniedUnitDisplay#HIDE}).
	 * {@link DeniedUnitDisplay#PLACEHOLDER} displays it with a "no access" placeholder instead of
	 * its content. A unit without an object to check is displayed empty either way, and a unit
	 * whose operation the user can never hold is not built at all.
	 * </p>
	 */
	@Name(DENIED)
	DeniedUnitDisplay getDenied();

}
