/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.util;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * {@link Filter} that {@link #accept(BoundObject) accepts} objects, for which the current user
 * {@link AccessManager#hasRole(BoundObject, Collection) has one of a given number of roles}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HasRoleFilter<C extends HasRoleFilter.Config<?>> extends AbstractConfiguredInstance<C>
		implements Filter<BoundObject> {

	/**
	 * Configuration options for {@link HasRoleFilter}.
	 */
	@TagName("has-some-role")
	public interface Config<I extends HasRoleFilter<?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getRoles()
		 */
		String ROLES = "roles";

		/**
		 * List of role names.
		 * 
		 * <p>
		 * Only those objects are accepted on which the current user has one on the configured
		 * roles.
		 * </p>
		 */
		@Name(ROLES)
		@Format(CommaSeparatedStrings.class)
		List<String> getRoles();

	}

	private final Collection<? extends BoundedRole> _roles;

	/**
	 * Creates a {@link HasRoleFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public HasRoleFilter(InstantiationContext context, C config) {
		super(context, config);
		_roles = BoundedRole.getRolesByName(context, config.getRoles());
	}

	@Override
	public boolean accept(BoundObject anObject) {
		return AccessManager.getInstance().hasRole(anObject, _roles);
	}

}