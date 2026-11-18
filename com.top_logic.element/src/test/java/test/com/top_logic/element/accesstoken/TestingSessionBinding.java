/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.accesstoken;

import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.accesstoken.SessionBinding;

/**
 * {@link SessionBinding} of the test permissions that bind to the session: makes a permission
 * session-binding without an endpoint behind it.
 */
public class TestingSessionBinding extends AbstractConfiguredInstance<TestingSessionBinding.Config<?>>
		implements SessionBinding {

	/** The endpoint below the application. */
	public static final String ENDPOINT_PATH = "/test-endpoint";

	/**
	 * Configuration of {@link TestingSessionBinding}.
	 */
	public interface Config<I extends TestingSessionBinding> extends PolymorphicConfiguration<I> {
		// No properties.
	}

	/**
	 * Creates a {@link TestingSessionBinding} from configuration.
	 */
	@CalledByReflection
	public TestingSessionBinding(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void bind(String sessionKey, HttpSession session) {
		// No endpoint to admit anybody.
	}

	@Override
	public String endpoint(String applicationURL) {
		return applicationURL + ENDPOINT_PATH;
	}

}
