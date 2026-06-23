/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.top_logic.layout.react.control.ReactControl;

/**
 * Projects a {@link ReactControl} subtree into an addressable {@link AgentNodeView} tree, assigning
 * each node a stable semantic address.
 *
 * <p>
 * <b>Addressing.</b> A node's address is a {@code /}-separated path of segments, one per ancestor.
 * Each segment is built from the node's {@link #roleOf(ReactControl) role} plus an optional
 * {@link #nameOf(ReactControl) name} discriminator, e.g. {@code button[Submit]} or {@code field}.
 * When several siblings share the same base segment, an occurrence index {@code #k} is appended to
 * keep the address unique ({@code row#0}, {@code row#1}). Crucially, the address derives from
 * <em>semantic</em> properties (role, bound name) and the tree's <em>shape</em>, not from the opaque
 * per-session control IDs ({@code v17}) — so an address recorded in one session resolves to the
 * corresponding control in a fresh session as long as the view structure is unchanged.
 * </p>
 *
 * <p>
 * The same segment algorithm is used for resolution by {@link AgentSession}, so addresses round-trip
 * exactly.
 * </p>
 */
public final class AgentTreeProjector {

	/**
	 * Address segment separator.
	 */
	public static final String SEPARATOR = "/";

	private AgentTreeProjector() {
		// Static utility.
	}

	/**
	 * Projects a control and its subtree, with the control placed at the given address.
	 *
	 * @param control
	 *        The root control of the subtree to project.
	 * @param address
	 *        The address at which {@code control} sits.
	 * @return The projected node view.
	 */
	public static AgentNodeView project(ReactControl control, String address) {
		List<ReactControl> kids = control.agentChildren();
		List<String> segments = segmentsFor(kids);

		List<AgentNodeView> childViews = new ArrayList<>(kids.size());
		for (int i = 0; i < kids.size(); i++) {
			String childAddress = join(address, segments.get(i));
			childViews.add(project(kids.get(i), childAddress));
		}

		return new AgentNodeView(address, roleOf(control), nameOf(control), control.getReactModule(),
			stateOf(control), actionsOf(control), childViews);
	}

	/**
	 * Joins a parent address and a child segment.
	 *
	 * @param parent
	 *        The parent address (the root is {@value #SEPARATOR}).
	 * @param segment
	 *        The child segment.
	 * @return The joined address.
	 */
	public static String join(String parent, String segment) {
		if (parent.endsWith(SEPARATOR)) {
			return parent + segment;
		}
		return parent + SEPARATOR + segment;
	}

	/**
	 * Computes the disambiguated address segments for a list of sibling controls, in order.
	 *
	 * <p>
	 * Siblings sharing a base segment receive an {@code #k} occurrence suffix; unique base segments
	 * are left bare. The result is positionally aligned with {@code siblings}.
	 * </p>
	 *
	 * @param siblings
	 *        The sibling controls.
	 * @return The segment for each sibling, in the same order.
	 */
	public static List<String> segmentsFor(List<ReactControl> siblings) {
		Map<String, Integer> totals = new HashMap<>();
		for (ReactControl control : siblings) {
			totals.merge(baseSegment(control), 1, Integer::sum);
		}
		Map<String, Integer> seen = new HashMap<>();
		List<String> result = new ArrayList<>(siblings.size());
		for (ReactControl control : siblings) {
			String base = baseSegment(control);
			if (totals.get(base) > 1) {
				int index = seen.merge(base, 1, Integer::sum) - 1;
				result.add(base + "#" + index);
			} else {
				result.add(base);
			}
		}
		return result;
	}

	/**
	 * The base address segment of a control: its role plus an optional {@code [name]} discriminator,
	 * before sibling disambiguation.
	 *
	 * @param control
	 *        The control.
	 * @return The base segment.
	 */
	public static String baseSegment(ReactControl control) {
		String role = roleOf(control);
		String name = nameOf(control);
		if (name == null || name.isEmpty()) {
			return role;
		}
		return role + "[" + sanitize(name) + "]";
	}

	/**
	 * The semantic role of a control: its {@link AgentNode#agentRole()} if it provides one, otherwise
	 * a role derived from its React module identifier.
	 *
	 * @param control
	 *        The control.
	 * @return The role, never {@code null}.
	 */
	public static String roleOf(ReactControl control) {
		if (control instanceof AgentNode node) {
			String role = node.agentRole();
			if (role != null) {
				return role;
			}
		}
		return moduleToRole(control.getReactModule());
	}

	/**
	 * The semantic name of a control: its {@link AgentNode#agentName()} if it provides one, otherwise
	 * a name derived from common label-like state keys, or {@code null} if none applies.
	 *
	 * @param control
	 *        The control.
	 * @return The name, or {@code null}.
	 */
	public static String nameOf(ReactControl control) {
		if (control instanceof AgentNode node) {
			String name = node.agentName();
			if (name != null) {
				return name;
			}
		}
		Map<String, Object> state = control.agentScalarState();
		for (String key : new String[] { "name", "label", "title", "text" }) {
			Object value = state.get(key);
			if (value instanceof String str && !str.isEmpty()) {
				return str;
			}
		}
		return null;
	}

	private static Map<String, Object> stateOf(ReactControl control) {
		if (control instanceof AgentNode node) {
			Map<String, Object> state = node.agentState();
			if (state != null) {
				return state;
			}
		}
		return control.agentScalarState();
	}

	private static List<AgentAction> actionsOf(ReactControl control) {
		if (control instanceof AgentNode node) {
			List<AgentAction> actions = node.agentActions();
			if (actions != null) {
				return actions;
			}
		}
		List<AgentAction> result = new ArrayList<>();
		for (String command : new TreeSet<>(control.commandNames())) {
			result.add(AgentAction.of(command));
		}
		return result;
	}

	/**
	 * Derives a role from a React module identifier (e.g. {@code "TLButton"} to {@code "button"},
	 * {@code "TableView"} to {@code "tableView"}).
	 */
	private static String moduleToRole(String module) {
		if (module == null || module.isEmpty()) {
			return "node";
		}
		String name = module;
		if (name.startsWith("TL") && name.length() > 2 && Character.isUpperCase(name.charAt(2))) {
			name = name.substring(2);
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * Reduces a free-form name to address-safe characters (letters, digits, {@code _} and {@code -}).
	 */
	private static String sanitize(String name) {
		StringBuilder result = new StringBuilder(name.length());
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
				result.append(c);
			} else if (c == ' ' && result.length() > 0 && result.charAt(result.length() - 1) != '_') {
				result.append('_');
			}
		}
		// Trim a trailing separator introduced by a trailing space.
		int end = result.length();
		while (end > 0 && result.charAt(end - 1) == '_') {
			end--;
		}
		return result.substring(0, end);
	}
}
