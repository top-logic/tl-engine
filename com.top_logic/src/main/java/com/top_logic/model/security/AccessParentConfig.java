/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;

/**
 * The access parent setting of a type: the element <code>&lt;access-parent&gt;</code> of a
 * {@link TLClassAccessRights} entry, holding exactly one {@link AccessParentDefinition}, e.g.
 * <code>&lt;container/&gt;</code>, <code>&lt;target reference="..."/&gt;</code> or
 * <code>&lt;self/&gt;</code>.
 */
public interface AccessParentConfig extends ConfigPart {

	/** Configuration name for {@link #getDefinition()}. */
	String DEFINITION = "definition";

	/** Configuration name for {@link #getRights()}. */
	String RIGHTS = "rights";

	/**
	 * How the access parent of an object of the type is determined.
	 */
	@Name(DEFINITION)
	@DefaultContainer
	@Mandatory
	PolymorphicConfiguration<? extends AccessParentDefinition> getDefinition();

	/**
	 * Setter for {@link #getDefinition()}.
	 */
	void setDefinition(PolymorphicConfiguration<? extends AccessParentDefinition> value);

	/**
	 * The entry this setting belongs to, giving the type the definition is configured for.
	 */
	@Name(RIGHTS)
	@Hidden
	@Container
	TLClassAccessRights getRights();

	/**
	 * Whether the given setting delegates the access decision: anything but
	 * {@link SelfAccessParent}.
	 *
	 * @param config
	 *        The setting, <code>null</code> for none.
	 */
	static boolean delegates(AccessParentConfig config) {
		return config != null && config.getDefinition() != null
			&& !(config.getDefinition() instanceof SelfAccessParent.Config);
	}

}
