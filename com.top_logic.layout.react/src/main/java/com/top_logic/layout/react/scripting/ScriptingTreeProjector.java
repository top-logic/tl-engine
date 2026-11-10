/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.top_logic.layout.react.control.ScriptingControl;
import com.top_logic.layout.react.control.ScriptingItem;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.identity.ObjectIdentities;
import com.top_logic.layout.react.control.identity.ProjectionMode;

/**
 * Projects a {@link ReactControl} subtree into an addressable {@link ScriptingNodeView} tree, assigning
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
 * <b>Navigation slots.</b> The content of a selection-driven container is addressed by the
 * {@link com.top_logic.layout.react.control.ScriptingControl#scriptingChildSlot(ReactControl) slot} of the
 * selected entry instead of its own role and name, e.g.
 * {@code /appShell/sidebar/item[attributes]/tabBar/tab[overview]/…}. The address thus encodes the
 * navigation context it belongs to with stable configuration identifiers: it stays valid while that
 * context is active and fails loudly — rather than silently matching a look-alike control — when
 * another sidebar item or tab is selected. A slotted child is never elided as
 * {@link ReactControl#scriptingTransparent() transparent}, so the slot segment is always present in the
 * addresses of the content subtree.
 * </p>
 *
 * <p>
 * The same segment algorithm is used for resolution by {@link ScriptingSession}, so addresses round-trip
 * exactly.
 * </p>
 */
public final class ScriptingTreeProjector {

	/**
	 * Address segment separator.
	 */
	public static final String SEPARATOR = "/";

	/**
	 * State keys consulted, in order, to derive a node name from presentation state.
	 */
	private static final String[] LABEL_KEYS = { "name", "label", "title", "text" };

	/**
	 * State entry of an item node holding the id its container displays the item under, see
	 * {@link ScriptingModelKey#putIdentity(Map, ProjectionMode, ObjectIdentities, Object, Object, String, Object)}.
	 */
	public static final String ITEM_ID = "id";

	/**
	 * Strategy for deriving a node name from a control's bound model (e.g. a table row's business
	 * object). Defaults to the application {@link MetaLabelProvider}; hosts or tests may override it
	 * via {@link #setModelNaming(Function)}.
	 */
	private static volatile Function<Object, String> MODEL_NAMING = ScriptingTreeProjector::defaultModelLabel;

	private ScriptingTreeProjector() {
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
		MODEL_NAMING = ScriptingTreeProjector::defaultModelLabel;
	}

	/**
	 * Projects a control and its subtree, with the control placed at the given address.
	 *
	 * <p>
	 * The addresses depend on the mode only in the segments of {@link ScriptingItem items}: an item
	 * is addressed as the mode identifies objects, see {@link #segments(Node, List, ProjectionMode)}.
	 * </p>
	 *
	 * @param control
	 *        The root control of the subtree to project.
	 * @param address
	 *        The address at which {@code control} sits.
	 * @param mode
	 *        How the nodes identify the objects they display: {@link ProjectionMode#AGENT} for an
	 *        observation an agent acts on, {@link ProjectionMode#KEY} for the state an assertion
	 *        records.
	 * @return The projected node view.
	 */
	public static ScriptingNodeView project(ReactControl control, String address, ProjectionMode mode) {
		return project(Node.of(control), address, mode);
	}

	/**
	 * Projects a node and its subtree, with the node placed at the given address.
	 *
	 * @see #project(ReactControl, String, ProjectionMode)
	 */
	public static ScriptingNodeView project(Node node, String address, ProjectionMode mode) {
		List<Node> kids = children(node);
		List<String> segments = segments(node, kids, mode);

		List<ScriptingNodeView> childViews = new ArrayList<>(kids.size());
		for (int i = 0; i < kids.size(); i++) {
			String childAddress = join(address, segments.get(i));
			childViews.add(project(kids.get(i), childAddress, mode));
		}

		if (node.isItem()) {
			ScriptingItem item = node.item();
			Map<String, Object> state = new LinkedHashMap<>();
			ScriptingModelKey.putIdentity(state, mode, node.control().objectIdentities(), null, item.object(),
				ITEM_ID, item.id());
			return new ScriptingNodeView(address, item.role(), modelName(item.object()), null, state, List.of(),
				childViews);
		}
		ReactControl control = node.control();
		return new ScriptingNodeView(address, roleOf(control), nameOf(control), control.getReactModule(),
			stateOf(control, mode), actionsOf(control), childViews);
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
	 * A node of the projection: a control, or an {@link ScriptingItem item} of a container control,
	 * grouping the controls the container displays for the item.
	 */
	public static final class Node {

		private final ReactControl _control;

		private final ScriptingItem _item;

		private final List<Node> _members;

		private Node(ReactControl control, ScriptingItem item, List<Node> members) {
			_control = control;
			_item = item;
			_members = members;
		}

		/**
		 * The node of the given control.
		 */
		public static Node of(ReactControl control) {
			return new Node(control, null, List.of());
		}

		/**
		 * Whether this node is an {@link #item()} rather than a control.
		 */
		public boolean isItem() {
			return _item != null;
		}

		/**
		 * The control of a control node, the container displaying the item of an
		 * {@link #isItem() item node}.
		 */
		public ReactControl control() {
			return _control;
		}

		/**
		 * The item of an {@link #isItem() item node}, {@code null} for a control node.
		 */
		public ScriptingItem item() {
			return _item;
		}

		@Override
		public String toString() {
			return isItem() ? _item.role() + "[" + _item.id() + "]" : String.valueOf(_control);
		}
	}

	/**
	 * The nodes below the given node, as the user sees them.
	 *
	 * <p>
	 * This is the single place pruning happens. It is fully generic - it asks each control and never
	 * inspects concrete control types -, so the projection and the resolution of addresses stay free
	 * of any type cascade:
	 * </p>
	 * <ul>
	 * <li>A control the user does not see ({@link ReactControl#scriptingVisible()}) is omitted with
	 * its subtree, so is a child its container does not show ({@link ReactControl#scriptingChildren()}).</li>
	 * <li>A {@link ReactControl#scriptingTransparent() structural} control is elided, its own
	 * children are lifted in its place.</li>
	 * <li>The controls a container displays for an {@link ReactControl#scriptingItems() item} are
	 * placed under a node of the item, those the user cannot operate (see {@link #isOperable(Node)})
	 * are omitted, and so is an item without any operable control.</li>
	 * </ul>
	 *
	 * @param node
	 *        The node whose children to compute.
	 * @return The nodes that act as the children of the given node.
	 */
	public static List<Node> children(Node node) {
		if (node.isItem()) {
			return node._members;
		}
		List<Node> result = new ArrayList<>();
		addChildren(result, node.control());
		return result;
	}

	private static void addChildren(List<Node> result, ReactControl control) {
		for (ReactControl child : control.scriptingChildren()) {
			addNode(result, control, child);
		}
		for (ScriptingItem item : control.scriptingItems()) {
			List<Node> members = new ArrayList<>();
			for (ReactControl member : item.controls()) {
				List<Node> candidates = new ArrayList<>();
				addNode(candidates, control, member);
				for (Node candidate : candidates) {
					if (isOperable(candidate)) {
						members.add(candidate);
					}
				}
			}
			if (!members.isEmpty()) {
				result.add(new Node(control, item, List.copyOf(members)));
			}
		}
	}

	private static void addNode(List<Node> result, ReactControl parent, ReactControl child) {
		if (!child.scriptingVisible()) {
			return;
		}
		if (child.scriptingTransparent() && parent.scriptingChildSlot(child) == null) {
			addChildren(result, child);
		} else {
			result.add(Node.of(child));
		}
	}

	/**
	 * Whether the user can operate the given node: it offers an action, or one of its descendants
	 * does.
	 */
	public static boolean isOperable(Node node) {
		if (!node.isItem() && !actionsOf(node.control()).isEmpty()) {
			return true;
		}
		for (Node child : children(node)) {
			if (isOperable(child)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The nodes that represent the given control at the top level: the control itself, or - if it is
	 * {@link ReactControl#scriptingTransparent() structural} - its children.
	 *
	 * @param control
	 *        The candidate root control.
	 * @return The top-level node(s).
	 */
	public static List<Node> roots(ReactControl control) {
		if (!control.scriptingVisible()) {
			return List.of();
		}
		return control.scriptingTransparent() ? children(Node.of(control)) : List.of(Node.of(control));
	}

	/**
	 * Computes the disambiguated address segments for a list of sibling nodes, in order.
	 *
	 * <p>
	 * Siblings sharing a base segment receive an {@code #k} occurrence suffix; unique base segments
	 * are left bare. The result is positionally aligned with {@code siblings}.
	 * </p>
	 *
	 * <p>
	 * An item is addressed as {@code role[id]} by the id its container displays it under, in the
	 * {@link ProjectionMode#KEY key mode} by the {@link #keyArgument(String) encoded} business key
	 * of its object instead, so that a recorded address designates the same item in every session.
	 * An item whose object has no key is addressed by its id in both modes.
	 * </p>
	 *
	 * @param parent
	 *        The node whose children the siblings are, consulted for
	 *        {@link com.top_logic.layout.react.control.ScriptingControl#scriptingChildSlot(ReactControl)
	 *        navigation slots}; {@code null} for top-level nodes.
	 * @param siblings
	 *        The sibling nodes.
	 * @param mode
	 *        How the items are addressed.
	 * @return The segment for each sibling, in the same order.
	 */
	public static List<String> segments(Node parent, List<Node> siblings, ProjectionMode mode) {
		ReactControl parentControl = parent == null ? null : parent.control();
		List<String> bases = new ArrayList<>(siblings.size());
		Map<String, Integer> totals = new HashMap<>();
		for (Node node : siblings) {
			String base = node.isItem() ? itemSegment(node, mode) : baseSegment(parentControl, node.control());
			bases.add(base);
			totals.merge(base, 1, Integer::sum);
		}
		Map<String, Integer> seen = new HashMap<>();
		List<String> result = new ArrayList<>(siblings.size());
		for (String base : bases) {
			if (totals.get(base) > 1) {
				int index = seen.merge(base, 1, Integer::sum) - 1;
				result.add(base + "#" + index);
			} else {
				result.add(base);
			}
		}
		return result;
	}

	private static String itemSegment(Node node, ProjectionMode mode) {
		ScriptingItem item = node.item();
		String argument = item.id();
		if (mode == ProjectionMode.KEY && item.object() != null) {
			ObjectIdentities identities = node.control().objectIdentities();
			String key = ScriptingModelKey.toJson(identities == null ? null : identities.keyContext(), item.object());
			if (key != null) {
				argument = keyArgument(key);
			}
		}
		return ScriptingControl.slotSegment(item.role(), argument);
	}

	/**
	 * The business key of an item in a form fit for an address segment: the characters that
	 * structure an address ({@value #SEPARATOR}, {@code [}, {@code ]}, {@code #}) and the escape
	 * character {@code %} are percent-encoded.
	 *
	 * @param key
	 *        The key JSON, see {@link ScriptingModelKey#toJson(Object, Object)}.
	 */
	public static String keyArgument(String key) {
		StringBuilder result = new StringBuilder(key.length());
		for (int i = 0; i < key.length(); i++) {
			char c = key.charAt(i);
			if (c == '%' || c == '/' || c == '[' || c == ']' || c == '#') {
				result.append('%').append(Character.toUpperCase(Character.forDigit(c >> 4, 16)))
					.append(Character.toUpperCase(Character.forDigit(c & 0xF, 16)));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * The base address segment of a control before sibling disambiguation: the
	 * {@link com.top_logic.layout.react.control.ScriptingControl#scriptingChildSlot(ReactControl) navigation
	 * slot} its parent assigns it, or its role plus an optional {@code [name]} discriminator.
	 *
	 * @param parent
	 *        The control's parent, or {@code null} for a top-level control.
	 * @param control
	 *        The control.
	 * @return The base segment.
	 */
	public static String baseSegment(ReactControl parent, ReactControl control) {
		if (parent != null) {
			String slot = parent.scriptingChildSlot(control);
			if (slot != null) {
				return slot;
			}
		}
		String role = roleOf(control);
		String name = nameOf(control);
		if (name == null || name.isEmpty()) {
			return role;
		}
		return role + "[" + sanitize(name) + "]";
	}

	/**
	 * The semantic role of a control: its {@link ReactControl#scriptingRole()} if it provides one, otherwise
	 * a role derived from its React module identifier.
	 *
	 * @param control
	 *        The control.
	 * @return The role, never {@code null}.
	 */
	public static String roleOf(ReactControl control) {
		String role = control.scriptingRole();
		if (role != null) {
			return role;
		}
		return moduleToRole(control.getReactModule());
	}

	/**
	 * The semantic name of a control. Resolved in order of decreasing specificity: the control's
	 * explicit {@link ReactControl#scriptingName()}, then a common label-like state key, then the application
	 * label of its bound {@link ReactControl#getModel() model} (so a control showing a business
	 * object — a table row, a list item, a selection — is addressed by that object's label). Returns
	 * {@code null} if none applies.
	 *
	 * @param control
	 *        The control.
	 * @return The name, or {@code null}.
	 */
	public static String nameOf(ReactControl control) {
		String explicit = control.scriptingName();
		if (explicit != null) {
			return explicit;
		}
		Map<String, Object> state = control.scriptingScalarState();
		for (String key : LABEL_KEYS) {
			Object value = state.get(key);
			if (value instanceof String str && !str.isEmpty()) {
				return str;
			}
		}
		return modelName(control.getModel());
	}

	/**
	 * The name of a node showing the given model: its application label, {@code null} if it has
	 * none.
	 */
	private static String modelName(Object model) {
		if (model == null) {
			return null;
		}
		String modelName = MODEL_NAMING.apply(model);
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
	 * The state of a node in the {@link ProjectionMode#KEY key form} an assertion step is captured
	 * and verified in.
	 *
	 * @param control
	 *        The control whose state to read.
	 * @return The node's semantic state map.
	 *
	 * @see #nodeState(ReactControl, ProjectionMode)
	 */
	public static Map<String, Object> nodeState(ReactControl control) {
		return nodeState(control, ProjectionMode.KEY);
	}

	/**
	 * The semantic state a node exposes to the headless interface — the same map
	 * {@link #project(ReactControl, String, ProjectionMode)} places on the node, without recursing
	 * into children.
	 *
	 * @param control
	 *        The control whose state to read.
	 * @param mode
	 *        How the node identifies the objects it displays.
	 * @return The node's semantic state map.
	 */
	public static Map<String, Object> nodeState(ReactControl control, ProjectionMode mode) {
		return stateOf(control, mode);
	}

	private static Map<String, Object> stateOf(ReactControl control, ProjectionMode mode) {
		if (control instanceof ScriptingNode node) {
			Map<String, Object> state = node.scriptingState(mode);
			if (state != null) {
				return state;
			}
		}
		return control.scriptingScalarState(mode);
	}

	/**
	 * The actions a control offers: its {@link ReactControl#scriptingCommands() commands} that are
	 * {@link ReactControl#scriptingCommandState(String) executable} in its current state.
	 */
	private static List<ScriptingAction> actionsOf(ReactControl control) {
		if (control instanceof ScriptingNode node) {
			List<ScriptingAction> actions = node.scriptingActions();
			if (actions != null) {
				List<ScriptingAction> executable = new ArrayList<>(actions.size());
				for (ScriptingAction action : actions) {
					if (control.scriptingCommandState(action.command()).isExecutable()) {
						executable.add(action);
					}
				}
				return executable;
			}
		}
		List<ScriptingAction> result = new ArrayList<>();
		for (String command : new TreeSet<>(control.scriptingCommands())) {
			if (!control.scriptingCommandState(command).isExecutable()) {
				continue;
			}
			ArgsSchema argsSchema = argsSchemaOf(control, command);
			if (argsSchema == null) {
				argsSchema = paramsSchemaOf(control, command);
			}
			result.add(new ScriptingAction(command, command, argsSchema));
		}
		return result;
	}

	/**
	 * The JSON Schema of the command's typed argument interface, or {@code null} if the command
	 * takes a raw {@code Map} (or no arguments).
	 *
	 * <p>
	 * The {@link com.top_logic.layout.react.control.ReactCommand} base properties (address, name and
	 * the derived target) are the recorded step's envelope, never sent by a client in the
	 * {@code arguments} — they are stripped so every command's advertised schema shows only its own
	 * arguments. The schema is then {@link #simplify(Map, String) simplified} to the plain object
	 * schema of the argument interface.
	 * </p>
	 *
	 * <p>
	 * Schemas are cached per argument descriptor, with an {@link ArgsSchema#id() id} that is
	 * unique among the argument interfaces. An argument interface whose schema cannot be built,
	 * e.g. one with an item-typed property whose polymorphic value space the schema builder cannot
	 * enumerate, falls back to a minimal object schema naming the argument properties. Objects are
	 * passed as the ids the projection shows, plain strings, so no argument interface of the
	 * displayed controls needs such a property.
	 * </p>
	 */
	private static ArgsSchema argsSchemaOf(ReactControl control, String command) {
		ConfigurationDescriptor argType = control.scriptingCommandArgsType(command);
		if (argType == null) {
			return null;
		}
		return SCHEMA_CACHE.computeIfAbsent(argType, descriptor -> {
			String id = schemaId(descriptor.getConfigurationInterface().getSimpleName(), descriptor);
			return new ArgsSchema(id, relocateReferences(buildArgsSchema(control, command, descriptor), id));
		});
	}

	/**
	 * The schema of the hand-declared {@code @ReactParam} parameters of a command taking a raw
	 * {@code Map}, or {@code null} if it declares none.
	 *
	 * <p>
	 * The schema is cached per control class and command, with the id
	 * <code>&lt;control class&gt;.&lt;command&gt;</code>.
	 * </p>
	 *
	 * @see ArgsSchema#forParams(String, List)
	 */
	private static ArgsSchema paramsSchemaOf(ReactControl control, String command) {
		ReactParam[] declared = control.scriptingCommandParams(command);
		if (declared == null || declared.length == 0) {
			return null;
		}
		ParamsKey key = new ParamsKey(control.getClass(), command);
		return PARAMS_SCHEMA_CACHE.computeIfAbsent(key, k -> {
			List<ScriptingParam> params = new ArrayList<>();
			for (ReactParam param : declared) {
				params.add(new ScriptingParam(param.name(), param.type(), param.required(), param.description()));
			}
			return ArgsSchema.forParams(schemaId(simpleName(k.type()) + "." + command, k), params);
		});
	}

	private static String simpleName(Class<?> type) {
		String simpleName = type.getSimpleName();
		if (!simpleName.isEmpty()) {
			return simpleName;
		}
		String name = type.getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}

	/**
	 * A command of a control class.
	 */
	private record ParamsKey(Class<?> type, String command) {
		// Pure value.
	}

	/**
	 * A readable, unique id of a schema: the given base name, made unique by a number if the
	 * schema of another owner already has it.
	 *
	 * @param base
	 *        The readable name of the schema.
	 * @param owner
	 *        What the schema describes: the argument interface or the command of a control class.
	 */
	private static String schemaId(String base, Object owner) {
		synchronized (SCHEMA_IDS) {
			String id = base;
			for (int n = 2; SCHEMA_IDS.containsKey(id) && !SCHEMA_IDS.get(id).equals(owner); n++) {
				id = base + n;
			}
			SCHEMA_IDS.put(id, owner);
			return id;
		}
	}

	private static Object buildArgsSchema(ReactControl control, String command, ConfigurationDescriptor argType) {
		try {
			Schema schema = new JsonConfigSchemaBuilder().buildConfigSchema(argType);
			@SuppressWarnings("unchecked")
			Map<String, Object> parsed = (Map<String, Object>) JSON.read(new StringReader(JsonSchemaWriter.toJson(schema)));
			stripEnvelopeProperties(parsed);
			return simplify(parsed, argType.getConfigurationInterface().getName());
		} catch (Exception | LinkageError ex) {
			// The schema builder reflects over every registered subtype of every item-valued
			// property; a subtype that cannot even link in this environment (LinkageError) must
			// degrade the advertised schema, not break observation.
			Logger.info("No full argument schema for command '" + command + "' on "
				+ control.getClass().getName() + " (unbounded value space): " + ex.getMessage(),
				ScriptingTreeProjector.class);
			return minimalArgsSchema(argType);
		}
	}

	/**
	 * The plain object schema of an argument interface: its title and description, and the
	 * properties and required properties of the interface and all its super interfaces in one
	 * {@code properties} and {@code required} member.
	 *
	 * <p>
	 * The schema builder describes a configuration interface as a chain of {@code allOf}
	 * combinations of one {@code $defs} entry per super interface, with a {@code $type} property
	 * naming the concrete interface, and offers the sub-interfaces of a polymorphic interface as
	 * {@code anyOf} alternatives. A command binds its arguments into exactly its declared
	 * interface, so the alternative of that interface is kept, and the chain is merged into one
	 * object schema; the {@code $type} property is dropped, since a client does not send it. A
	 * schema of another form is returned as it is.
	 * </p>
	 *
	 * @param schema
	 *        The schema as built by the {@link JsonConfigSchemaBuilder}.
	 * @param typeName
	 *        The name of the argument interface.
	 */
	@SuppressWarnings("unchecked")
	private static Object simplify(Map<String, Object> schema, String typeName) {
		Object defsValue = schema.get(SCHEMA_DEFS);
		Map<String, Object> defs = defsValue instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
		Object body = schema;
		if (schema.get(SCHEMA_ANY_OF) instanceof List<?> alternatives) {
			body = null;
			for (Object alternative : alternatives) {
				if (declaresType(alternative, defs, typeName, 0)) {
					body = alternative;
					break;
				}
			}
			if (body == null) {
				return schema;
			}
		}
		Map<String, Object> properties = new LinkedHashMap<>();
		List<Object> required = new ArrayList<>();
		if (!merge(body, schema, defs, properties, required, 0)) {
			return schema;
		}
		Map<String, Object> result = new LinkedHashMap<>();
		copy(schema, result, SCHEMA_TITLE);
		copy(schema, result, SCHEMA_DESCRIPTION);
		result.put(SCHEMA_TYPE, SCHEMA_TYPE_OBJECT);
		result.put(SCHEMA_PROPERTIES, properties);
		if (!required.isEmpty()) {
			result.put(SCHEMA_REQUIRED, required);
		}
		if (JSON.toString(properties).contains('"' + ScriptingNodeView.REF + '"')) {
			// A property schema refers to a definition.
			result.put(SCHEMA_DEFS, defs);
		}
		return result;
	}

	private static void copy(Map<String, Object> source, Map<String, Object> target, String key) {
		Object value = source.get(key);
		if (value != null) {
			target.put(key, value);
		}
	}

	/**
	 * Whether the given schema (an alternative of an {@code anyOf}) fixes the {@code $type}
	 * property to the given interface name.
	 */
	private static boolean declaresType(Object schema, Map<String, Object> defs, String typeName, int depth) {
		if (!(schema instanceof Map<?, ?> map) || depth > MAX_SCHEMA_DEPTH) {
			return false;
		}
		if (map.get(SCHEMA_PROPERTIES) instanceof Map<?, ?> properties
			&& properties.get(SCHEMA_TYPE_PROPERTY) instanceof Map<?, ?> typeProperty
			&& typeName.equals(typeProperty.get(SCHEMA_CONST))) {
			return true;
		}
		if (map.get(ScriptingNodeView.REF) instanceof String ref && ref.startsWith(DEFS_REFERENCE_PREFIX)) {
			return declaresType(defs.get(ref.substring(DEFS_REFERENCE_PREFIX.length())), defs, typeName, depth + 1);
		}
		if (map.get(SCHEMA_ALL_OF) instanceof List<?> parts) {
			for (Object part : parts) {
				if (declaresType(part, defs, typeName, depth + 1)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Merges the properties and required properties of an object schema, following its
	 * {@code allOf} parts and its references into the given definitions.
	 *
	 * @return Whether the schema is a plain object schema of that form. If not, the merge result
	 *         is incomplete.
	 */
	private static boolean merge(Object schema, Map<String, Object> top, Map<String, Object> defs,
			Map<String, Object> properties, List<Object> required, int depth) {
		if (!(schema instanceof Map<?, ?> map) || depth > MAX_SCHEMA_DEPTH) {
			return false;
		}
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (SCHEMA_TITLE.equals(key) || SCHEMA_DESCRIPTION.equals(key)) {
				// The documentation of the argument interface is taken from the top-level schema.
				continue;
			}
			if (map == top && SCHEMA_DEFS.equals(key)) {
				continue;
			}
			if (SCHEMA_ALL_OF.equals(key) && value instanceof List<?> parts) {
				for (Object part : parts) {
					if (!merge(part, top, defs, properties, required, depth + 1)) {
						return false;
					}
				}
			} else if (ScriptingNodeView.REF.equals(key) && value instanceof String ref
				&& ref.startsWith(DEFS_REFERENCE_PREFIX)) {
				Object target = defs.get(ref.substring(DEFS_REFERENCE_PREFIX.length()));
				if (!merge(target, top, defs, properties, required, depth + 1)) {
					return false;
				}
			} else if (SCHEMA_TYPE.equals(key)) {
				if (!SCHEMA_TYPE_OBJECT.equals(value)) {
					return false;
				}
			} else if (SCHEMA_PROPERTIES.equals(key) && value instanceof Map<?, ?> declared) {
				for (Map.Entry<?, ?> property : declared.entrySet()) {
					if (!SCHEMA_TYPE_PROPERTY.equals(property.getKey())) {
						properties.putIfAbsent(String.valueOf(property.getKey()), property.getValue());
					}
				}
			} else if (SCHEMA_REQUIRED.equals(key) && value instanceof List<?> names) {
				for (Object name : names) {
					if (!SCHEMA_TYPE_PROPERTY.equals(name) && !required.contains(name)) {
						required.add(name);
					}
				}
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Rewrites the references of the given schema to its own {@code $defs} into references
	 * relative to the observation, which lists the schema under the given id.
	 */
	@SuppressWarnings("unchecked")
	private static Object relocateReferences(Object schema, String id) {
		if (schema instanceof Map<?, ?> map) {
			for (Map.Entry<String, Object> entry : ((Map<String, Object>) map).entrySet()) {
				if (ScriptingNodeView.REF.equals(entry.getKey()) && entry.getValue() instanceof String ref
					&& ref.startsWith(DEFS_REFERENCE_PREFIX)) {
					entry.setValue(ArgsSchema.reference(id) + "/" + SCHEMA_DEFS + "/"
						+ ref.substring(DEFS_REFERENCE_PREFIX.length()));
				} else {
					relocateReferences(entry.getValue(), id);
				}
			}
		} else if (schema instanceof List<?> list) {
			for (Object element : list) {
				relocateReferences(element, id);
			}
		}
		return schema;
	}

	/**
	 * A minimal object schema naming the argument properties (without value schemas), for an
	 * argument interface whose full schema cannot be built.
	 */
	private static Object minimalArgsSchema(ConfigurationDescriptor argType) {
		Map<String, Object> properties = new HashMap<>();
		for (com.top_logic.basic.config.PropertyDescriptor property : argType.getProperties()) {
			String name = property.getPropertyName();
			if (!ENVELOPE_PROPERTIES.contains(name)) {
				properties.put(name, Map.of());
			}
		}
		Map<String, Object> schema = new HashMap<>();
		schema.put(SCHEMA_TYPE, SCHEMA_TYPE_OBJECT);
		schema.put(SCHEMA_PROPERTIES, properties);
		return schema;
	}

	/**
	 * Built argument schemas by descriptor, so the (potentially expensive or failing) build runs once
	 * per argument interface instead of once per observation.
	 */
	private static final Map<ConfigurationDescriptor, ArgsSchema> SCHEMA_CACHE =
		new java.util.concurrent.ConcurrentHashMap<>();

	/**
	 * Schemas of hand-declared parameters by control class and command, see
	 * {@link #paramsSchemaOf(ReactControl, String)}.
	 */
	private static final Map<ParamsKey, ArgsSchema> PARAMS_SCHEMA_CACHE =
		new java.util.concurrent.ConcurrentHashMap<>();

	/**
	 * The owners of the schemas by {@link ArgsSchema#id() id}, see {@link #schemaId(String, Object)}.
	 */
	private static final Map<String, Object> SCHEMA_IDS = new HashMap<>();

	/** Nesting limit when following {@code allOf} parts and references of a schema. */
	private static final int MAX_SCHEMA_DEPTH = 32;

	private static final String SCHEMA_DEFS = "$defs";

	private static final String DEFS_REFERENCE_PREFIX = "#/" + SCHEMA_DEFS + "/";

	private static final String SCHEMA_ALL_OF = "allOf";

	private static final String SCHEMA_ANY_OF = "anyOf";

	private static final String SCHEMA_TITLE = "title";

	private static final String SCHEMA_DESCRIPTION = "description";

	private static final String SCHEMA_TYPE = "type";

	private static final String SCHEMA_TYPE_OBJECT = "object";

	private static final String SCHEMA_PROPERTIES = "properties";

	private static final String SCHEMA_REQUIRED = "required";

	private static final String SCHEMA_CONST = "const";

	/** The property of a configuration schema naming the concrete configuration interface. */
	private static final String SCHEMA_TYPE_PROPERTY = "$type";

	/**
	 * Removes the {@link com.top_logic.layout.react.control.ReactCommand} envelope properties from
	 * every {@code properties} object and {@code required} list of the given parsed JSON Schema, in
	 * place.
	 */
	private static void stripEnvelopeProperties(Object schema) {
		if (schema instanceof Map<?, ?> map) {
			Object properties = map.get(SCHEMA_PROPERTIES);
			if (properties instanceof Map<?, ?> propertyMap) {
				propertyMap.keySet().removeAll(ENVELOPE_PROPERTIES);
			}
			Object required = map.get(SCHEMA_REQUIRED);
			if (required instanceof List<?> requiredList) {
				requiredList.removeAll(ENVELOPE_PROPERTIES);
			}
			for (Object value : map.values()) {
				stripEnvelopeProperties(value);
			}
		} else if (schema instanceof List<?> list) {
			for (Object element : list) {
				stripEnvelopeProperties(element);
			}
		}
	}

	/**
	 * The {@link com.top_logic.layout.react.control.ReactCommand} base property names stripped from
	 * advertised argument schemas by {@link #stripEnvelopeProperties(Object)}.
	 */
	private static final Set<String> ENVELOPE_PROPERTIES = Set.of(
		com.top_logic.layout.react.control.ReactCommand.ADDRESS,
		com.top_logic.layout.react.control.ReactCommand.NAME,
		com.top_logic.layout.react.control.ReactCommand.TARGET);

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
