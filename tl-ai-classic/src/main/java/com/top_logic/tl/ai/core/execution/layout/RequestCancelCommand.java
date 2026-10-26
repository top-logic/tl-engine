/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution.layout;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClassifier;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Requests cancellation of the selected {@link AgentExecution}.
 *
 * <p>
 * Deliberately not a TL-Script transaction writing the run's cancel marker directly: the marker has
 * to be written by the agent execution store, which runs on the store's writer thread and re-reads
 * the run's status at the head revision first, so a run that is concurrently finishing on another
 * cluster node is seen correctly. A plain UI transaction bypasses both.
 * </p>
 *
 * <p>
 * Cancellation is asynchronous. The node executing the run observes the committed marker at its
 * next agent-loop turn; the run then finalizes as cancelled. The message shown here says so rather
 * than pretending the run is already over.
 * </p>
 *
 * @see AgentExecutionStore#requestCancel(AgentExecution)
 */
public class RequestCancelCommand extends PreconditionCommandHandler {

	/**
	 * Configuration options for {@link RequestCancelCommand}.
	 */
	public interface Config extends PreconditionCommandHandler.Config {

		@Override
		@FormattedDefault("css:fa-solid fa-ban")
		ThemeImage getImage();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault("class.com.top_logic.tl.ai.core.execution.layout.I18NConstants.REQUEST_CANCEL")
		ResKey getResourceKey();
	}

	/**
	 * Creates a {@link RequestCancelCommand} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public RequestCancelCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof AgentExecution execution)) {
			return new Failure(I18NConstants.REQUEST_CANCEL_DISABLED_NO_RUN);
		}
		if (!AgentExecutionStore.Module.INSTANCE.isActive()) {
			return new Failure(I18NConstants.REQUEST_CANCEL_DISABLED_NO_STORE);
		}
		if (!isRunning(execution)) {
			return new Failure(I18NConstants.REQUEST_CANCEL_DISABLED_TERMINAL);
		}
		if (Boolean.TRUE.equals(execution.getCancelRequested())) {
			return new Failure(I18NConstants.REQUEST_CANCEL_DISABLED_PENDING);
		}
		return Success.toSuccess(context -> {
			if (AgentExecutionStore.getInstance().requestCancel(execution)) {
				InfoService.showInfo(I18NConstants.REQUEST_CANCEL_SENT);
			} else {
				// The run reached a terminal state between rendering the button and pressing it.
				InfoService.showInfo(I18NConstants.REQUEST_CANCEL_NOT_RUNNING);
			}
		});
	}

	private static boolean isRunning(AgentExecution execution) {
		TLClassifier status = execution.getStatus();
		return status != null && ExecutionStatus.RUNNING.name().equals(status.getName());
	}
}
