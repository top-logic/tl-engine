/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The access parent setting of a type: the element <code>&lt;access-parent&gt;</code> of a
 * {@link TLClassAccessRights} entry, holding exactly one {@link AccessParentDefinition}, e.g.
 * <code>&lt;container/&gt;</code>, <code>&lt;target reference="..."/&gt;</code>,
 * <code>&lt;referer reference="..."/&gt;</code>, <code>&lt;any-of&gt;</code> of several of them or
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

	/**
	 * Whether the given setting delegates only some operations, see
	 * {@link OperationRestriction#getOperations()}.
	 *
	 * <p>
	 * The type of a restricted setting decides the other operations by grants of its own. An
	 * {@link AnyOfAccessParent} is restricted when all its candidates are.
	 * </p>
	 *
	 * @param config
	 *        The setting, <code>null</code> for none.
	 */
	static boolean isRestricted(AccessParentConfig config) {
		return config != null && isRestricted(config.getDefinition());
	}

	/**
	 * Whether the given definition delegates only some operations, see
	 * {@link #isRestricted(AccessParentConfig)}.
	 */
	private static boolean isRestricted(PolymorphicConfiguration<?> definition) {
		if (definition instanceof OperationRestriction restriction) {
			return !restriction.getOperations().isEmpty();
		}
		if (definition instanceof AnyOfAccessParent.Config anyOf) {
			return !anyOf.getCandidates().isEmpty()
				&& anyOf.getCandidates().stream().allMatch(AccessParentConfig::isRestricted);
		}
		return false;
	}

	/**
	 * The names of the operations the given setting delegates, as written in the configuration.
	 *
	 * @param config
	 *        The setting, <code>null</code> for none.
	 * @return The names of the operations {@link OperationRestriction#getOperations() listed} and
	 *         Discover, which is delegated together with any of them, for an
	 *         {@link AnyOfAccessParent} those of all its candidates; <code>null</code> when the
	 *         setting delegates all operations; the empty set when it does not
	 *         {@link #delegates(AccessParentConfig) delegate} at all.
	 */
	static Set<String> delegatedOperationNames(AccessParentConfig config) {
		if (!delegates(config)) {
			return Collections.emptySet();
		}
		if (!isRestricted(config)) {
			return null;
		}
		Set<String> result = new HashSet<>();
		addDelegatedOperationNames(result, config.getDefinition());
		return result;
	}

	/**
	 * Adds the names of the operations the given {@link #isRestricted(PolymorphicConfiguration)
	 * restricted} definition delegates to the given set.
	 */
	private static void addDelegatedOperationNames(Set<String> result, PolymorphicConfiguration<?> definition) {
		if (definition instanceof AnyOfAccessParent.Config anyOf) {
			for (PolymorphicConfiguration<?> candidate : anyOf.getCandidates()) {
				addDelegatedOperationNames(result, candidate);
			}
		} else if (definition instanceof OperationRestriction restriction) {
			for (CommandGroupReference operation : restriction.getOperations()) {
				result.add(operation.id());
			}
			result.add(SimpleBoundCommandGroup.DISCOVER_NAME);
		}
	}

}
