/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.List;

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
	 * {@link SelfAccessParent}, also as the only candidate of an {@link AnyOfAccessParent}.
	 *
	 * @param config
	 *        The setting, <code>null</code> for none.
	 */
	static boolean delegates(AccessParentConfig config) {
		return config != null && config.getDefinition() != null && !isSelf(config.getDefinition());
	}

	/**
	 * Whether the given definition lets the type decide for itself: {@link SelfAccessParent}, or an
	 * {@link AnyOfAccessParent} naming nothing else.
	 */
	private static boolean isSelf(PolymorphicConfiguration<?> definition) {
		if (definition instanceof AnyOfAccessParent.Config anyOf) {
			List<PolymorphicConfiguration<? extends AccessParentDefinition>> candidates = anyOf.getCandidates();
			return !candidates.isEmpty() && candidates.stream().allMatch(AccessParentConfig::isSelf);
		}
		return definition instanceof SelfAccessParent.Config;
	}

	/**
	 * Whether the given setting delegates the access decision and names the object itself as a
	 * candidate of an {@link AnyOfAccessParent}, also of a nested one.
	 *
	 * <p>
	 * The grants of the type then decide the delegated operations as well, together with the
	 * access parents, see {@link SelfRelation}.
	 * </p>
	 *
	 * @param config
	 *        The setting, <code>null</code> for none.
	 */
	static boolean includesSelf(AccessParentConfig config) {
		return delegates(config) && includesSelf(config.getDefinition());
	}

	private static boolean includesSelf(PolymorphicConfiguration<?> definition) {
		return definition instanceof AnyOfAccessParent.Config anyOf
			&& anyOf.getCandidates().stream().anyMatch(
				candidate -> candidate instanceof SelfAccessParent.Config || includesSelf(candidate));
	}

	/**
	 * Whether the given setting delegates only some operations.
	 *
	 * <p>
	 * The type of a restricted setting decides the other operations by grants of its own. A
	 * building block is restricted when it lists its {@link OperationRestriction#getOperations()
	 * operations}; a {@link RefererAccessParent} always is, since it never decides Create. An
	 * {@link AnyOfAccessParent} is restricted when all its candidates are, when it does not delegate
	 * Create, because no candidate leading to the container does, see
	 * {@link #delegatesOperation(AccessParentConfig, String)}, or when it names the object itself,
	 * see {@link #includesSelf(AccessParentConfig)}. An {@link AnyOfAccessParent} with a single
	 * candidate is that candidate.
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
	 *
	 * @param definition
	 *        The definition of a setting or one of the candidates of an {@link AnyOfAccessParent},
	 *        <code>null</code> for none.
	 */
	static boolean isRestricted(PolymorphicConfiguration<?> definition) {
		if (definition instanceof RefererAccessParent.Config) {
			// Never decides Create, see RefererRelation.
			return true;
		}
		if (definition instanceof OperationRestriction restriction) {
			return !restriction.getOperations().isEmpty();
		}
		if (definition instanceof AnyOfAccessParent.Config anyOf) {
			List<PolymorphicConfiguration<? extends AccessParentDefinition>> candidates = anyOf.getCandidates();
			if (candidates.size() == 1) {
				return isRestricted(candidates.get(0));
			}
			return !candidates.isEmpty() && (allCandidatesRestricted(anyOf)
				|| !delegatesOperation(anyOf, SimpleBoundCommandGroup.CREATE_NAME) || includesSelf(anyOf));
		}
		return false;
	}

	/**
	 * Whether every building block of the given {@link AnyOfAccessParent} lists its operations, the
	 * candidates of a nested {@link AnyOfAccessParent} taken as its own candidates, as the resolved
	 * {@link AnyOfRelation} takes them.
	 */
	private static boolean allCandidatesRestricted(AnyOfAccessParent.Config anyOf) {
		return anyOf.getCandidates().stream()
			.allMatch(candidate -> candidate instanceof AnyOfAccessParent.Config nested
				? allCandidatesRestricted(nested)
				: isRestricted(candidate));
	}

	/**
	 * Whether the given setting delegates the named operation, as written in the configuration.
	 *
	 * <p>
	 * A building block delegates the operations it {@link OperationRestriction#getOperations()
	 * lists} and Discover, which is delegated together with any of them unless the building block
	 * leaves it to them ({@link OperationRestriction#getDiscover()}), or all operations when it
	 * lists none, except Create for a {@link RefererAccessParent}, which never decides it. An
	 * {@link AnyOfAccessParent} delegates an operation one of its candidates
	 * delegates, Create only when a candidate leading to the container delegates it.
	 * </p>
	 *
	 * @param config
	 *        The setting, <code>null</code> for none.
	 * @param operationName
	 *        The name of the operation.
	 * @return <code>false</code> when the setting does not {@link #delegates(AccessParentConfig)
	 *         delegate} at all.
	 */
	static boolean delegatesOperation(AccessParentConfig config, String operationName) {
		return delegates(config) && delegatesOperation(config.getDefinition(), operationName);
	}

	/**
	 * Whether the given definition delegates the named operation, see
	 * {@link #delegatesOperation(AccessParentConfig, String)}.
	 */
	private static boolean delegatesOperation(PolymorphicConfiguration<?> definition, String operationName) {
		if (definition instanceof AnyOfAccessParent.Config anyOf) {
			List<PolymorphicConfiguration<? extends AccessParentDefinition>> candidates = anyOf.getCandidates();
			if (candidates.size() == 1) {
				return delegatesOperation(candidates.get(0), operationName);
			}
			if (SimpleBoundCommandGroup.CREATE_NAME.equals(operationName)) {
				return candidates.stream().anyMatch(AccessParentConfig::createsInContainer);
			}
			return candidates.stream().anyMatch(candidate -> delegatesOperation(candidate, operationName));
		}
		if (definition instanceof SelfAccessParent.Config) {
			return false;
		}
		if (definition instanceof RefererAccessParent.Config
			&& SimpleBoundCommandGroup.CREATE_NAME.equals(operationName)) {
			return false;
		}
		if (definition instanceof OperationRestriction restriction) {
			List<CommandGroupReference> operations = restriction.getOperations();
			if (operations.isEmpty()) {
				return true;
			}
			if (SimpleBoundCommandGroup.DISCOVER_NAME.equals(operationName)
				&& restriction.getDiscover() != DiscoverDelegation.IMPLIED) {
				return true;
			}
			return operations.stream().anyMatch(operation -> operationName.equals(operation.id()));
		}
		return true;
	}

	/**
	 * Whether the given candidate of an {@link AnyOfAccessParent} leads to the container and
	 * delegates Create there, directly or through a nested {@link AnyOfAccessParent}.
	 */
	private static boolean createsInContainer(PolymorphicConfiguration<?> candidate) {
		if (candidate instanceof AnyOfAccessParent.Config nested) {
			return nested.getCandidates().stream().anyMatch(AccessParentConfig::createsInContainer);
		}
		return candidate instanceof ContainerAccessParent.Config
			&& delegatesOperation(candidate, SimpleBoundCommandGroup.CREATE_NAME);
	}

}
