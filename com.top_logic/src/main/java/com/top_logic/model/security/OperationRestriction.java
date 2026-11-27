/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.security.CommaSeparatedCommandGroups;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler.Config.ToCommandGroupReference;

/**
 * Configuration of an {@link AccessParentDefinition} that delegates only some operations to the
 * access parent, e.g. <code>&lt;target reference="..." operations="Read"/&gt;</code>.
 *
 * <p>
 * The restriction is resolved into a {@link RestrictedRelation} when the access rights are loaded.
 * </p>
 */
public interface OperationRestriction extends ConfigPart {

	/** Configuration name for {@link #getOperations()}. */
	String OPERATIONS = "operations";

	/** Configuration name for {@link #getDiscover()}. */
	String DISCOVER = "discover";

	/** Configuration name for {@link #getOwner()}. */
	String OWNER = "owner";

	/**
	 * The operations on an object of the type that the access parent decides.
	 *
	 * <p>
	 * Without operations, the access parent decides all operations. Otherwise, it decides exactly
	 * the operations listed, Create and Delete only when listed, and Discover with any of them,
	 * unless {@link #getDiscover()} says otherwise. All other operations are decided by the grants
	 * of the type itself, which must not grant an operation the access parent decides.
	 * </p>
	 */
	@Name(OPERATIONS)
	@Format(CommaSeparatedCommandGroups.class)
	@Options(fun = AccessParentReferenceOptions.Operations.class, args = @Ref(OWNER),
		mapping = ToCommandGroupReference.class)
	List<CommandGroupReference> getOperations();

	/**
	 * Setter for {@link #getOperations()}.
	 */
	void setOperations(List<CommandGroupReference> value);

	/**
	 * How the access parent decides Discover on an object of the type, when it is restricted to
	 * some {@link #getOperations() operations}.
	 *
	 * <p>
	 * By default, Discover is {@link DiscoverDelegation#DELEGATED delegated} together with the
	 * listed operations: whoever may discover the access parent may discover the object. With
	 * {@link DiscoverDelegation#IMPLIED}, the object is discoverable through the access parent only
	 * for a user the access parent allows one of the listed operations; Discover on the object is
	 * otherwise decided by the grants of the type itself. This keeps the objects below an access
	 * parent hidden from a user who may only discover the access parent.
	 * </p>
	 *
	 * <p>
	 * {@link DiscoverDelegation#IMPLIED} requires a list of operations, which must not contain
	 * Discover.
	 * </p>
	 */
	@Name(DISCOVER)
	DiscoverDelegation getDiscover();

	/**
	 * Setter for {@link #getDiscover()}.
	 */
	void setDiscover(DiscoverDelegation value);

	/**
	 * The configuration this definition belongs to: the {@link AccessParentConfig setting} of a
	 * type, or a definition combining several ones like {@link AnyOfAccessParent}.
	 *
	 * @implNote The options of the definition are those of the type of the setting, found by
	 *           following the containers of the owner, see {@link AccessParentReferenceOptions}.
	 *           The configuration of a custom combining definition must therefore extend
	 *           {@link ConfigPart}.
	 */
	@Name(OWNER)
	@Hidden
	@Container
	ConfigurationItem getOwner();

}
