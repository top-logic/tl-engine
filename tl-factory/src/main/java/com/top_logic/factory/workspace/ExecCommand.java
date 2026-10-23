/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A command to run inside a running workspace with
 * {@link WorkspaceProvider#exec(String, ExecCommand, ExecOutput)}.
 *
 * <p>
 * The command is given as program and arguments, it is not interpreted by a shell. To run a shell
 * command line, run the shell explicitly, e.g. {@code sh -c "..."}.
 * </p>
 */
public final class ExecCommand {

	private final List<String> _command;

	private String _workingDir;

	private String _user;

	private final Map<String, String> _env = new LinkedHashMap<>();

	private ExecCommand(List<String> command) {
		if (command.isEmpty()) {
			throw new IllegalArgumentException("An empty command cannot be run.");
		}
		_command = Collections.unmodifiableList(new ArrayList<>(command));
	}

	/**
	 * Creates an {@link ExecCommand} running the given program with the given arguments.
	 */
	public static ExecCommand of(String... command) {
		return new ExecCommand(Arrays.asList(command));
	}

	/**
	 * Creates an {@link ExecCommand} running the given program with the given arguments.
	 */
	public static ExecCommand of(List<String> command) {
		return new ExecCommand(command);
	}

	/**
	 * Sets the directory inside the container the command runs in.
	 *
	 * @see #getWorkingDir()
	 */
	public ExecCommand setWorkingDir(String workingDir) {
		_workingDir = workingDir;
		return this;
	}

	/**
	 * Sets the user inside the container that runs the command.
	 *
	 * @see #getUser()
	 */
	public ExecCommand setUser(String user) {
		_user = user;
		return this;
	}

	/**
	 * Adds an environment variable to the environment of the command.
	 *
	 * @see #getEnv()
	 */
	public ExecCommand putEnv(String name, String value) {
		_env.put(name, value);
		return this;
	}

	/**
	 * The program to run followed by its arguments.
	 */
	public List<String> getCommand() {
		return _command;
	}

	/**
	 * The directory inside the container the command runs in, {@code null} for the working
	 * directory of the container.
	 */
	public String getWorkingDir() {
		return _workingDir;
	}

	/**
	 * The user that runs the command, {@code null} for the user of the container.
	 */
	public String getUser() {
		return _user;
	}

	/**
	 * Environment variables added to the environment of the container for this command.
	 */
	public Map<String, String> getEnv() {
		return Collections.unmodifiableMap(_env);
	}

	@Override
	public String toString() {
		return String.join(" ", _command);
	}

}
