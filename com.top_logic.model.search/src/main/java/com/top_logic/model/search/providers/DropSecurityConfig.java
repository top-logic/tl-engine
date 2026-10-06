/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Security options of a drop operation that is configured with TL-Script.
 *
 * <p>
 * A drop is only possible for a user who is allowed to perform an operation of the configured
 * {@link #getGroup() command group} on the {@link #getTarget() target object} of the drop.
 * </p>
 *
 * @see DropSecurity
 */
@Abstract
public interface DropSecurityConfig extends ConfigurationItem {

	/**
	 * Name of {@link #getGroup()}.
	 */
	String GROUP = "group";

	/**
	 * Name of {@link #getTarget()}.
	 */
	String TARGET = "target";

	/**
	 * The {@link BoundCommandGroup command group} whose permission is required for performing the
	 * drop.
	 *
	 * <p>
	 * The drop is only possible, if the current user has a role on the {@link #getTarget() target
	 * object} of the drop that grants this command group.
	 * </p>
	 */
	@Name(GROUP)
	@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
	CommandGroupReference getGroup();

	/**
	 * Function computing the object on which the permission for the drop is checked.
	 *
	 * <p>
	 * The function receives the same arguments as the function checking whether the drop can be
	 * performed. If the function is not set, the object referenced by the drop position is the
	 * target (the referenced row for a table drop, the referenced node for a drop onto a tree
	 * node, and the parent node into which the elements are dropped for an ordered tree drop).
	 * If the target is a tree node, the permission is checked on its business object.
	 * </p>
	 *
	 * <p>
	 * If the target is empty (e.g. for a drop onto an empty table), the permission is checked on
	 * the model of the component in which the drop happens.
	 * </p>
	 */
	@Name(TARGET)
	@Nullable
	Expr getTarget();

}
