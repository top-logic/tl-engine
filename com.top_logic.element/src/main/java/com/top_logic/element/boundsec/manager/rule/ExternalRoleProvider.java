/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Set;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.security.SecurityStorage.SecurityStorageExecutor;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * The {@link ExternalRoleProvider} extends the {@link RoleProvider} in order to
 * provide the algorithms to
 * 
 * <ul>
 * <li>compute all roles (in case of a rebuild of the {@link SecurityStorage},
 * see {@link #computeRoles(SecurityStorageExecutor)}),</li>
 * <li>determining objects affected by changes (see {@link #getAffectingTypes()}
 * and {@link #getAffectedObjects(Object)}).</li>
 * </ul>
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface ExternalRoleProvider extends RoleProvider, ConfiguredInstance<ExternalRoleProvider.Config> {

	/**
	 * {@link TypedConfiguration} of the {@link ExternalRoleProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<ExternalRoleProvider> {

		/** Property name of {@link #getRuleName()}. */
		String RULE_NAME = "rule-name";

		@Name(RULE_NAME)
		String getRuleName();

	}

	/**
	 * Compute all roles implied by this {@link ExternalRoleProvider}
	 * and add them to the {@link SecurityStorage}
	 * via the given {@link SecurityStorageExecutor}.
	 * 
	 * This method is used only in the context of precalculated roles
	 * as done by the {@link SecurityStorage}.
	 * 
	 * TODO TSA: think of how to decouple this aspect from the other aspects of the {@link ExternalRoleProvider}
	 * 
	 * @param executor the {@link SecurityStorageExecutor} used to store the calculated roles, never <code>null</code>.
	 */
	public void computeRoles(SecurityStorageExecutor executor);
	
	/**
	 * the type names ({@link KnowledgeItem#tTable()}) of objects affecting this {@link RoleProvider}, never <code>null</code>
	 */
	public Set<String> getAffectingTypes();
	/**
	 * @param aChanged  an object that changed (was created/deleted) in the current transaction.
	 * @return the set of objects affected by the change to this object due to this {@link RoleProvider}
	 *         never <code>null</code>
	 */
	public Set<BoundObject> getAffectedObjects(Object aChanged);


}

