/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tl.ai.core.execution.model.AgentSession;

/**
 * Decides which conversations of the chat are answered by a {@link ConversationBackend} rather
 * than by the framework's own agent.
 *
 * <p>
 * The {@link Config#getResolvers() resolvers} are asked in the order they are configured; the
 * first that names a backend wins. A conversation no resolver claims - and every conversation
 * while this service is not started - is answered by the framework's own agent.
 * </p>
 *
 * <p>
 * This module configures the service with no resolver. An application adds its own in its
 * configuration:
 * </p>
 *
 * <pre>
 * &lt;config service-class="com.top_logic.ai.chat.backend.ConversationBackendService"&gt;
 *     &lt;instance&gt;
 *         &lt;resolvers&gt;
 *             &lt;resolver class="my.app.WorkspaceBackendResolver"/&gt;
 *         &lt;/resolvers&gt;
 *     &lt;/instance&gt;
 * &lt;/config&gt;
 * </pre>
 */
public class ConversationBackendService extends ConfiguredManagedClass<ConversationBackendService.Config<?>> {

	/**
	 * Configuration of {@link ConversationBackendService}.
	 */
	public interface Config<I extends ConversationBackendService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for {@link #getResolvers()}.
		 */
		String RESOLVERS = "resolvers";

		/**
		 * Tag of an entry of {@link #getResolvers()}.
		 */
		String RESOLVER = "resolver";

		/**
		 * What decides which conversations an external backend answers.
		 *
		 * <p>
		 * Asked in this order before every run of a conversation; the first resolver that names a
		 * backend wins. Empty, every conversation is answered by the framework's own agent.
		 * </p>
		 *
		 * @see #RESOLVERS
		 */
		@Name(RESOLVERS)
		@EntryTag(RESOLVER)
		@Label("Conversation backend resolvers")
		List<PolymorphicConfiguration<ConversationBackendResolver>> getResolvers();
	}

	private static volatile ConversationBackendService _instance;

	private final List<ConversationBackendResolver> _resolvers;

	/**
	 * Creates a {@link ConversationBackendService} from configuration.
	 */
	public ConversationBackendService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_resolvers = List.copyOf(TypedConfiguration.getInstanceList(context, config.getResolvers()));
	}

	@Override
	protected void startUp() {
		super.startUp();
		_instance = this;
	}

	@Override
	protected void shutDown() {
		_instance = null;
		super.shutDown();
	}

	/**
	 * The backend that answers the given conversation, or <code>null</code> when the framework's
	 * own agent answers it.
	 *
	 * @param session
	 *        The conversation; <code>null</code> (a run that is not recorded) is answered by the
	 *        framework's own agent.
	 */
	public static ConversationBackend backendFor(AgentSession session) {
		ConversationBackendService service = _instance;
		if (service == null || session == null) {
			return null;
		}
		return service.resolve(session);
	}

	private ConversationBackend resolve(AgentSession session) {
		for (ConversationBackendResolver resolver : _resolvers) {
			ConversationBackend backend = resolver.backendFor(session);
			if (backend != null) {
				return backend;
			}
		}
		return null;
	}

	/**
	 * Singleton reference for {@link ConversationBackendService}.
	 */
	public static final class Module extends TypedRuntimeModule<ConversationBackendService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ConversationBackendService> getImplementation() {
			return ConversationBackendService.class;
		}
	}
}
