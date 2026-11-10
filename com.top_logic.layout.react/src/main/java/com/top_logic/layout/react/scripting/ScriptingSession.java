/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.scripting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.identity.ControlReferences;
import com.top_logic.layout.react.control.identity.ObjectIdTranslation;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector.Node;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

import de.haumacher.msgbuf.data.DataObject;

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
 * {@link ScriptingNodeView} (what is on screen, what it holds, what can be done),</li>
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
 * the window's displayed root control (see {@link #forRoot(ReactControl)}). A {@link ScriptingSession} is
 * a thin, stateless view; create one per interaction.
 * </p>
 */
public final class ScriptingSession implements ControlReferences {

	/**
	 * Address of the synthetic root node.
	 */
	public static final String ROOT = ScriptingTreeProjector.SEPARATOR;

	/**
	 * Member of the {@link #observeActions() affordance-first view} listing the actionable nodes.
	 */
	public static final String ACTIONS = "actions";

	private final ReactControl _root;

	/** The registry of the window's controls by their ids, {@code null} if there is none. */
	private final SSEUpdateQueue _queue;

	private ScriptingSession(ReactControl root, SSEUpdateQueue queue) {
		_root = root;
		_queue = queue;
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
	public static ScriptingSession forRoot(ReactControl root) {
		return new ScriptingSession(root, root == null ? null : root.getReactContext().getSSEQueue());
	}

	/**
	 * Creates a headless session over the window the given control is displayed in.
	 *
	 * @param member
	 *        A control of the window.
	 * @return A new session rooted at the window's root control.
	 */
	public static ScriptingSession forWindowOf(ReactControl member) {
		SSEUpdateQueue queue = member.getReactContext().getSSEQueue();
		return new ScriptingSession(queue == null ? null : queue.getRootControl(), queue);
	}

	/**
	 * The top-level nodes: the window root, or its children if the root itself is a structural
	 * wrapper.
	 */
	private List<Node> roots() {
		return _root == null ? List.of() : ScriptingTreeProjector.roots(_root);
	}

	/**
	 * Projects the current control tree into the addressable observation an agent acts on, rooted
	 * at the synthetic {@link #ROOT} node.
	 *
	 * @return The root {@link ScriptingNodeView}; its {@link ScriptingNodeView#children() children} are the
	 *         window's root controls.
	 *
	 * @see #observe(ProjectionMode)
	 */
	public ScriptingNodeView observe() {
		return observe(ProjectionMode.AGENT);
	}

	/**
	 * Projects the current control tree into an addressable observation rooted at the synthetic
	 * {@link #ROOT} node.
	 *
	 * @param mode
	 *        How the nodes identify the objects they display: {@link ProjectionMode#AGENT} by the
	 *        ids the commands take, {@link ProjectionMode#KEY} by business keys, the same in every
	 *        session.
	 * @return The root {@link ScriptingNodeView}; its {@link ScriptingNodeView#children() children} are the
	 *         window's root controls.
	 */
	public ScriptingNodeView observe(ProjectionMode mode) {
		List<Node> roots = roots();
		List<String> segments = ScriptingTreeProjector.segments(null, roots, mode);
		List<ScriptingNodeView> rootViews = new ArrayList<>(roots.size());
		for (int i = 0; i < roots.size(); i++) {
			String address = ScriptingTreeProjector.join(ROOT, segments.get(i));
			rootViews.add(ScriptingTreeProjector.project(roots.get(i), address, mode));
		}
		return new ScriptingNodeView(ROOT, "app", null, null, Map.of(), List.of(), rootViews);
	}

	/**
	 * Convenience: the {@link #observe() observation} rendered as indented JSON.
	 */
	public String observeJson() {
		return observeJson(ProjectionMode.AGENT);
	}

	/**
	 * Convenience: the {@link #observe(ProjectionMode) observation} in the given mode rendered as
	 * indented JSON.
	 */
	public String observeJson(ProjectionMode mode) {
		return observe(mode).toJson();
	}

	/**
	 * The affordance-first view: a flat list of the actionable nodes (each with its address, role,
	 * name, semantic state and actions, but no children), rendered as JSON.
	 *
	 * <p>
	 * This is the compact view for an agent loop — it answers "what can I do right now and where"
	 * without the surrounding container hierarchy. Addresses are the same ones {@link #act} accepts.
	 * </p>
	 *
	 * @see #observeActions()
	 */
	public String observeActionsJson() {
		return ScriptingNodeView.toJson(observeActions());
	}

	/**
	 * The affordance-first view of {@link #observeActionsJson()} as a plain {@link Map}/{@link List}
	 * structure: an object whose {@link #ACTIONS} member lists the actionable nodes and whose
	 * {@link ScriptingNodeView#SCHEMAS} member lists the argument schemas their actions refer to.
	 *
	 * <p>
	 * The structure is complete when this method returns, so a caller can render it with
	 * {@link ScriptingNodeView#writeJson(Appendable, Object)} after all projection work is done.
	 * </p>
	 */
	public Map<String, Object> observeActions() {
		Map<String, Object> schemas = new TreeMap<>();
		List<Object> entries = new ArrayList<>();
		for (ScriptingNodeView node : observe().actionableNodes()) {
			entries.add(node.toMap(false, schemas));
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(ACTIONS, entries);
		ScriptingNodeView.putSchemas(result, schemas);
		return result;
	}

	/**
	 * Resolves a semantic address to the live control it designates.
	 *
	 * @param address
	 *        An address as produced by {@link #observe()} (e.g. {@code "/form/button[Submit]"}).
	 * @return The control at that address, or {@code null} for the synthetic {@link #ROOT}.
	 * @throws IllegalArgumentException
	 *         If no control matches the address, or the address designates an
	 *         {@link com.top_logic.layout.react.control.ScriptingItem item} rather than a control.
	 *
	 * @see #resolveNode(String)
	 */
	public ReactControl resolve(String address) {
		Node node = resolveNode(address);
		if (node == null) {
			return null;
		}
		if (node.isItem()) {
			throw new IllegalArgumentException("The address '" + address + "' designates the item '"
				+ node + "', not a control.");
		}
		return node.control();
	}

	/**
	 * Resolves a semantic address to the node of the projection it designates.
	 *
	 * <p>
	 * The segment of an {@link com.top_logic.layout.react.control.ScriptingItem item} is accepted in
	 * both forms the projection addresses it in: by the id of the item, as the
	 * {@link ProjectionMode#AGENT agent observation} shows it, and by the business key of its
	 * object, as a recorded address carries it (see {@link #addressOf(ReactControl)}).
	 * </p>
	 *
	 * @param address
	 *        An address as produced by {@link #observe(ProjectionMode)}.
	 * @return The node at that address, or {@code null} for the synthetic {@link #ROOT}.
	 * @throws IllegalArgumentException
	 *         If no node matches the address.
	 */
	public Node resolveNode(String address) {
		List<String> segments = splitAddress(address);
		if (segments.isEmpty()) {
			return null;
		}

		List<Node> candidates = roots();
		Node current = null;
		StringBuilder walked = new StringBuilder();
		for (String segment : segments) {
			List<String> available = ScriptingTreeProjector.segments(current, candidates, ProjectionMode.AGENT);
			int index = available.indexOf(segment);
			if (index < 0) {
				index = ScriptingTreeProjector.segments(current, candidates, ProjectionMode.KEY).indexOf(segment);
			}
			if (index < 0) {
				throw new IllegalArgumentException("No node at address '" + address + "': segment '"
					+ segment + "' not found under '" + (walked.length() == 0 ? ROOT : walked.toString())
					+ "'. Available: " + available);
			}
			current = candidates.get(index);
			candidates = ScriptingTreeProjector.children(current);
			walked.append(ScriptingTreeProjector.SEPARATOR).append(segment);
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
	 *        The command ID (an advertised {@link ScriptingAction#command()}).
	 * @param arguments
	 *        The command arguments, or an empty map.
	 * @return The reply of the command's handler, <code>null</code> if it replies nothing, see
	 *         {@link com.top_logic.layout.react.control.ReactCommandHandler}.
	 * @throws IllegalArgumentException
	 *         If the address does not resolve to a control.
	 * @throws RuntimeException
	 *         The failure of the command, a {@link com.top_logic.util.error.TopLogicException} for
	 *         a failure the user can act on.
	 */
	public DataObject act(String address, String command, Map<String, Object> arguments) {
		return execute(target(address), command, arguments);
	}

	/**
	 * Dispatches a command to a control on behalf of a headless consumer - an agent or a replayed
	 * script -, refusing a command the control does not offer in its current state.
	 *
	 * <p>
	 * The headless consumer can do what a user can do, no more: a command that is not
	 * {@link ReactControl#scriptingCommandState(String) executable} - the click of a disabled
	 * button, the input into a read-only field - is refused, as a click on it in the browser would
	 * be impossible. Every headless dispatch goes through here.
	 * </p>
	 *
	 * @param target
	 *        The control to dispatch to.
	 * @param command
	 *        The command ID.
	 * @param arguments
	 *        The command arguments, {@code null} for none.
	 * @return The reply of the command's handler, <code>null</code> if it replies nothing.
	 * @throws TopLogicException
	 *         The {@link CommandErrors#notExecutable(ExecutableState) refusal} of a command that is
	 *         not executable, or the failure of the command.
	 */
	public static DataObject execute(ReactControl target, String command, Map<String, Object> arguments) {
		ExecutableState state = target.scriptingCommandState(command);
		if (!state.isExecutable()) {
			throw CommandErrors.notExecutable(state);
		}
		return target.executeCommand(command, arguments == null ? Map.of() : arguments);
	}

	/**
	 * The control at the given address, which an {@link #act(String, String, Map) action} can
	 * address.
	 *
	 * @param address
	 *        The address of the control.
	 * @return The control, never {@code null}.
	 * @throws IllegalArgumentException
	 *         If the address does not resolve to a control.
	 */
	public ReactControl target(String address) {
		ReactControl control = resolve(address);
		if (control == null) {
			throw new IllegalArgumentException("Cannot act on the synthetic root node '" + address + "'.");
		}
		return control;
	}

	/**
	 * Replays a recorded step: an {@link AssertCommand assertion} is verified against the projected
	 * state of the node at its address (a subset match, see
	 * {@link AssertCommand#mismatchingKeys(Map, Map)}, in the {@link ProjectionMode#KEY key form}),
	 * any other step is dispatched to the control at its address.
	 *
	 * <p>
	 * The keys the step carries in its {@link com.top_logic.layout.react.control.identity.DisplayedObjectId
	 * object id properties} are resolved to the ids the objects have in the target control now,
	 * through its {@link ReactControl#objectIdentities() object identities}, see
	 * {@link ObjectIdTranslation#keysToIds(ReactCommand, ReactControl, ControlReferences)}. Keys of
	 * objects another control displays (see
	 * {@link com.top_logic.layout.react.control.identity.DisplayedObjectId#control()}) are resolved
	 * through that control, which the step names by its address.
	 * </p>
	 *
	 * @param step
	 *        The recorded step.
	 * @return The reply of the command's handler, <code>null</code> for an assertion and for a
	 *         command replying nothing.
	 * @throws IllegalArgumentException
	 *         If the step's address does not resolve to a control.
	 * @throws TopLogicException
	 *         If the command fails, the assertion does not hold, or a key of the step designates no
	 *         object the target control has at hand.
	 */
	public DataObject replay(ReactCommand step) {
		if (step instanceof AssertCommand assertion) {
			Map<String, Object> expected = assertion.stateEntries();
			Map<String, Object> actual =
				ScriptingTreeProjector.nodeState(resolve(assertion.getAddress()), ProjectionMode.KEY);
			List<String> mismatches = AssertCommand.mismatchingKeys(expected, actual);
			if (!mismatches.isEmpty()) {
				throw new TopLogicException(
					I18NConstants.ERROR_ASSERTION_FAILED__ADDRESS_KEYS.fill(assertion.getAddress(), mismatches));
			}
			return null;
		}
		ReactControl target = target(step.getAddress());
		return execute(target, step.getName(), ObjectIdTranslation.keysToIds(step, target, this));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * A reference starting with {@link #ROOT} is an address and resolves like
	 * {@link #resolve(String)}; any other is the id the control is registered under in the window.
	 * </p>
	 */
	@Override
	public ReactControl controlOf(String reference) {
		if (reference == null || reference.isEmpty()) {
			return null;
		}
		if (reference.startsWith(ROOT)) {
			try {
				return resolve(reference);
			} catch (IllegalArgumentException ex) {
				return null;
			}
		}
		if (_queue == null) {
			return null;
		}
		ReactCommandTarget registered = _queue.getControl(reference);
		return registered instanceof ReactControl control ? control : null;
	}

	/**
	 * The replay-stable semantic address of the given live control — the reverse of
	 * {@link #resolve(String)}.
	 *
	 * <p>
	 * Used by the script recorder to turn the control a browser command targets (by its session
	 * control id) into the stable address a recorded step carries. It walks the projected tree with
	 * the same segment algorithm {@link #resolve(String)} uses, so the returned address is guaranteed
	 * to resolve back to {@code target}. An {@link com.top_logic.layout.react.control.ScriptingItem
	 * item} on the way is addressed by the business key of its object, as in the
	 * {@link ProjectionMode#KEY key projection}, so that the address designates the same item in
	 * another session.
	 * </p>
	 *
	 * @param target
	 *        The control to address.
	 * @return The address, or {@code null} if {@code target} is not in the projection (e.g. a
	 *         read-only cell control, a hidden control, or a control under an elided wrapper).
	 */
	@Override
	public String addressOf(ReactControl target) {
		if (target == null) {
			return null;
		}
		List<Integer> path = new ArrayList<>();
		if (!searchPath(roots(), target, path)) {
			return null;
		}
		String address = ROOT;
		Node parent = null;
		List<Node> candidates = roots();
		for (int level = 0; level < path.size(); level++) {
			int index = path.get(level).intValue();
			Node node = candidates.get(index);
			// The segments of controls do not depend on the mode; keys are built only where needed.
			ProjectionMode mode = node.isItem() ? ProjectionMode.KEY : ProjectionMode.AGENT;
			address = ScriptingTreeProjector.join(address,
				ScriptingTreeProjector.segments(parent, candidates, mode).get(index));
			parent = node;
			candidates = ScriptingTreeProjector.children(node);
		}
		return address;
	}

	/**
	 * Searches the given control below the given candidates, recording the index of the node taken
	 * on each level.
	 */
	private static boolean searchPath(List<Node> candidates, ReactControl target, List<Integer> path) {
		for (int i = 0; i < candidates.size(); i++) {
			Node node = candidates.get(i);
			path.add(Integer.valueOf(i));
			if (!node.isItem() && node.control() == target) {
				return true;
			}
			if (searchPath(ScriptingTreeProjector.children(node), target, path)) {
				return true;
			}
			path.remove(path.size() - 1);
		}
		return false;
	}

	private static List<String> splitAddress(String address) {
		List<String> result = new ArrayList<>();
		if (address == null) {
			return result;
		}
		for (String part : address.split(ScriptingTreeProjector.SEPARATOR)) {
			if (!part.isEmpty()) {
				result.add(part);
			}
		}
		return result;
	}
}
