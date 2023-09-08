/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.client.registry;

import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;

/**
 * {@link ServiceMethodRegistry} for tests.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestingServiceMethodRegistry extends ServiceMethodRegistry {

	/**
	 * Creates a {@link TestingServiceMethodRegistry} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TestingServiceMethodRegistry(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected HttpContext createContext() {
		HttpContext context = super.createContext();
		context.setAttribute("http.socket-factory-registry", RegistryBuilder.<ConnectionSocketFactory> create()
			.register(URIScheme.HTTP.id, HttpUnitConnectionSocketFactory.getSocketFactory())
			.register(URIScheme.HTTPS.id, HttpUnitConnectionSocketFactory.getSocketFactory())
            .build());

		return context;
	}

}
