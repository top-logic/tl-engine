/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A headless, server-side facade over a single React window's control tree.
 *
 * <p>
 * This is the interface a non-browser consumer uses to drive an application session: the script
 * recorder (capturing what a user does) and an AI agent (deciding what to do) are both clients of
 * the same three primitives:
 * </p>
 * <ul>
 * <li>{@link #observe()} — project the current control tree into an addressable
 * {@link AgentNodeView} (what is on screen, what it holds, what can be done),</li>
 * <li>{@link #resolve(String)} — turn a semantic address back into the live control, and</li>
 * <li>{@link #act(String, String, Map)} — invoke an advertised action on an addressed control.</li>
 * </ul>
 *
 * <p>
 * {@link #act(String, String, Map)} dispatches through {@link ReactControl#executeCommand} — the
 * exact entry point the browser command endpoint uses — so the headless interface cannot drift from
 * the real UI behavior; there is one implementation of what an action does.
 * </p>
 *
 * <p>
 * The observation is rooted at a synthetic {@code "/"} node (role {@code app}) whose single child is
 * the window's displayed root control (see {@link #forRoot(ReactControl)}). A {@link AgentSession} is
 * a thin, stateless view; create one per interaction.
 * </p>
 */
public final class AgentSession {

	/**
	 * Address of the synthetic root node.
	 */
	public static final String ROOT = AgentTreeProjector.SEPARATOR;

	private final ReactControl _root;

	private AgentSession(ReactControl root) {
		_root = root;
	}

	/**
	 * Creates a headless session rooted at the window's displayed root control.
	 *
	 * <p>
	 * The projection is exactly the subtree reachable from this root, so controls that are still
	 * registered in the window's queue but no longer reachable (orphaned navigation content) are not
	 * included. A {@code null} root yields an empty observation — the window simply has no rendered
	 * tree.
	 * </p>
	 *
	 * @param root
	 *        The window's root control, or {@code null} if none is set.
	 * @return A new session.
	 */
	public static AgentSession forRoot(ReactControl root) {
		return new AgentSession(root);
	}

	/**
	 * The visible top-level controls: the window root, or its visible children if the root itself is
	 * a structural wrapper.
	 */
	private List<ReactControl> roots() {
		return _root == null ? List.of() : AgentTreeProjector.visibleRoots(_root);
	}

	/**
	 * Projects the current control tree into an addressable observation rooted at the synthetic
	 * {@link #ROOT} node.
	 *
	 * @return The root {@link AgentNodeView}; its {@link AgentNodeView#children() children} are the
	 *         window's root controls.
	 */
	public AgentNodeView observe() {
		List<ReactControl> roots = roots();
		List<String> segments = AgentTreeProjector.segmentsFor(roots);
		List<AgentNodeView> rootViews = new ArrayList<>(roots.size());
		for (int i = 0; i < roots.size(); i++) {
			String address = AgentTreeProjector.join(ROOT, segments.get(i));
			rootViews.add(AgentTreeProjector.project(roots.get(i), address));
		}
		return new AgentNodeView(ROOT, "app", null, null, Map.of(), List.of(), rootViews);
	}

	/**
	 * Convenience: the {@link #observe() observation} rendered as indented JSON.
	 */
	public String observeJson() {
		return observe().toJson();
	}

	/**
	 * The affordance-first view: a flat list of the actionable nodes (each with its address, role,
	 * name, semantic state and actions, but no children), rendered as JSON.
	 *
	 * <p>
	 * This is the compact view for an agent loop — it answers "what can I do right now and where"
	 * without the surrounding container hierarchy. Addresses are the same ones {@link #act} accepts.
	 * </p>
	 */
	public String observeActionsJson() {
		List<Object> entries = new ArrayList<>();
		for (AgentNodeView node : observe().actionableNodes()) {
			entries.add(node.toMap(false));
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("actions", entries);
		return AgentNodeView.toJson(result);
	}

	/**
	 * Resolves a semantic address to the live control it designates.
	 *
	 * @param address
	 *        An address as produced by {@link #observe()} (e.g. {@code "/form/button[Submit]"}).
	 * @return The control at that address, or {@code null} for the synthetic {@link #ROOT}.
	 * @throws IllegalArgumentException
	 *         If no control matches the address.
	 */
	public ReactControl resolve(String address) {
		List<String> segments = splitAddress(address);
		if (segments.isEmpty()) {
			return null;
		}

		List<ReactControl> candidates = roots();
		ReactControl current = null;
		StringBuilder walked = new StringBuilder();
		for (String segment : segments) {
			List<String> available = AgentTreeProjector.segmentsFor(candidates);
			int index = available.indexOf(segment);
			if (index < 0) {
				throw new IllegalArgumentException("No node at address '" + address + "': segment '"
					+ segment + "' not found under '" + (walked.length() == 0 ? ROOT : walked.toString())
					+ "'. Available: " + available);
			}
			current = candidates.get(index);
			candidates = AgentTreeProjector.visibleChildren(current);
			walked.append(AgentTreeProjector.SEPARATOR).append(segment);
		}
		return current;
	}

	/**
	 * Invokes an action on the control at the given address.
	 *
	 * <p>
	 * Equivalent to resolving the address and calling
	 * {@link ReactControl#executeCommand(String, Map)} with the command and arguments — the same path
	 * the browser uses. State changes the command makes become visible in the next
	 * {@link #observe()}.
	 * </p>
	 *
	 * @param address
	 *        The address of the target control.
	 * @param command
	 *        The command ID (an advertised {@link AgentAction#command()}).
	 * @param arguments
	 *        The command arguments, or an empty map.
	 * @return The {@link HandlerResult} of the command.
	 * @throws IllegalArgumentException
	 *         If the address does not resolve to a control.
	 */
	public HandlerResult act(String address, String command, Map<String, Object> arguments) {
		ReactControl control = resolve(address);
		if (control == null) {
			throw new IllegalArgumentException("Cannot act on the synthetic root node '" + address + "'.");
		}
		return control.executeCommand(command, arguments == null ? Map.of() : arguments);
	}

	private static List<String> splitAddress(String address) {
		List<String> result = new ArrayList<>();
		if (address == null) {
			return result;
		}
		for (String part : address.split(AgentTreeProjector.SEPARATOR)) {
			if (!part.isEmpty()) {
				result.add(part);
			}
		}
		return result;
	}
}
