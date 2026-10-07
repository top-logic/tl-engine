/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.annotate.security;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLAccessRights;

/**
 * Rule withdrawing the permission to perform an operation on a model element.
 *
 * <p>
 * An {@link AccessRevoke} removes its roles from the roles that the preceding rules allowed to
 * perform the operation it names. Without any role, it removes every role for that operation, no
 * matter which rule contributed it: On an attribute, the operation is no longer restricted by the
 * attribute, so that the decision taken for the object applies alone. On a type, the operation is
 * denied for every role, since the rules of the type's module and of its generalizations have
 * already been applied when the type's own rules run.
 * </p>
 *
 * <p>
 * A rule following an {@link AccessRevoke} may allow the operation again.
 * </p>
 *
 * @see AccessGrant
 * @see TLAccessRights
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(AccessRevoke.TAG_NAME)
public interface AccessRevoke extends AccessRule {

	/** Tag name to use for an {@link AccessRevoke} in a rule sequence. */
	String TAG_NAME = "revoke";

}
