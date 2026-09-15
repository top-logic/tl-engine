/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.annotate.security;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.annotate.AccessRightsConfig;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * Rule adjusting the roles that are allowed to perform an operation on a model element.
 *
 * <p>
 * The rules of a model element form a sequence that is applied in order: An {@link AccessGrant}
 * adds roles to the operation it names, an {@link AccessRevoke} takes roles away again. Since the
 * rules of all configuration layers are appended to that sequence, an application extends the
 * rights declared by the framework instead of replacing them.
 * </p>
 *
 * @see AccessRightsConfig#getGrants()
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
@DisplayOrder({
	AccessRule.OPERATION,
	AccessRule.ROLES,
	AccessRule.INHERIT,
})
public interface AccessRule extends ConfigurationItem {

	/** Config property name for {@link #getOperation()}. */
	String OPERATION = "operation";

	/** Config property name for {@link #getRoles()}. */
	String ROLES = "roles";

	/** Config property name for {@link #isInherit()}. */
	String INHERIT = "inherit";

	/**
	 * The operation (command group) this rule applies to.
	 */
	@Mandatory
	@Name(OPERATION)
	CommandGroupReference getOperation();

	/**
	 * The roles this rule adds to or removes from the {@link #getOperation() operation}.
	 */
	@Format(CommaSeparatedRoles.class)
	@Name(ROLES)
	List<RoleConfig> getRoles();

	/**
	 * Whether this rule also applies to the specializations (subclasses) of the annotated type.
	 */
	@Name(INHERIT)
	boolean isInherit();

}
