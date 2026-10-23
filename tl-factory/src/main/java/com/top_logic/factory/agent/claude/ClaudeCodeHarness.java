/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent.claude;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.factory.agent.AgentHarness;
import com.top_logic.factory.agent.AgentLaunch;
import com.top_logic.factory.agent.AgentProtocol;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;

/**
 * {@link AgentHarness} running Claude Code in print mode with stream-json input and output.
 *
 * <p>
 * The process is started as
 * </p>
 *
 * <pre>
 * claude -p --input-format stream-json --output-format stream-json --verbose --include-partial-messages
 *     --permission-mode bypassPermissions [--model &lt;model&gt;] [--resume &lt;session ID&gt;] [&lt;extra args&gt;]
 * </pre>
 *
 * <p>
 * as the {@link ApplicationWorkspaces#WORKSPACE_USER workspace user} - Claude Code refuses to
 * bypass its permission prompts as root - with the token of the process in
 * {@link LLMProxyService#ENV_AUTH_TOKEN}. The container has the URL of the LLM proxy in
 * {@link LLMProxyService#ENV_BASE_URL}. The output is translated by {@link ClaudeCodeProtocol}.
 * </p>
 */
public class ClaudeCodeHarness extends AbstractConfiguredInstance<ClaudeCodeHarness.Config<?>>
		implements AgentHarness {

	/**
	 * Configuration of {@link ClaudeCodeHarness}.
	 */
	public interface Config<I extends ClaudeCodeHarness> extends PolymorphicConfiguration<I> {

		@Override
		@ClassDefault(ClaudeCodeHarness.class)
		Class<? extends I> getImplementationClass();

		/** Configuration property name for {@link #getCommand()}. */
		String COMMAND = "command";

		/** Configuration property name for {@link #getModel()}. */
		String MODEL = "model";

		/** Configuration property name for {@link #getExtraArgs()}. */
		String EXTRA_ARGS = "extra-args";

		/**
		 * The program of Claude Code in the workspace image.
		 */
		@Name(COMMAND)
		@StringDefault(DEFAULT_COMMAND)
		String getCommand();

		/**
		 * The model Claude Code works with, e.g. {@code claude-sonnet-4-6} or an alias such as
		 * {@code sonnet}; empty for the default of Claude Code.
		 */
		@Name(MODEL)
		@Nullable
		String getModel();

		/**
		 * Further arguments appended to the command line, e.g. {@code --max-turns,50}.
		 */
		@Name(EXTRA_ARGS)
		@Format(CommaSeparatedStrings.class)
		List<String> getExtraArgs();

	}

	/** The default of {@link Config#getCommand()}. */
	public static final String DEFAULT_COMMAND = "claude";

	/** The name the runs of Claude Code are recorded under. */
	public static final String AGENT_NAME = "claude-code";

	/** Option: print mode, no interactive session. */
	public static final String OPTION_PRINT = "-p";

	/** Option: the format of the input. */
	public static final String OPTION_INPUT_FORMAT = "--input-format";

	/** Option: the format of the output. */
	public static final String OPTION_OUTPUT_FORMAT = "--output-format";

	/** Format of {@link #OPTION_INPUT_FORMAT} and {@link #OPTION_OUTPUT_FORMAT}: a JSON object per line. */
	public static final String FORMAT_STREAM_JSON = "stream-json";

	/** Option: every message in the output, required for {@link #FORMAT_STREAM_JSON} output. */
	public static final String OPTION_VERBOSE = "--verbose";

	/** Option: the events of the Messages API in the output, as they are streamed. */
	public static final String OPTION_INCLUDE_PARTIAL_MESSAGES = "--include-partial-messages";

	/** Option: how Claude Code asks for permissions. */
	public static final String OPTION_PERMISSION_MODE = "--permission-mode";

	/** Mode of {@link #OPTION_PERMISSION_MODE}: no prompts; the container is the boundary. */
	public static final String PERMISSION_MODE_BYPASS = "bypassPermissions";

	/** Option: the model. */
	public static final String OPTION_MODEL = "--model";

	/** Option: continue the conversation with the given session ID. */
	public static final String OPTION_RESUME = "--resume";

	/**
	 * Creates a {@link ClaudeCodeHarness} from configuration.
	 */
	public ClaudeCodeHarness(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public String agentName() {
		return AGENT_NAME;
	}

	@Override
	public ExecCommand command(AgentLaunch launch) {
		Config<?> config = getConfig();
		List<String> command = new ArrayList<>();
		command.add(config.getCommand());
		command.add(OPTION_PRINT);
		command.add(OPTION_INPUT_FORMAT);
		command.add(FORMAT_STREAM_JSON);
		command.add(OPTION_OUTPUT_FORMAT);
		command.add(FORMAT_STREAM_JSON);
		command.add(OPTION_VERBOSE);
		command.add(OPTION_INCLUDE_PARTIAL_MESSAGES);
		command.add(OPTION_PERMISSION_MODE);
		command.add(PERMISSION_MODE_BYPASS);
		String model = config.getModel();
		if (model != null && !model.isBlank()) {
			command.add(OPTION_MODEL);
			command.add(model.strip());
		}
		String resume = launch.resumeSessionId();
		if (resume != null && !resume.isEmpty()) {
			command.add(OPTION_RESUME);
			command.add(resume);
		}
		for (String arg : config.getExtraArgs()) {
			if (!arg.isBlank()) {
				command.add(arg.strip());
			}
		}
		return ExecCommand.of(command)
			.setWorkingDir(launch.workingDir())
			.setUser(ApplicationWorkspaces.WORKSPACE_USER)
			.putEnv(LLMProxyService.ENV_AUTH_TOKEN, launch.token());
	}

	@Override
	public AgentProtocol newProtocol() {
		return new ClaudeCodeProtocol(Clock.systemUTC());
	}

}
