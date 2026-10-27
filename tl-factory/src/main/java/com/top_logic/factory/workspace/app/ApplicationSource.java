/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.app;

import java.time.Duration;

import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.util.error.TopLogicException;

/**
 * Where the sources of an application {@link ApplicationWorkspaces#create(ApplicationSpec, ExecOutput)
 * created} in a workspace come from.
 *
 * <p>
 * The source runs in the started workspace, before the application is built. It puts the sources
 * into the {@link Context#appDir() directory of the application}, e.g. by generating them from the
 * archetype ({@link #ARCHETYPE}, the default) or by cloning a repository.
 * </p>
 *
 * @see ApplicationSpec#setSource(ApplicationSource)
 */
public interface ApplicationSource {

	/**
	 * The default source: the application is {@link Context#generate() generated} from the
	 * archetype.
	 */
	ApplicationSource ARCHETYPE = Context::generate;

	/**
	 * Puts the sources of the application into its directory in the workspace.
	 *
	 * @param context
	 *        The commands of the workspace and the application to create.
	 * @throws TopLogicException
	 *         If a step fails; the workspace is left as it is.
	 */
	void provide(Context context) throws InterruptedException;

	/**
	 * What an {@link ApplicationSource} works with: the application to create and the commands in
	 * its started workspace.
	 */
	interface Context {

		/**
		 * The application to create.
		 */
		ApplicationSpec spec();

		/**
		 * The TopLogic version the application is built on: the one of the {@link #spec()}, or
		 * the configured default.
		 */
		String engineVersion();

		/**
		 * The directory in the workspace the sources of the application belong in, see
		 * {@link ApplicationWorkspaces#appDir(String)}.
		 */
		String appDir();

		/**
		 * The longest time a long-running command may take, e.g. the clone of a repository.
		 *
		 * @see ApplicationWorkspaces.Config#getBuildTimeout()
		 */
		Duration commandTimeout();

		/**
		 * Writes a progress line naming the next step to the output of the operation.
		 */
		void step(String message);

		/**
		 * Runs a command in the workspace, its output written to the output of the operation.
		 *
		 * @throws TopLogicException
		 *         If the command ends with another exit code than {@code 0}; the message has the
		 *         end of its output.
		 */
		void run(ExecCommand command) throws InterruptedException;

		/**
		 * Runs a command in the workspace and returns its standard output, which is not written to
		 * the output of the operation; its error output is.
		 *
		 * @throws TopLogicException
		 *         If the command ends with another exit code than {@code 0}.
		 */
		String output(ExecCommand command) throws InterruptedException;

		/**
		 * Runs a command in the workspace, its output written to the given output.
		 *
		 * @return The exit code.
		 */
		int exec(ExecCommand command, ExecOutput output) throws InterruptedException;

		/**
		 * Generates the application from the archetype into its {@link #appDir() directory},
		 * which must not exist.
		 */
		void generate() throws InterruptedException;

	}

}
