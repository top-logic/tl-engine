/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Guards the shipped default of the {@link MCPServerService.Module} switch.
 *
 * <p>
 * {@code /mcp/*} authenticates nobody: the servlet is mapped with no filter and no
 * {@code security-constraint}, and every resource handler reads the KnowledgeBase in a system
 * interaction, which bypasses model access rights. Serving that endpoint is therefore an
 * administrative decision and not a default - see {@code doc/security/mcp-endpoint.md} and issue #261.
 * </p>
 *
 * <p>
 * The value is one character in a configuration file, so nothing else in the build notices when it
 * flips back. This test does, by reading the shipped file rather than the booted configuration: the
 * question is what an application inherits when it configures nothing, and a booted
 * {@code ModuleSystem} has already merged every override on the classpath.
 * </p>
 */
public class TestShippedModuleDefault extends TestCase {

	/** Directory name of this Maven module, for a working directory above it. */
	private static final String MODULE_NAME = "tl-ai-mcp-server";

	/** Path of the shipped configuration, relative to this module's base directory. */
	private static final String CONFIG_PATH =
		"src/main/webapp/WEB-INF/conf/tl-ai-mcp-server.conf.config.xml";

	/** Matches the module entry and captures its value, whatever the attribute order. */
	private static final Pattern MODULE_ENTRY = Pattern.compile(
		"<module\\s+key=\"com\\.top_logic\\.ai\\.mcp\\.server\\.MCPServerService\\$Module\"\\s+value=\"([^\"]*)\"");

	/**
	 * The MCP server module is off in the configuration this module ships.
	 */
	public void testTheMcpServerModuleIsOffByDefault() throws IOException {
		File config = shippedConfig();
		String contents = Files.readString(config.toPath(), StandardCharsets.UTF_8);

		Matcher entry = MODULE_ENTRY.matcher(contents);
		assertTrue("No entry for MCPServerService$Module in " + config
			+ " - the switch must stay declared, and declared as false.", entry.find());
		assertEquals("The MCP endpoint authenticates nobody (issue #261), so this module must ship"
			+ " switched off. Do not flip it here: an application that wants the endpoint overrides"
			+ " the module in its own configuration - see doc/security/mcp-endpoint.md.",
			"false", entry.group(1));
	}

	/**
	 * A request that arrives while the endpoint is off is told which switch turns it on.
	 *
	 * <p>
	 * The status code is the servlet's business and needs a container to observe; the wording is not,
	 * and it is the part that decides whether a 503 on {@code /mcp/*} is diagnosable at all.
	 * </p>
	 */
	public void testTheOffStateNamesItsSwitch() {
		String message = MCPTransportServlet.SERVICE_DISABLED_MESSAGE;

		assertTrue("The answer must name the module an administrator has to enable.",
			message.contains("com.top_logic.ai.mcp.server.MCPServerService$Module"));
		assertTrue("The answer must say that off is the default, not an accident.",
			message.contains("off by default"));
		assertTrue("The answer must name the reason, so nobody re-enables it without reading it.",
			message.contains("authenticates nobody"));
		assertTrue("The answer must point at the tracked follow-up.", message.contains("#261"));
	}

	/**
	 * The shipped configuration file, located from the working directory.
	 *
	 * @return An existing file, never <code>null</code>.
	 */
	private static File shippedConfig() {
		File base = new File("").getAbsoluteFile();
		while (base != null) {
			File inModule = new File(base, CONFIG_PATH);
			if (inModule.isFile()) {
				return inModule;
			}

			// Tolerate a working directory above the module, as a reactor build may have.
			File belowReactorRoot = new File(new File(base, MODULE_NAME), CONFIG_PATH);
			if (belowReactorRoot.isFile()) {
				return belowReactorRoot;
			}

			base = base.getParentFile();
		}
		throw new AssertionError("Cannot locate " + CONFIG_PATH + " from "
			+ new File("").getAbsolutePath() + ".");
	}
}
