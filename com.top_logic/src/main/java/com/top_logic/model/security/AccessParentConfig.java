/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.security.SecurityConfigurationService.TLClassAccessRights;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

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

	/**
	 * Whether the given setting delegates only some operations, see
	 * {@link OperationRestriction#getOperations()}.
	 *
	 * <p>
	 * The type of a restricted setting decides the other operations by grants of its own.
	 * </p>
	 *
	 * @param config
	 *        The setting, <code>null</code> for none.
	 */
	static boolean isRestricted(AccessParentConfig config) {
		return config != null && config.getDefinition() instanceof OperationRestriction restriction
			&& !restriction.getOperations().isEmpty();
	}

	/**
	 * The names of the operations the given setting delegates, as written in the configuration.
	 *
	 * @param config
	 *        The setting, <code>null</code> for none.
	 * @return The names of the operations {@link OperationRestriction#getOperations() listed} and
	 *         Discover, which is delegated together with any of them; <code>null</code> when the
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
		for (CommandGroupReference operation : ((OperationRestriction) config.getDefinition()).getOperations()) {
			result.add(operation.id());
		}
		result.add(SimpleBoundCommandGroup.DISCOVER_NAME);
		return result;
	}

	/**
	 * The localized list of the given operations, named as the user knows them.
	 *
	 * @param operationNames
	 *        The names (IDs) of the operations, at least one.
	 * @return The labels of the operations sorted by name, as a comma-separated list; an operation
	 *         without a label is named by its name.
	 */
	static ResKey operationLabels(Collection<String> operationNames) {
		ResKey labels = null;
		for (String name : new TreeSet<>(operationNames)) {
			ResKey label = ResKey.fallback(
				com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES.key(name), ResKey.text(name));
			labels = labels == null ? label : I18NConstants.OPERATION_LIST__LIST_OPERATION.fill(labels, label);
		}
		return labels;
	}

}
