/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.security;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.security.ApplicableOperations;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * Declaration of an operation that applies to the objects of a type.
 *
 * <p>
 * An operation is a command group with a global name. Only an operation that applies to a type may
 * be granted on it. The {@link ApplicableOperations#BUILT_IN built-in operations} apply to every
 * type without a declaration; any other operation applies to the type declaring it and to all its
 * specializations.
 * </p>
 *
 * @see ApplicableOperations
 */
@TagName(OperationDeclaration.TAG_NAME)
@DisplayOrder({
	OperationDeclaration.NAME,
	OperationDeclaration.IMPLIED_BY,
})
public interface OperationDeclaration extends ConfigurationItem {

	/** Tag name of an {@link OperationDeclaration} in a list of declarations. */
	String TAG_NAME = "operation";

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getImpliedBy()}. */
	String IMPLIED_BY = "implied-by";

	/**
	 * The declared operation (command group).
	 *
	 * <p>
	 * The command group must be known to the command group registry and needs a label
	 * (<code>commandGroup.&lt;Name&gt;</code> and <code>commandGroup.&lt;Name&gt;.tooltip</code>)
	 * in a resource bundle of the application.
	 * </p>
	 */
	@Mandatory
	@Name(NAME)
	CommandGroupReference getName();

	/**
	 * Setter for {@link #getName()}.
	 */
	void setName(CommandGroupReference value);

	/**
	 * An operation implying the declared one.
	 *
	 * <p>
	 * Whoever holds the given operation on an object also holds the declared operation on it. The
	 * implying operation must apply to the type, too. Implications are transitive and must not
	 * form a cycle.
	 * </p>
	 */
	@Name(IMPLIED_BY)
	CommandGroupReference getImpliedBy();

	/**
	 * Setter for {@link #getImpliedBy()}.
	 */
	void setImpliedBy(CommandGroupReference value);

}
