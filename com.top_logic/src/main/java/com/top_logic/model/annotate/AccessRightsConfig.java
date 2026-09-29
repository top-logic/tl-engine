/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.annotate;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.annotate.security.AccessGrant;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * Configuration of access rights.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface AccessRightsConfig extends ConfigurationItem {

	/** Configuration name for {@link #getGrants()}. */
	String GRANTS = "grants";

	/**
	 * The access grants for this model element, indexed by their operation (command group).
	 */
	@DefaultContainer
	@Key(AccessGrant.OPERATION)
	@Name(GRANTS)
	Map<CommandGroupReference, AccessGrant> getGrants();
}

