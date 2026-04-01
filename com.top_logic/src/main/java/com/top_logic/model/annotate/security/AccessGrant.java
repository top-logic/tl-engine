/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.annotate.security;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.annotate.TLAccessRights;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * Configuration granting access to a specific operation to a set of roles.
 *
 * <p>
 * An {@link AccessGrant} associates one operation (identified by a {@link CommandGroupReference})
 * with the roles that are allowed to perform it on the annotated model element.
 * </p>
 *
 * @see TLAccessRights
 *
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	AccessGrant.OPERATION,
	AccessGrant.ROLES,
	AccessGrant.INHERIT,
})
public interface AccessGrant extends ConfigurationItem {

	/** Config property name for {@link #getOperation()}. */
	String OPERATION = "operation";

	/** Config property name for {@link #getRoles()}. */
	String ROLES = "roles";

	/** Config property name for {@link #isInherit()}. */
	String INHERIT = "inherit";

	/**
	 * The operation (command group) this grant applies to.
	 */
	@Mandatory
	@Name(OPERATION)
	CommandGroupReference getOperation();

	/**
	 * The roles that are granted access for the {@link #getOperation() operation}.
	 */
	@Format(CommaSeparatedRoles.class)
	@Name(ROLES)
	List<RoleConfig> getRoles();

	/**
	 * Whether this grant is propagated to specializations (subclasses) of the annotated type.
	 */
	@Name(INHERIT)
	boolean isInherit();

}

