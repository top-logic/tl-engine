/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.accesstoken;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.accesstoken.SessionBinding;

/**
 * {@link SessionBinding} of test permissions that bind to the session, recording the sessions it
 * registers.
 */
public class RecordingSessionBinding extends AbstractConfiguredInstance<RecordingSessionBinding.Config<?>>
		implements SessionBinding {

	/** The endpoint below the application. */
	public static final String ENDPOINT_PATH = "/test-endpoint";

	/** The sessions registered, by key. */
	public static final Map<String, HttpSession> SESSIONS = new ConcurrentHashMap<>();

	/**
	 * Configuration of {@link RecordingSessionBinding}.
	 */
	public interface Config<I extends RecordingSessionBinding> extends PolymorphicConfiguration<I> {
		// No properties.
	}

	/**
	 * Creates a {@link RecordingSessionBinding} from configuration.
	 */
	@CalledByReflection
	public RecordingSessionBinding(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void bind(String sessionKey, HttpSession session) {
		SESSIONS.put(sessionKey, session);
	}

	@Override
	public String endpoint(String applicationURL) {
		return applicationURL + ENDPOINT_PATH;
	}

}
