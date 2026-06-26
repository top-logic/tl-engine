/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.schema.JsonSchemaWriter;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;

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

	/**
	 * State keys consulted, in order, to derive a node name from presentation state.
	 */
	private static final String[] LABEL_KEYS = { "name", "label", "title", "text" };

	/**
	 * Strategy for deriving a node name from a control's bound model (e.g. a table row's business
	 * object). Defaults to the application {@link MetaLabelProvider}; hosts or tests may override it
	 * via {@link #setModelNaming(Function)}.
	 */
	private static volatile Function<Object, String> MODEL_NAMING = AgentTreeProjector::defaultModelLabel;

	private AgentTreeProjector() {
		// Static utility.
	}

	/**
	 * Overrides the strategy used to derive a node name from a control's bound model.
	 *
	 * <p>
	 * The default resolves the model's application label via {@link MetaLabelProvider}; this hook lets
	 * an embedding (or a test running without application services) supply its own mapping. A strategy
	 * may return {@code null} to decline naming a given model.
	 * </p>
	 *
	 * @param naming
	 *        The new strategy, never {@code null}.
	 */
	public static void setModelNaming(Function<Object, String> naming) {
		MODEL_NAMING = naming;
	}

	/**
	 * Restores the default model-naming strategy ({@link MetaLabelProvider}).
	 */
	public static void resetModelNaming() {
		MODEL_NAMING = AgentTreeProjector::defaultModelLabel;
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
		List<ReactControl> kids = visibleChildren(control);
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
	 * The semantic children of a control: its direct children with
	 * {@link ReactControl#agentTransparent() structural} ones elided and their own visible children
	 * lifted in their place.
	 *
	 * <p>
	 * This is the single place pruning happens. It is fully generic — it asks each child whether it
	 * is transparent and never inspects concrete control types — so the projection and resolution
	 * stay free of any type cascade.
	 * </p>
	 *
	 * @param control
	 *        The control whose semantic children to compute.
	 * @return The visible (non-transparent) descendants that act as this control's children.
	 */
	public static List<ReactControl> visibleChildren(ReactControl control) {
		List<ReactControl> result = new ArrayList<>();
		for (ReactControl child : control.agentChildren()) {
			if (child.agentTransparent()) {
				result.addAll(visibleChildren(child));
			} else {
				result.add(child);
			}
		}
		return result;
	}

	/**
	 * Expands a control to the visible nodes that should represent it at the top level: the control
	 * itself, or — if it is {@link ReactControl#agentTransparent() structural} — its visible
	 * children.
	 *
	 * @param control
	 *        The candidate root control.
	 * @return The visible top-level node(s).
	 */
	public static List<ReactControl> visibleRoots(ReactControl control) {
		return control.agentTransparent() ? visibleChildren(control) : List.of(control);
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
		String role = control.agentRole();
		if (role != null) {
			return role;
		}
		return moduleToRole(control.getReactModule());
	}

	/**
	 * The semantic name of a control. Resolved in order of decreasing specificity: the control's
	 * explicit {@link AgentNode#agentName()}, then a common label-like state key, then the application
	 * label of its bound {@link ReactControl#getModel() model} (so a control showing a business
	 * object — a table row, a list item, a selection — is addressed by that object's label). Returns
	 * {@code null} if none applies.
	 *
	 * @param control
	 *        The control.
	 * @return The name, or {@code null}.
	 */
	public static String nameOf(ReactControl control) {
		String explicit = control.agentName();
		if (explicit != null) {
			return explicit;
		}
		Map<String, Object> state = control.agentScalarState();
		for (String key : LABEL_KEYS) {
			Object value = state.get(key);
			if (value instanceof String str && !str.isEmpty()) {
				return str;
			}
		}
		String modelName = MODEL_NAMING.apply(control.getModel());
		if (modelName != null && !modelName.isEmpty() && !isDefaultToString(modelName)) {
			return modelName;
		}
		return null;
	}

	/**
	 * Default model-naming strategy: the model's application label, or {@code null} if it has none or
	 * cannot be resolved (e.g. no running application services). A default {@link Object#toString()}
	 * label is filtered out centrally by {@link #nameOf(ReactControl)}.
	 */
	private static String defaultModelLabel(Object model) {
		if (model == null) {
			return null;
		}
		try {
			return MetaLabelProvider.INSTANCE.getLabel(model);
		} catch (Throwable ex) {
			// No label service available (e.g. headless unit test) or model not labelable.
			return null;
		}
	}

	/**
	 * Whether a label is a default {@link Object#toString()} of the form {@code Something@1a2b3c} —
	 * a class identity plus an identity hashcode. Such a value is unstable (the hashcode varies per
	 * run) and meaningless as an address, so it is rejected as a name.
	 *
	 * <p>
	 * The check requires the suffix after the last {@code @} to be a non-empty run of hex digits, so
	 * genuine labels containing {@code @} (e.g. an email address {@code user@example.com}) are kept.
	 * </p>
	 */
	private static boolean isDefaultToString(String label) {
		int at = label.lastIndexOf('@');
		if (at <= 0 || at == label.length() - 1) {
			return false;
		}
		for (int i = at + 1; i < label.length(); i++) {
			if (Character.digit(label.charAt(i), 16) < 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The semantic state a node exposes to the agent interface — the same map {@link #project} places
	 * on the node, without recursing into children. Used to capture and verify assertion steps.
	 *
	 * @param control
	 *        The control whose state to read.
	 * @return The node's semantic state map.
	 */
	public static Map<String, Object> nodeState(ReactControl control) {
		return stateOf(control);
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
		for (String command : new TreeSet<>(control.agentCommands())) {
			Object argsSchema = argsSchemaOf(control, command);
			List<AgentParam> params = new ArrayList<>();
			if (argsSchema == null) {
				for (ReactParam param : control.agentCommandParams(command)) {
					params.add(new AgentParam(param.name(), param.type(), param.required(), param.description()));
				}
			}
			result.add(new AgentAction(command, command, params, argsSchema));
		}
		return result;
	}

	/**
	 * The JSON Schema of the command's typed argument interface, as a parsed JSON value, or
	 * {@code null} if the command takes a raw {@code Map} (or no arguments).
	 */
	private static Object argsSchemaOf(ReactControl control, String command) {
		ConfigurationDescriptor argType = control.agentCommandArgsType(command);
		if (argType == null) {
			return null;
		}
		try {
			Schema schema = new JsonConfigSchemaBuilder().buildConfigSchema(argType);
			return JSON.read(new StringReader(JsonSchemaWriter.toJson(schema)));
		} catch (Exception ex) {
			Logger.error("Failed to project argument schema for command '" + command + "' on "
				+ control.getClass().getName(), ex, AgentTreeProjector.class);
			return null;
		}
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
