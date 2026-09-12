/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.client.registry;

import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.DefaultHttpClientConnectionOperator;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionOperator;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;

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
	protected HttpClientBuilder createClientBuilder() {
		return HttpClients.custom().setConnectionManager(newConnectionManager());
	}

	/**
	 * A {@link HttpClientConnectionManager} that routes requests through httpunit instead of a real
	 * network connection.
	 */
	private static HttpClientConnectionManager newConnectionManager() {
		HttpClientConnectionOperator operator = new DefaultHttpClientConnectionOperator(
			HttpUnitConnectionSocketFactory.getSocketFactory(),
			DefaultSchemePortResolver.INSTANCE,
			SystemDefaultDnsResolver.INSTANCE,
			RegistryBuilder.<TlsSocketStrategy> create().build());
		return new PoolingHttpClientConnectionManager(operator, PoolConcurrencyPolicy.STRICT, PoolReusePolicy.LIFO,
			TimeValue.NEG_ONE_MILLISECOND, ManagedHttpClientConnectionFactory.INSTANCE);
	}

}
