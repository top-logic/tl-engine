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

/**
 * An immutable, addressable projection of one node in the control tree, as a headless consumer sees
 * it.
 *
 * <p>
 * This is the unit of observation of the headless agent interface: a stable {@link #address()}, a
 * semantic {@link #role()} and optional {@link #name()}, the node's data {@link #state()}, the
 * {@link #actions()} it offers, and its {@link #children()}. The address is the handle an agent
 * passes back to {@link AgentSession#act(String, String, Map)} to act on this node.
 * </p>
 *
 * @see AgentTreeProjector
 * @see AgentSession
 */
public final class AgentNodeView {

	private final String _address;

	private final String _role;

	private final String _name;

	private final String _module;

	private final Map<String, Object> _state;

	private final List<AgentAction> _actions;

	private final List<AgentNodeView> _children;

	/**
	 * Creates an {@link AgentNodeView}.
	 *
	 * @param address
	 *        The stable semantic address of this node (see {@link #address()}).
	 * @param role
	 *        The semantic role (see {@link #role()}).
	 * @param name
	 *        The semantic name, or {@code null} (see {@link #name()}).
	 * @param module
	 *        The underlying React module identifier, or {@code null}.
	 * @param state
	 *        The node's data state.
	 * @param actions
	 *        The actions the node offers.
	 * @param children
	 *        The child node projections.
	 */
	public AgentNodeView(String address, String role, String name, String module,
			Map<String, Object> state, List<AgentAction> actions, List<AgentNodeView> children) {
		_address = address;
		_role = role;
		_name = name;
		_module = module;
		_state = state;
		_actions = actions;
		_children = children;
	}

	/**
	 * The stable, path-structured address of this node (e.g. {@code "/form/button[Submit]"}).
	 */
	public String address() {
		return _address;
	}

	/**
	 * The semantic role of this node (e.g. {@code "button"}, {@code "field"}).
	 */
	public String role() {
		return _role;
	}

	/**
	 * The semantic name of this node, or {@code null} if it has none.
	 */
	public String name() {
		return _name;
	}

	/**
	 * The underlying React module identifier, or {@code null}.
	 */
	public String module() {
		return _module;
	}

	/**
	 * The node's data state.
	 */
	public Map<String, Object> state() {
		return _state;
	}

	/**
	 * The actions this node offers.
	 */
	public List<AgentAction> actions() {
		return _actions;
	}

	/**
	 * The child node projections.
	 */
	public List<AgentNodeView> children() {
		return _children;
	}

	/**
	 * Converts this view (and its subtree) into a plain {@link Map}/{@link List} structure suitable
	 * for JSON serialization.
	 */
	public Map<String, Object> toMap() {
		return toMap(true);
	}

	/**
	 * Converts this view into a plain {@link Map}/{@link List} structure.
	 *
	 * @param withChildren
	 *        Whether to include the {@code children} subtree. The flat {@code actions} view passes
	 *        {@code false} to emit each node without its descendants.
	 */
	public Map<String, Object> toMap(boolean withChildren) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("address", _address);
		result.put("role", _role);
		if (_name != null) {
			result.put("name", _name);
		}
		if (_module != null) {
			result.put("module", _module);
		}
		if (_state != null && !_state.isEmpty()) {
			result.put("state", _state);
		}
		if (_actions != null && !_actions.isEmpty()) {
			List<Object> actions = new ArrayList<>();
			for (AgentAction action : _actions) {
				actions.add(actionToMap(action));
			}
			result.put("actions", actions);
		}
		if (withChildren && _children != null && !_children.isEmpty()) {
			List<Object> children = new ArrayList<>();
			for (AgentNodeView child : _children) {
				children.add(child.toMap());
			}
			result.put("children", children);
		}
		return result;
	}

	/**
	 * Collects this node and its descendants that expose at least one action, as a flat list (in
	 * document order). This is the basis of the affordance-first {@code actions} view: the nodes an
	 * agent can act on, without the surrounding container hierarchy.
	 */
	public List<AgentNodeView> actionableNodes() {
		List<AgentNodeView> result = new ArrayList<>();
		collectActionable(result);
		return result;
	}

	private void collectActionable(List<AgentNodeView> out) {
		if (_actions != null && !_actions.isEmpty()) {
			out.add(this);
		}
		if (_children != null) {
			for (AgentNodeView child : _children) {
				child.collectActionable(out);
			}
		}
	}

	private static Map<String, Object> actionToMap(AgentAction action) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("command", action.command());
		result.put("label", action.label());
		if (!action.params().isEmpty()) {
			List<Object> params = new ArrayList<>();
			for (AgentParam param : action.params()) {
				Map<String, Object> p = new LinkedHashMap<>();
				p.put("name", param.name());
				p.put("type", param.type());
				p.put("required", param.required());
				p.put("description", param.description());
				params.add(p);
			}
			result.put("params", params);
		}
		if (action.argsSchema() != null) {
			result.put("argsSchema", action.argsSchema());
		}
		return result;
	}

	/**
	 * Renders this view (and its subtree) as indented JSON.
	 *
	 * <p>
	 * This is the agent-facing serialization of an observation; it intentionally produces
	 * human-readable output for inspection and recording.
	 * </p>
	 */
	public String toJson() {
		return toJson(toMap());
	}

	/**
	 * Renders an arbitrary {@link Map}/{@link List}/scalar structure as indented JSON, using the same
	 * formatting as {@link #toJson()}. Used to serialize the flat {@code actions} view.
	 *
	 * @param value
	 *        The value to render.
	 * @return The indented JSON.
	 */
	public static String toJson(Object value) {
		StringBuilder out = new StringBuilder();
		writeJson(out, value, 0);
		return out.toString();
	}

	@Override
	public String toString() {
		return toJson();
	}

	@SuppressWarnings("unchecked")
	private static void writeJson(StringBuilder out, Object value, int indent) {
		if (value == null) {
			out.append("null");
		} else if (value instanceof String str) {
			writeJsonString(out, str);
		} else if (value instanceof Boolean || value instanceof Number) {
			out.append(value);
		} else if (value instanceof Map<?, ?> map) {
			if (map.isEmpty()) {
				out.append("{}");
				return;
			}
			out.append("{\n");
			int i = 0;
			for (Map.Entry<String, Object> entry : ((Map<String, Object>) map).entrySet()) {
				indent(out, indent + 1);
				writeJsonString(out, entry.getKey());
				out.append(": ");
				writeJson(out, entry.getValue(), indent + 1);
				if (++i < map.size()) {
					out.append(',');
				}
				out.append('\n');
			}
			indent(out, indent);
			out.append('}');
		} else if (value instanceof List<?> list) {
			if (list.isEmpty()) {
				out.append("[]");
				return;
			}
			out.append("[\n");
			for (int i = 0; i < list.size(); i++) {
				indent(out, indent + 1);
				writeJson(out, list.get(i), indent + 1);
				if (i + 1 < list.size()) {
					out.append(',');
				}
				out.append('\n');
			}
			indent(out, indent);
			out.append(']');
		} else {
			writeJsonString(out, String.valueOf(value));
		}
	}

	private static void writeJsonString(StringBuilder out, String str) {
		out.append('"');
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			switch (c) {
				case '"':
					out.append("\\\"");
					break;
				case '\\':
					out.append("\\\\");
					break;
				case '\n':
					out.append("\\n");
					break;
				case '\r':
					out.append("\\r");
					break;
				case '\t':
					out.append("\\t");
					break;
				default:
					out.append(c);
			}
		}
		out.append('"');
	}

	private static void indent(StringBuilder out, int level) {
		for (int i = 0; i < level; i++) {
			out.append("  ");
		}
	}
}
