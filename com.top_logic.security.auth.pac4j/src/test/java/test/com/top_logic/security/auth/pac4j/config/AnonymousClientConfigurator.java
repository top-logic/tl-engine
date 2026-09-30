/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.config;

import jakarta.servlet.ServletContext;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.direct.AnonymousClient;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;

/**
 * {@link ClientConfigurator} for an authentication that never visits an identity provider, standing
 * in for the mechanisms that cannot authenticate the user of an established session again.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AnonymousClientConfigurator
		extends AbstractConfiguredInstance<AnonymousClientConfigurator.Config<?>> implements ClientConfigurator {

	/**
	 * Configuration options for {@link AnonymousClientConfigurator}.
	 */
	public interface Config<I extends AnonymousClientConfigurator> extends ClientConfigurator.Config<I> {
		// Nothing beyond the common client options.
	}

	/**
	 * Creates a {@link AnonymousClientConfigurator} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AnonymousClientConfigurator(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public Client createClient(ServletContext context) {
		AnonymousClient result = new AnonymousClient();
		result.setName(getConfig().getName());
		return result;
	}

}
