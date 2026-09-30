/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.annotate.security;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLAccessRights;

/**
 * Rule allowing a set of roles to perform an operation on a model element.
 *
 * <p>
 * An {@link AccessGrant} adds its roles to the roles that are allowed to perform the operation it
 * names. Granting the same operation twice therefore results in the union of both role sets.
 * </p>
 *
 * @see AccessRevoke
 * @see TLAccessRights
 *
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(AccessGrant.TAG_NAME)
public interface AccessGrant extends AccessRule {

	/** Tag name to use for an {@link AccessGrant} in a rule sequence. */
	String TAG_NAME = "grant";

}
