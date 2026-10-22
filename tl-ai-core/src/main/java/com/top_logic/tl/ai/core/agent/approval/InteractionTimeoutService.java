/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import java.time.Duration;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * The deadlines a human gets to answer an agent: how long a tool approval and how long a question
 * asked through {@link com.top_logic.tl.ai.core.tools.interaction.AskUserTool} wait for a response.
 *
 * <p>
 * Both are measured from the moment the request is raised, and both are chosen for a person, not
 * for a machine: an end user who is asked to approve a write to shared business data has to be able
 * to walk over to a colleague and come back. An application that wants a different deadline - a
 * kiosk that must not park a session for half an hour, an approval that a supervisor answers only
 * when they get back to their desk - sets it here.
 * </p>
 *
 * <p>
 * What expiry <em>does</em> is not configurable: an expired approval resolves as
 * {@link ApprovalOutcome#TIMED_OUT} (the tool call is denied), and an expired question returns a
 * note telling the agent to go on with an assumption it names in its answer. These settings only
 * decide when that happens.
 * </p>
 */
public class InteractionTimeoutService extends ConfiguredManagedClass<InteractionTimeoutService.Config<?>> {

	/**
	 * The approval deadline used when this service is not running.
	 *
	 * @see Config#getApprovalTimeoutMinutes()
	 */
	public static final Duration DEFAULT_APPROVAL_TIMEOUT = Duration.ofMinutes(30);

	/**
	 * The question deadline used when this service is not running.
	 *
	 * @see Config#getQuestionTimeoutMinutes()
	 */
	public static final Duration DEFAULT_QUESTION_TIMEOUT = Duration.ofMinutes(30);

	/**
	 * Configuration of {@link InteractionTimeoutService}.
	 */
	public interface Config<I extends InteractionTimeoutService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name for the approval timeout.
		 *
		 * @see #getApprovalTimeoutMinutes()
		 */
		String APPROVAL_TIMEOUT_MINUTES = "approval-timeout-minutes";

		/**
		 * Configuration property name for the question timeout.
		 *
		 * @see #getQuestionTimeoutMinutes()
		 */
		String QUESTION_TIMEOUT_MINUTES = "question-timeout-minutes";

		/**
		 * How long a tool call waits for the user to approve or reject it, in minutes.
		 *
		 * <p>
		 * When the deadline passes, the approval is treated as refused: the tool call is denied and
		 * the agent is told the approval timed out.
		 * </p>
		 *
		 * @see #APPROVAL_TIMEOUT_MINUTES
		 */
		@Name(APPROVAL_TIMEOUT_MINUTES)
		@IntDefault(30)
		int getApprovalTimeoutMinutes();

		/**
		 * How long a question the agent asked waits for the user's answer, in minutes.
		 *
		 * <p>
		 * When the deadline passes, the agent is told that nobody answered, and to go on with an
		 * assumption it names in its answer. A chat application sets a few minutes here.
		 * </p>
		 *
		 * @see #QUESTION_TIMEOUT_MINUTES
		 */
		@Name(QUESTION_TIMEOUT_MINUTES)
		@IntDefault(30)
		int getQuestionTimeoutMinutes();
	}

	private static volatile InteractionTimeoutService _instance;

	/**
	 * Creates a {@link InteractionTimeoutService} from configuration.
	 */
	public InteractionTimeoutService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The running instance, or <code>null</code> if the service is not started.
	 */
	public static InteractionTimeoutService getInstance() {
		return _instance;
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
	 * How long a tool approval waits for the user's decision.
	 *
	 * <p>
	 * Falls back to {@link #DEFAULT_APPROVAL_TIMEOUT} when the service is not started, which is the
	 * case in unit tests and in embedded uses that boot no module system. An application always has
	 * it running - it is switched on unconditionally in {@code tl-ai-core.conf.config.xml}.
	 * </p>
	 */
	public static Duration approvalTimeout() {
		InteractionTimeoutService service = getInstance();
		return service == null
			? DEFAULT_APPROVAL_TIMEOUT
			: Duration.ofMinutes(service.getConfig().getApprovalTimeoutMinutes());
	}

	/**
	 * How long a question asked through
	 * {@link com.top_logic.tl.ai.core.tools.interaction.AskUserTool} waits for the user's answer.
	 *
	 * @see #approvalTimeout() For the fallback when the service is not started.
	 */
	public static Duration questionTimeout() {
		InteractionTimeoutService service = getInstance();
		return service == null
			? DEFAULT_QUESTION_TIMEOUT
			: Duration.ofMinutes(service.getConfig().getQuestionTimeoutMinutes());
	}

	/**
	 * {@link TypedRuntimeModule} for {@link InteractionTimeoutService}.
	 */
	public static final class Module extends TypedRuntimeModule<InteractionTimeoutService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<InteractionTimeoutService> getImplementation() {
			return InteractionTimeoutService.class;
		}
	}
}
