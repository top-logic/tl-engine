/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.credential.azure;

import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;

/**
 * {@link CredentialProvider} of the tests, sending the bearer token <code>token-N</code>, where
 * <code>N</code> counts the calls to {@link #invalidate()}, starting with 1.
 */
public class TestingCredentials extends AbstractConfiguredInstance<TestingCredentials.Config>
		implements CredentialProvider {

	/**
	 * Configuration options for {@link TestingCredentials}.
	 */
	public interface Config extends PolymorphicConfiguration<TestingCredentials> {
		// No options.
	}

	private final AtomicInteger _generation = new AtomicInteger(1);

	/**
	 * Creates a {@link TestingCredentials} from configuration.
	 */
	public TestingCredentials(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void authenticate(RequestAuthentication request) {
		request.setHeader(AUTHORIZATION, BEARER + " token-" + _generation.get());
	}

	@Override
	public void invalidate() {
		_generation.incrementAndGet();
	}

}
