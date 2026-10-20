/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * What this server declares about a published tool, derived from the tool's own {@link ToolMode} and
 * {@link Tool#group() group}.
 *
 * <p>
 * The MCP {@code annotations} of a tool definition are hints for the <em>client</em>: they are how a
 * client that has never heard of TopLogic can tell that {@code listModules} only reads and
 * {@code deleteInstance} does not. Publishing them is an interoperability service, and nothing else:
 * a hint is a server's claim about itself, which the specification tells clients not to base trust
 * decisions on. This application's own consuming side takes that literally and reads none of them -
 * every tool reached over MCP is {@link ToolMode#EXTERNAL} whatever the far end says about it. So
 * the table below buys a foreign client an informed prompt and buys a tl-ai client nothing, by
 * design in both directions.
 * </p>
 *
 * <table>
 * <caption>How a tool's mode and group become the four hints</caption>
 * <tr>
 * <th>{@link ToolMode}</th>
 * <th>{@code readOnlyHint}</th>
 * <th>{@code destructiveHint}</th>
 * <th>{@code idempotentHint}</th>
 * <th>{@code openWorldHint}</th>
 * </tr>
 * <tr>
 * <td>{@link ToolMode#READ READ}</td>
 * <td>{@code true}</td>
 * <td>{@code false}</td>
 * <td>unset</td>
 * <td>{@code true} iff {@link ToolGroups#EGRESS}</td>
 * </tr>
 * <tr>
 * <td>{@link ToolMode#USER_SCOPED USER_SCOPED}</td>
 * <td>{@code false}</td>
 * <td>{@code false}</td>
 * <td>unset</td>
 * <td>{@code true} iff {@link ToolGroups#EGRESS}</td>
 * </tr>
 * <tr>
 * <td>{@link ToolMode#DATA_WRITE DATA_WRITE}</td>
 * <td>{@code false}</td>
 * <td>{@code true}</td>
 * <td>unset</td>
 * <td>{@code true} iff {@link ToolGroups#EGRESS}</td>
 * </tr>
 * <tr>
 * <td>{@link ToolMode#STRUCTURAL STRUCTURAL}</td>
 * <td>{@code false}</td>
 * <td>{@code true}</td>
 * <td>unset</td>
 * <td>{@code true} iff {@link ToolGroups#EGRESS}</td>
 * </tr>
 * <tr>
 * <td>{@link ToolMode#EXTERNAL EXTERNAL}</td>
 * <td>{@code false}</td>
 * <td>{@code true}</td>
 * <td>unset</td>
 * <td>{@code true}</td>
 * </tr>
 * </table>
 *
 * <p>
 * <b>{@code USER_SCOPED} publishes a write.</b> The hints have two states and this mode is a third
 * thing: it changes something, but only the calling account's own workspace - a task, a personal
 * page, a memory kept about that user. {@code readOnlyHint: true} would be false, because something
 * did change and a client that caches reads would be wrong to cache this one. So it is a write, and
 * of the two kinds of write it is the harmless one: what it can damage belongs to the account making
 * the call, and no other user's data is reachable from it. That is what {@code destructiveHint:
 * false} says to a client deciding how loudly to ask.
 * </p>
 *
 * <p>
 * <b>{@code destructiveHint} is stated even for {@code READ}</b>, where the specification calls it
 * meaningful only when {@code readOnlyHint == false}. Its default is {@code true}, so a client that
 * reads the one field without the other - which is a thing clients do - would otherwise take a
 * read-only tool for a destructive one. Saying {@code false} costs a field and cannot be misread.
 * </p>
 *
 * <p>
 * <b>{@code idempotentHint} is left unset</b>, because nothing in the tool contract answers it. The
 * mode says how deeply a call changes this application, not whether calling it twice differs from
 * calling it once, and inventing an answer per mode would be a claim with nothing behind it. Unset
 * means the client's default, {@code false}, which is the careful reading.
 * </p>
 *
 * <p>
 * <b>{@code openWorldHint} comes from the group, not only from the mode</b>, and it has to: the mode
 * describes what a call changes <em>here</em> and says nothing about where its data goes.
 * {@code webFetch} is {@link ToolMode#READ} and reaches the whole internet, which is the
 * specification's own example of an open world. {@link ToolGroups#EGRESS} is the axis that carries
 * that property, so it is the one asked, in addition to {@link ToolMode#EXTERNAL} - which leaves the
 * application by definition.
 * </p>
 *
 * @see McpToolAdapter#adapt(McpExposure.Published)
 */
public final class McpToolAnnotations {

	/**
	 * The annotations to publish for the given tool.
	 *
	 * <p>
	 * The {@link io.modelcontextprotocol.spec.McpSchema.ToolAnnotations#title() title} is carried
	 * here as well as on the tool definition itself. A client's display name for a tool is
	 * {@code title}, then {@code annotations.title}, then {@code name}; the first field arrived
	 * with the 2025-06-18 revision, so a client older than that finds a readable name only in the
	 * second one - and {@code name} is the wire name, which is not it.
	 * </p>
	 *
	 * @param tool
	 *        The tool being published.
	 * @param title
	 *        The name a person reads for it.
	 * @return The {@code annotations} object of its MCP definition, never <code>null</code>.
	 */
	public static McpSchema.ToolAnnotations of(Tool tool, String title) {
		ToolMode mode = tool.mode();

		boolean readOnly = mode == ToolMode.READ;
		boolean destructive = switch (mode) {
			case READ, USER_SCOPED -> false;
			case DATA_WRITE, STRUCTURAL, EXTERNAL -> true;
		};
		boolean openWorld = mode == ToolMode.EXTERNAL || ToolGroups.EGRESS.equals(tool.group());

		return new McpSchema.ToolAnnotations(title, Boolean.valueOf(readOnly),
			Boolean.valueOf(destructive), null, Boolean.valueOf(openWorld), null);
	}

	private McpToolAnnotations() {
		// Static utility.
	}

}
