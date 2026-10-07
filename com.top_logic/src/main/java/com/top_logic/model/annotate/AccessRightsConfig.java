/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.annotate;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.model.annotate.security.AccessGrant;
import com.top_logic.model.annotate.security.AccessRevoke;
import com.top_logic.model.annotate.security.AccessRule;

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
	 * The access rules for this model element, applied in the order of their declaration.
	 * 
	 * <p>
	 * An {@link AccessGrant} allows its roles to perform the operation it names, an
	 * {@link AccessRevoke} takes that permission away again. Several rules may name the same
	 * operation; each of them adjusts the roles the preceding ones established. Since the rules of
	 * all configuration layers are appended to this sequence, an application adds to the rights
	 * declared by the framework instead of replacing them.
	 * </p>
	 */
	@DefaultContainer
	@Name(GRANTS)
	@Subtypes({
		@Subtype(tag = AccessGrant.TAG_NAME, type = AccessGrant.class),
		@Subtype(tag = AccessRevoke.TAG_NAME, type = AccessRevoke.class),
	})
	List<AccessRule> getGrants();
}

