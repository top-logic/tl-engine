/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLClass;

/**
 * The configured access parent of a type, see {@link AccessParentConfig}.
 * <p>
 * A definition is resolved once for the type it is configured for, when the access rights are
 * loaded, into the {@link AccessParentFunction} the access check follows.
 * </p>
 */
public interface AccessParentDefinition {

	/**
	 * Resolves this definition for the given type.
	 *
	 * @param context
	 *        The context to report an unusable setting to.
	 * @param type
	 *        The type the definition is configured for.
	 * @return The relation objects of the type delegate their access decision through,
	 *         <code>null</code> for a type deciding for itself ({@link SelfAccessParent}) and for an
	 *         unusable setting, which is reported to the context.
	 */
	AccessParentFunction resolve(InstantiationContext context, TLClass type);

}
