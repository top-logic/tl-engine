/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.function.Function;

import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendResolver;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * A {@link ConversationBackendResolver} whose answers a test installs, registered in the test
 * configuration of this module.
 *
 * <p>
 * Installed statically, like {@link ScriptedModel}'s script, because the resolver is created once at
 * service startup. Without an installed answer it claims no subject and no subject type, so every
 * other test runs the framework's own agent.
 * </p>
 */
public class ScriptedBackend implements ConversationBackendResolver {

	private static final Function<TLObject, ConversationBackend> NO_BACKENDS = subject -> null;

	private static final Function<TLStructuredType, BackendFeatures> NO_FEATURES = subjectType -> null;

	private static volatile Function<TLObject, ConversationBackend> _backends = NO_BACKENDS;

	private static volatile Function<TLStructuredType, BackendFeatures> _features = NO_FEATURES;

	/** Creates a {@link ScriptedBackend} from configuration. */
	public ScriptedBackend(InstantiationContext context, PolymorphicConfiguration<?> config) {
		// Nothing configured; see install.
	}

	/**
	 * Installs which backend answers a conversation about which subject, <code>null</code> for
	 * none; claims no subject type.
	 */
	public static void install(Function<TLObject, ConversationBackend> backends) {
		install(backends, NO_FEATURES);
	}

	/**
	 * Installs which backend answers a conversation about which subject, and what a conversation
	 * about a subject of which type offers; <code>null</code> for none.
	 */
	public static void install(Function<TLObject, ConversationBackend> backends,
			Function<TLStructuredType, BackendFeatures> features) {
		_backends = backends;
		_features = features;
	}

	/** Claims no subject and no subject type again. */
	public static void reset() {
		install(NO_BACKENDS, NO_FEATURES);
	}

	@Override
	public ConversationBackend backendFor(TLObject subject) {
		return _backends.apply(subject);
	}

	@Override
	public BackendFeatures featuresFor(TLStructuredType subjectType) {
		return _features.apply(subjectType);
	}
}
