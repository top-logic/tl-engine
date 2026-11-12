/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.top_logic.common.json.gstream.JsonWriter;

/**
 * An immutable, addressable projection of one node in the control tree, as a headless consumer sees
 * it.
 *
 * <p>
 * This is the unit of observation of the headless interface: a stable {@link #address()}, a
 * semantic {@link #role()} and optional {@link #name()}, the node's data {@link #state()}, the
 * {@link #actions()} it offers, and its {@link #children()}. The address is the handle an agent
 * passes back to {@link ScriptingSession#act(String, String, Map)} to act on this node.
 * </p>
 *
 * @see ScriptingTreeProjector
 * @see ScriptingSession
 */
public final class ScriptingNodeView {

	/**
	 * Indentation of one nesting level in the JSON rendering, see {@link #toJson()}.
	 */
	private static final String INDENT = "  ";

	/** Member of a node holding its {@link #address()}. */
	public static final String ADDRESS = "address";

	/** Member of a node holding its {@link #role()}. */
	public static final String ROLE = "role";

	/** Member of a node holding its {@link #name()}. */
	public static final String NAME = "name";

	/** Member of a node holding its {@link #module()}. */
	public static final String MODULE = "module";

	/** Member of a node holding its {@link #state()}. */
	public static final String STATE = "state";

	/** Member of a node listing its {@link #actions()}. */
	public static final String ACTIONS = "actions";

	/** Member of a node listing its {@link #children()}. */
	public static final String CHILDREN = "children";

	/** Member of an action holding its {@link ScriptingAction#command()}. */
	public static final String COMMAND = "command";

	/** Member of an action holding its {@link ScriptingAction#label()}. */
	public static final String LABEL = "label";

	/**
	 * Member of an action holding the {@link #REF reference} to its {@link ScriptingAction#argsSchema()}
	 * in the {@link #SCHEMAS} of the observation.
	 */
	public static final String ARGS_SCHEMA = "argsSchema";

	/** Member of a JSON Schema reference holding the JSON pointer of the referenced schema. */
	public static final String REF = "$ref";

	/**
	 * Member of an observation holding the argument schemas its actions refer to, by
	 * {@link ArgsSchema#id()}.
	 */
	public static final String SCHEMAS = "schemas";

	private final String _address;

	private final String _role;

	private final String _name;

	private final String _module;

	private final Map<String, Object> _state;

	private final List<ScriptingAction> _actions;

	private final List<ScriptingNodeView> _children;

	/**
	 * Creates an {@link ScriptingNodeView}.
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
	public ScriptingNodeView(String address, String role, String name, String module,
			Map<String, Object> state, List<ScriptingAction> actions, List<ScriptingNodeView> children) {
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
	public List<ScriptingAction> actions() {
		return _actions;
	}

	/**
	 * The child node projections.
	 */
	public List<ScriptingNodeView> children() {
		return _children;
	}

	/**
	 * Converts this view (and its subtree) into a plain {@link Map}/{@link List} structure suitable
	 * for JSON serialization: the observation document.
	 *
	 * <p>
	 * The argument schemas the actions of the subtree refer to are listed once, in the
	 * {@link #SCHEMAS} member of the result; an action names its schema by an
	 * {@link ArgsSchema#reference() reference} into it.
	 * </p>
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> schemas = new TreeMap<>();
		Map<String, Object> result = toMap(true, schemas);
		putSchemas(result, schemas);
		return result;
	}

	/**
	 * Converts this view into a plain {@link Map}/{@link List} structure, for a node within a
	 * document that lists the argument schemas.
	 *
	 * @param withChildren
	 *        Whether to include the {@code children} subtree. The flat {@code actions} view passes
	 *        {@code false} to emit each node without its descendants.
	 * @param schemas
	 *        The schemas of the document by {@link ArgsSchema#id() id}, to which the schemas the
	 *        actions refer to are added.
	 *
	 * @see #putSchemas(Map, Map)
	 */
	public Map<String, Object> toMap(boolean withChildren, Map<String, Object> schemas) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(ADDRESS, _address);
		result.put(ROLE, _role);
		if (_name != null) {
			result.put(NAME, _name);
		}
		if (_module != null) {
			result.put(MODULE, _module);
		}
		if (_state != null && !_state.isEmpty()) {
			result.put(STATE, _state);
		}
		if (_actions != null && !_actions.isEmpty()) {
			List<Object> actions = new ArrayList<>();
			for (ScriptingAction action : _actions) {
				actions.add(actionToMap(action, schemas));
			}
			result.put(ACTIONS, actions);
		}
		if (withChildren && _children != null && !_children.isEmpty()) {
			List<Object> children = new ArrayList<>();
			for (ScriptingNodeView child : _children) {
				children.add(child.toMap(true, schemas));
			}
			result.put(CHILDREN, children);
		}
		return result;
	}

	/**
	 * Adds the collected schemas as {@link #SCHEMAS} member to an observation document.
	 *
	 * @param document
	 *        The observation document.
	 * @param schemas
	 *        The schemas collected by {@link #toMap(boolean, Map)} for the nodes of the document.
	 */
	public static void putSchemas(Map<String, Object> document, Map<String, Object> schemas) {
		if (!schemas.isEmpty()) {
			document.put(SCHEMAS, schemas);
		}
	}

	/**
	 * Collects this node and its descendants that expose at least one action, as a flat list (in
	 * document order). This is the basis of the affordance-first {@code actions} view: the nodes an
	 * agent can act on, without the surrounding container hierarchy.
	 */
	public List<ScriptingNodeView> actionableNodes() {
		List<ScriptingNodeView> result = new ArrayList<>();
		collectActionable(result);
		return result;
	}

	private void collectActionable(List<ScriptingNodeView> out) {
		if (_actions != null && !_actions.isEmpty()) {
			out.add(this);
		}
		if (_children != null) {
			for (ScriptingNodeView child : _children) {
				child.collectActionable(out);
			}
		}
	}

	private static Map<String, Object> actionToMap(ScriptingAction action, Map<String, Object> schemas) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(COMMAND, action.command());
		result.put(LABEL, action.label());
		ArgsSchema argsSchema = action.argsSchema();
		if (argsSchema != null) {
			schemas.putIfAbsent(argsSchema.id(), argsSchema.schema());
			result.put(ARGS_SCHEMA, Map.of(REF, argsSchema.reference()));
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
	 *
	 * @see #writeJson(Appendable)
	 */
	public String toJson() {
		return toJson(toMap());
	}

	/**
	 * Writes this view (and its subtree) as indented JSON to the given output, in the format of
	 * {@link #toJson()}.
	 *
	 * @param out
	 *        The output to write to, e.g. the writer of an HTTP response.
	 */
	public void writeJson(Appendable out) throws IOException {
		writeJson(out, toMap());
	}

	/**
	 * Renders an arbitrary {@link Map}/{@link Collection}/scalar structure as indented JSON, using
	 * the same formatting as {@link #toJson()}. Used to serialize the flat {@code actions} view.
	 *
	 * @param value
	 *        The value to render.
	 * @return The indented JSON.
	 *
	 * @see #writeJson(Appendable, Object)
	 */
	public static String toJson(Object value) {
		StringBuilder out = new StringBuilder();
		try {
			writeJson(out, value);
		} catch (IOException ex) {
			throw new IllegalStateException("Writing to a string buffer failed.", ex);
		}
		return out.toString();
	}

	/**
	 * Writes an arbitrary {@link Map}/{@link Collection}/scalar structure as indented JSON to the
	 * given output, in the format of {@link #toJson(Object)}.
	 *
	 * @param out
	 *        The output to write to.
	 * @param value
	 *        The value to write.
	 */
	public static void writeJson(Appendable out, Object value) throws IOException {
		writeJson(out, value, true);
	}

	/**
	 * Writes an arbitrary {@link Map}/{@link Collection}/scalar structure as JSON to the given
	 * output.
	 *
	 * @param out
	 *        The output to write to.
	 * @param value
	 *        The value to write.
	 * @param indent
	 *        Whether to write the indented format of {@link #toJson(Object)}, or compact JSON
	 *        without any whitespace.
	 */
	public static void writeJson(Appendable out, Object value, boolean indent) throws IOException {
		writeValue(jsonWriter(out, indent), value);
	}

	/**
	 * A {@link JsonWriter} producing the indented format of {@link #toJson(Object)}, for writing a
	 * document that embeds observations with {@link #writeValue(JsonWriter, Object)}.
	 *
	 * @param out
	 *        The output to write to.
	 */
	public static JsonWriter jsonWriter(Appendable out) {
		return jsonWriter(out, true);
	}

	/**
	 * A {@link JsonWriter} for writing a document that embeds observations with
	 * {@link #writeValue(JsonWriter, Object)}.
	 *
	 * @param out
	 *        The output to write to.
	 * @param indent
	 *        Whether to write the indented format of {@link #toJson(Object)}, or compact JSON.
	 */
	public static JsonWriter jsonWriter(Appendable out, boolean indent) {
		JsonWriter json = new JsonWriter(out);
		if (indent) {
			json.setIndent(INDENT);
		}
		// A non-finite number in a node's state is written as it is rather than failing the
		// observation.
		json.setLenient(true);
		return json;
	}

	/**
	 * Writes an arbitrary {@link Map}/{@link Collection}/scalar structure as JSON value.
	 *
	 * <p>
	 * A value of another type is written as the string of its {@link Object#toString()}.
	 * </p>
	 *
	 * @param json
	 *        The writer to write to.
	 * @param value
	 *        The value to write.
	 */
	public static void writeValue(JsonWriter json, Object value) throws IOException {
		if (value == null) {
			json.nullValue();
		} else if (value instanceof String str) {
			json.value(str);
		} else if (value instanceof Boolean bool) {
			json.value(bool.booleanValue());
		} else if (value instanceof Number number) {
			json.value(number);
		} else if (value instanceof Map<?, ?> map) {
			json.beginObject();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				json.name(String.valueOf(entry.getKey()));
				writeValue(json, entry.getValue());
			}
			json.endObject();
		} else if (value instanceof Collection<?> collection) {
			json.beginArray();
			for (Object element : collection) {
				writeValue(json, element);
			}
			json.endArray();
		} else {
			json.value(String.valueOf(value));
		}
	}

	@Override
	public String toString() {
		return toJson();
	}
}
