/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityObjectProviderFormat;

/**
 * {@link SecurityObjectProvider} delegating to different {@link SecurityObjectProvider} depending
 * on the {@link BoundCommandGroup}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DispatchingSecurityObjectProvider implements SecurityObjectProvider {

	/**
	 * Configuration of a {@link DispatchingSecurityObjectProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<DispatchingSecurityObjectProvider> {

		/** Name of property {@link #getDefault()}. */
		String DEFAULT_NAME = "default";

		/** Name of property {@link #getProviders()}. */
		String PROVIDERS_NAME = "providers";

		/**
		 * The default {@link SecurityObjectProvider} that is used for {@link BoundCommandGroup}
		 * that are not configured.
		 */
		@Mandatory
		@Format(SecurityObjectProviderFormat.class)
		@Name(DEFAULT_NAME)
		PolymorphicConfiguration<SecurityObjectProvider> getDefault();
		
		/**
		 * Setter for {@link #getDefault()}.
		 */
		void setDefault(PolymorphicConfiguration<SecurityObjectProvider> config);

		/**
		 * The mapping of the command group name to the {@link SecurityObjectProvider} handling it.
		 */
		@Key(ProviderConfig.COMMAND_GROUP)
		@Name(PROVIDERS_NAME)
		Map<String, ProviderConfig> getProviders();

	}

	/**
	 * {@link ConfigurationItem} holding a {@link SecurityObjectProvider} for a
	 * {@link BoundCommandGroup}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ProviderConfig extends ConfigurationItem {

		/** Name of property {@link #getCommandGroup()}. */
		String COMMAND_GROUP = "command-group";

		/** Name of property {@link #getImpl()}. */
		String IMPL = "impl";

		/**
		 * The {@link BoundCommandGroup#getID() id} of the command group for which
		 * {@link #getImpl()} should be used.
		 */
		@Name(COMMAND_GROUP)
		String getCommandGroup();

		/** Setter for {@link #getCommandGroup()}. */
		void setCommandGroup(String cmdGroup);

		/**
		 * The {@link SecurityObjectProvider} that is used for the command group with given
		 * {@link #getCommandGroup() id}.
		 */
		@Mandatory
		@Format(SecurityObjectProviderFormat.class)
		@Name(IMPL)
		PolymorphicConfiguration<SecurityObjectProvider> getImpl();

		/** Setter for {@link #getImpl()}. */
		void setImpl(PolymorphicConfiguration<SecurityObjectProvider> config);

	}

	private final SecurityObjectProvider _default;

	private final Map<String, SecurityObjectProvider> _providers;

	/**
	 * Creates a new {@link DispatchingSecurityObjectProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DispatchingSecurityObjectProvider}.
	 */
	public DispatchingSecurityObjectProvider(InstantiationContext context, Config config) {
		_default = context.getInstance(config.getDefault());
		Map<String, SecurityObjectProvider> providers = new HashMap<>();
		for (ProviderConfig providerConfig : config.getProviders().values()) {
			SecurityObjectProvider provider = context.getInstance(providerConfig.getImpl());
			if (provider == null) {
				StringBuilder errorNullProvider = new StringBuilder();
				errorNullProvider.append("No security provider for command group '");
				errorNullProvider.append(providerConfig.getCommandGroup());
				errorNullProvider.append("' given.");
				context.error(errorNullProvider.toString());
				continue;
			}
			providers.put(providerConfig.getCommandGroup(), provider);
		}
		_providers = providers;
	}

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		SecurityObjectProvider dispatcher = _providers.get(aCommandGroup.getID());
		if (dispatcher == null) {
			dispatcher = _default;
		}
		return dispatcher.getSecurityObject(aChecker, model, aCommandGroup);
	}

}

