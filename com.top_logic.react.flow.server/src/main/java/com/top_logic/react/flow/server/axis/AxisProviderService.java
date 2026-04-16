/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.axis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Registry of {@link AxisProvider}s indexed by their {@link AxisProvider#getId() id}.
 *
 * <p>
 * Registered through TopLogic module config; applications add their own providers as
 * {@code <provider class="..."/>} entries under the service config.
 * </p>
 */
public class AxisProviderService extends ConfiguredManagedClass<AxisProviderService.Config> {

	/**
	 * Configuration interface for the {@link AxisProviderService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<AxisProviderService> {

		/** Name of the providers property. */
		String PROVIDERS = "providers";

		/** Entry tag name for individual provider entries. */
		String PROVIDER = "provider";

		/**
		 * The registered {@link AxisProvider}s.
		 */
		@Name(PROVIDERS)
		@EntryTag(PROVIDER)
		List<PolymorphicConfiguration<? extends AxisProvider>> getProviders();
	}

	private final Map<String, AxisProvider> _providers;

	/**
	 * Creates a new {@link AxisProviderService} from the given configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AxisProviderService(InstantiationContext context, Config config) {
		super(context, config);

		Map<String, AxisProvider> providers = new HashMap<>();
		for (PolymorphicConfiguration<? extends AxisProvider> entry : config.getProviders()) {
			AxisProvider provider = context.getInstance(entry);
			if (provider != null) {
				providers.put(provider.getId(), provider);
			}
		}
		_providers = providers;
	}

	/**
	 * Look up a provider by id.
	 *
	 * @param id
	 *        The provider identifier as returned by {@link AxisProvider#getId()}.
	 * @return The matching {@link AxisProvider}, or {@code null} if unknown.
	 */
	public AxisProvider lookup(String id) {
		return _providers.get(id);
	}

	/**
	 * All registered providers as an unmodifiable list.
	 */
	public List<AxisProvider> getProviders() {
		return Collections.unmodifiableList(new ArrayList<>(_providers.values()));
	}

	/**
	 * Module registration for {@link AxisProviderService}.
	 */
	public static final class Module extends TypedRuntimeModule<AxisProviderService> {

		/**
		 * Singleton instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<AxisProviderService> getImplementation() {
			return AxisProviderService.class;
		}
	}
}
