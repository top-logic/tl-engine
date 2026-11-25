/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
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
	 *         unusable setting, which is reported to the context. A restriction to some
	 *         {@link OperationRestriction#getOperations() operations} is not part of the result: it
	 *         is applied by the caller.
	 */
	AccessParentFunction resolve(InstantiationContext context, TLClass type);

	/**
	 * Resolves this definition for the given type, when the access rights are loaded.
	 *
	 * <p>
	 * A definition combining other definitions, like {@link AnyOfAccessParent}, overrides this
	 * method and resolves its candidates through the given resolver.
	 * </p>
	 *
	 * @param context
	 *        The context to report an unusable setting to.
	 * @param type
	 *        The type the definition is configured for.
	 * @param candidates
	 *        Resolves a definition nested in this one, including its restriction to some
	 *        operations.
	 * @return See {@link #resolve(InstantiationContext, TLClass)}, which is the default.
	 */
	default AccessParentFunction resolveWith(InstantiationContext context, TLClass type, CandidateResolver candidates) {
		return resolve(context, type);
	}

	/**
	 * Resolution of a definition nested in another one, see
	 * {@link AccessParentDefinition#resolveWith(InstantiationContext, TLClass, CandidateResolver)}.
	 */
	@FunctionalInterface
	interface CandidateResolver {

		/**
		 * Resolves the given definition for the given type.
		 *
		 * @param context
		 *        The context to report an unusable setting to.
		 * @param type
		 *        The type the definition is configured for.
		 * @param candidate
		 *        The configuration of the definition to resolve.
		 * @return The relation of the definition, a {@link RestrictedRelation} when it is
		 *         restricted to some operations; <code>null</code> for an unusable setting, which is
		 *         reported to the context, {@link SelfAccessParent} included, which only
		 *         {@link AnyOfAccessParent} resolves as a candidate.
		 *         A combining definition receiving <code>null</code> is unusable itself and returns
		 *         <code>null</code> as well, without reporting again.
		 */
		AccessParentFunction resolveCandidate(InstantiationContext context, TLClass type,
				PolymorphicConfiguration<? extends AccessParentDefinition> candidate);

	}

}
