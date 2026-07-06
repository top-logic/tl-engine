/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationDescriptor;

/**
 * Headless agent-interface of a {@link ReactControl}.
 *
 * <p>
 * These methods expose the control tree, its data state and its action space without rendering or
 * invoking anything, so that a headless consumer (the script recorder or an AI agent driving the
 * session) can observe a control and address it semantically. They reuse the very same state map
 * and command dispatch that the browser client uses, so there is a single source of truth for what
 * a control looks like and what it can do. Acting on a control goes through the inherited
 * {@link #executeCommand(String, java.util.Map)}.
 * </p>
 *
 * <p>
 * Declared in the control package rather than the headless layer so that a control can describe
 * itself to the agent without depending on the headless layer.
 * </p>
 */
public interface AgentControl extends ReactCommandTarget {

	/**
	 * The set of command IDs this control accepts via {@link #executeCommand(String, java.util.Map)}.
	 *
	 * <p>
	 * Derived from the {@link ReactCommand @ReactCommand}-annotated methods of this control's class.
	 * This is the raw action space; the headless layer may enrich it with argument schemas (see
	 * {@link com.top_logic.layout.react.headless.AgentNode}).
	 * </p>
	 */
	Set<String> commandNames();

	/**
	 * Whether this control is a purely structural wrapper that the headless agent projection should
	 * elide, lifting its children into its parent.
	 *
	 * <p>
	 * Layout-only containers (stacks, insets, slots, reload boundaries) carry no task-level meaning
	 * for an agent; emitting them only deepens addresses and bloats the observation. Such controls
	 * override this to return {@code true}. The projection logic stays generic — it asks each control
	 * via this method and never switches on concrete control types — so a new structural container is
	 * handled by overriding this one method, with no change to the projector.
	 * </p>
	 *
	 * @return {@code true} if this control should be elided from the agent projection; {@code false}
	 *         (the default) to appear as an addressable node.
	 */
	boolean agentTransparent();

	/**
	 * A stable, semantic name for this control, used as the discriminator in its agent address (e.g.
	 * a bound field name, a tab label, a business-object key).
	 *
	 * @return The name, or {@code null} (the default) to let the projection derive a name from label
	 *         state or the bound model.
	 */
	String agentName();

	/**
	 * An explicit semantic role for this control (e.g. {@code "field"}, {@code "table"}), overriding
	 * the role the projection otherwise derives from the React module identifier.
	 *
	 * @return The role, or {@code null} for the default derivation.
	 */
	String agentRole();

	/**
	 * The direct child controls embedded in this control's state.
	 *
	 * <p>
	 * In the view system the state tree <em>is</em> the control tree: a child control is simply a
	 * {@link ReactControl}-valued entry in the state map (possibly nested inside maps or lists, as
	 * with {@link ReactCompositeControl}'s {@code children} list or a panel's {@code toolbar}). The
	 * children are returned in a stable order so that semantic addresses are reproducible across
	 * calls.
	 * </p>
	 */
	List<ReactControl> agentChildren();

	/**
	 * A copy of this control's own data state, with all entries that (transitively) hold child
	 * controls removed.
	 *
	 * <p>
	 * The result is the control's semantic, scalar/structural payload (labels, values, flags) as a
	 * headless consumer should see it; embedded child controls are not included here because they are
	 * represented as separate addressable nodes via {@link #agentChildren()}.
	 * </p>
	 */
	Map<String, Object> agentScalarState();

	/**
	 * The commands this control advertises to a headless agent: its {@link #commandNames()} minus its
	 * chrome commands.
	 */
	Set<String> agentCommands();

	/**
	 * The declared argument schema of the given command (from its
	 * {@link ReactCommand#params() @ReactCommand params}), so the headless projection can advertise
	 * what a command expects instead of leaving a consumer to guess.
	 *
	 * @param command
	 *        The command ID.
	 * @return The parameter declarations; empty if the command declares none.
	 */
	ReactParam[] agentCommandParams(String command);

	/**
	 * The {@link ConfigurationDescriptor} of the typed argument the given command declares, or
	 * {@code null} if it takes a raw {@code Map} (or no arguments).
	 *
	 * <p>
	 * When present, the headless projection advertises the command's argument schema and renders a
	 * recorded step from this one interface, rather than from the (superseded)
	 * {@link #agentCommandParams(String) hand-declared params}.
	 * </p>
	 *
	 * @param command
	 *        The command ID.
	 */
	ConfigurationDescriptor agentCommandArgsType(String command);
}
