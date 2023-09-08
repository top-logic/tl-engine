/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.InAppSecurityObjectProviderConfig;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * {@link SecurityObjectProvider} delegating to another {@link SecurityObjectProvider} depending on
 * the given command group.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class SecurityObjectByGroup extends AbstractConfiguredInstance<SecurityObjectByGroup.Config>
		implements SecurityObjectProvider {

	/**
	 * Typed configuration interface definition for {@link SecurityObjectByGroup}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<SecurityObjectByGroup>, InAppSecurityObjectProviderConfig {

		/** Configuration name for {@link #getProviders()}. */
		String PROVIDERS = "providers";

		@Override
		@Mandatory
		PolymorphicConfiguration<? extends SecurityObjectProvider> getSecurityObject();

		/**
		 * {@link SecurityObjectProvider} computing the security object from the given model for a
		 * special {@link BoundCommandGroup}.
		 * 
		 * <p>
		 * If this {@link SecurityObjectProvider} is accessed with a {@link BoundCommandGroup} that
		 * is not contained, the default provider is used.
		 * </p>
		 * 
		 * @see #getSecurityObject()
		 */
		@Key(ProviderByGroup.GROUP)
		@Name(PROVIDERS)
		List<ProviderByGroup> getProviders();
	}

	/**
	 * Configuration of a {@link SecurityObjectProvider} to use for a special
	 * {@link BoundCommandGroup}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@DisplayOrder({
		ProviderByGroup.GROUP,
		ProviderByGroup.SECURITY_OBJECT
	})
	public interface ProviderByGroup extends InAppSecurityObjectProviderConfig {

		/** Name of the property {@link #getGroup()}. */
		String GROUP = "group";

		/**
		 * The {@link BoundCommandGroup} for which the configured {@link #getSecurityObject()} must
		 * be used.
		 */
		@Mandatory
		@Name(GROUP)
		@Options(fun = CommandHandler.Config.AllCommandGroups.class)
		BoundCommandGroup getGroup();

		@Override
		@Mandatory
		PolymorphicConfiguration<? extends SecurityObjectProvider> getSecurityObject();

	}

	private final SecurityObjectProvider _defaultProvider;

	private final Map<BoundCommandGroup, SecurityObjectProvider> _providerByGroup;

	/**
	 * Create a {@link SecurityObjectByGroup}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SecurityObjectByGroup(InstantiationContext context, Config config) {
		super(context, config);
		_defaultProvider = context.getInstance(config.getSecurityObject());
		_providerByGroup = config.getProviders().stream().collect(Collectors.toMap(ProviderByGroup::getGroup,
			functionByGroup -> context.getInstance(functionByGroup.getSecurityObject())));

	}

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		SecurityObjectProvider provider = _providerByGroup.get(aCommandGroup);
		if (provider == null) {
			provider = _defaultProvider;
		}
		return provider.getSecurityObject(aChecker, model, aCommandGroup);
	}

}

