/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AccessParentDefinition} with several candidates, e.g.
 * <code>&lt;any-of&gt;&lt;container/&gt;&lt;referer reference="..."/&gt;&lt;/any-of&gt;</code>.
 *
 * <p>
 * The access parents of an object are those of all candidates; an operation is allowed as soon as
 * it is allowed on one of them. A candidate restricted to some
 * {@link OperationRestriction#getOperations() operations} abstains for the others. A nested
 * <code>&lt;any-of&gt;</code> adds its candidates, <code>&lt;self/&gt;</code> is no candidate.
 * </p>
 *
 * @see AnyOfRelation
 */
@Label("Any of")
public class AnyOfAccessParent extends AbstractConfiguredInstance<AnyOfAccessParent.Config>
		implements AccessParentDefinition {

	/**
	 * Configuration of {@link AnyOfAccessParent}.
	 *
	 * <p>
	 * As a {@link ConfigPart}, it leads the option functions of its candidates to the setting it
	 * belongs to, see {@link AccessParentReferenceOptions}.
	 * </p>
	 */
	@TagName("any-of")
	public interface Config extends PolymorphicConfiguration<AnyOfAccessParent>, ConfigPart {

		/** Configuration name for {@link #getCandidates()}. */
		String CANDIDATES = "candidates";

		/**
		 * The definitions each leading to access parents of an object of the type.
		 */
		@Name(CANDIDATES)
		@DefaultContainer
		List<PolymorphicConfiguration<? extends AccessParentDefinition>> getCandidates();

	}

	/**
	 * Creates a {@link AnyOfAccessParent} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AnyOfAccessParent(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Reports an error: the candidates are resolved only when the access rights are loaded, see
	 * {@link #resolveWith(InstantiationContext, TLClass, CandidateResolver)}.
	 */
	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		context.error("The candidates of the access parent of " + TLModelUtil.qualifiedName(type)
			+ " are resolved only when the access rights are loaded.");
		return null;
	}

	/**
	 * @return An {@link AnyOfRelation} of the resolved candidates, <code>null</code> when one of
	 *         them is unusable.
	 */
	@Override
	public AccessParentFunction resolveWith(InstantiationContext context, TLClass type, CandidateResolver candidates) {
		String typeName = TLModelUtil.qualifiedName(type);
		List<PolymorphicConfiguration<? extends AccessParentDefinition>> configs = getConfig().getCandidates();
		if (configs.isEmpty()) {
			context.error("The access parent of " + typeName + " has no candidate in <any-of>.");
			return null;
		}
		List<AccessParentFunction> result = new ArrayList<>();
		boolean valid = true;
		for (PolymorphicConfiguration<? extends AccessParentDefinition> candidate : configs) {
			if (candidate instanceof SelfAccessParent.Config) {
				context.error("The access parent of " + typeName
					+ " names <self/> in <any-of>: a type deciding for itself has no candidates.");
				valid = false;
				continue;
			}
			AccessParentFunction function = candidates.resolveCandidate(context, type, candidate);
			if (function == null) {
				valid = false;
				continue;
			}
			if (function instanceof AnyOfRelation nested) {
				result.addAll(nested.candidates());
			} else {
				result.add(function);
			}
		}
		return valid ? new AnyOfRelation(result) : null;
	}

}
