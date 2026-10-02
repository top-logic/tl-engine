/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Model for a command button providing label, executability, and execution.
 *
 * <p>
 * The {@link ReactButtonControl} uses this interface to read button state (label, disabled) and
 * listen for changes. The button registers a state change listener at construction and removes it
 * during cleanup.
 * </p>
 *
 * <p>
 * The model is the single authority on whether the command may run: every UI element runs it
 * through {@link #executeCommand(ReactContext)}, which refuses a command that is not
 * {@link #getExecutableState() executable} and reports why. An implementation supplies only the
 * command's effect, {@link #perform(ReactContext)}.
 * </p>
 *
 * <p>
 * Lifecycle management ({@code attach}/{@code detach}) is NOT part of this interface because a
 * single model may be shared between multiple buttons. This happens when a child element contributes
 * an implicit command to a panel's {@code CommandScope}: the child creates a button for the command,
 * and the panel adds a second toolbar button for the same model. If the toolbar button is later
 * removed (because the command leaves the scope), it must not detach the model that the child's
 * button still uses. The model creator is responsible for lifecycle via
 * {@code ReactControl.addCleanupAction()}.
 * </p>
 */
public interface CommandModel {

	/**
	 * The command name for lookup via scope resolution (may be {@code null}).
	 */
	String getName();

	/**
	 * The resolved button label text.
	 */
	String getLabel();

	/**
	 * The image for the command button, or {@code null} if no image is set.
	 */
	ThemeImage getImage();

	/**
	 * The resolved tooltip text for the command button, or {@code null} if no tooltip is set.
	 */
	default String getTooltip() {
		return null;
	}

	/**
	 * Whether the command is currently executable.
	 */
	boolean isExecutable();

	/**
	 * Whether the command's effect is currently in force.
	 *
	 * <p>
	 * The alternative currently chosen among a set - the active UI theme, the selected language -
	 * or a toggle that is currently pressed. An active command is rendered as a marked button or
	 * menu entry, so that the choice in force is visible next to the alternatives still on offer.
	 * </p>
	 *
	 * <p>
	 * Independent of {@link #isExecutable()} and of {@link #isVisible()}: choosing the alternative
	 * that is already in force stays executable, and the user sees which one that is.
	 * </p>
	 */
	default boolean isActive() {
		return false;
	}

	/**
	 * Whether the command button should be visible.
	 *
	 * <p>
	 * A hidden command is not displayed to the user at all (e.g. "Apply" when the form is in view
	 * mode). A visible but non-executable command is shown in disabled state.
	 * </p>
	 *
	 * @return {@code true} if the button should be rendered visibly, {@code false} if it should be
	 *         hidden.
	 */
	boolean isVisible();

	/**
	 * The state the command's rules assign to it right now.
	 *
	 * <p>
	 * Agrees with {@link #isVisible()} and {@link #isExecutable()}, and in addition carries the
	 * {@link ExecutableState#getI18NReasonKey() reason} for a refusal: a UI element that refuses to
	 * run the command reports this state, so that the user learns why.
	 * </p>
	 *
	 * <p>
	 * By default, the state is derived from {@link #isVisible()} and {@link #isExecutable()} and
	 * carries only the generic reasons {@link ExecutableState#NOT_EXEC_HIDDEN} and
	 * {@link ExecutableState#NOT_EXEC_DISABLED}. A model whose rules give a reason of their own
	 * reports it by overriding this method; a model delegating to another one delegates this
	 * method as well.
	 * </p>
	 */
	default ExecutableState getExecutableState() {
		if (!isVisible()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		if (!isExecutable()) {
			return ExecutableState.NOT_EXEC_DISABLED;
		}
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * Executes the command, if it is executable.
	 *
	 * <p>
	 * This is the one entry for running a command from the user interface - a button, a menu
	 * entry, a sidebar item, a dashboard tile. The command's {@link #getExecutableState() state}
	 * decides: a command that is not executable does not {@link #perform(ReactContext) perform}
	 * anything, and the call returns the {@link HandlerResult#notExecutable(ExecutableState)
	 * refusal} carrying that state, so that the user learns why. A command that is executable
	 * performs, and the call returns what it reports.
	 * </p>
	 *
	 * <p>
	 * The check is part of this contract, not of the caller: whoever executes a command gets the
	 * refusal as result and passes it on to be reported, like any other result of the command.
	 * </p>
	 *
	 * @param context
	 *        The view display context.
	 * @return The result of the command execution, or the refusal of a command that is not
	 *         executable.
	 */
	default HandlerResult executeCommand(ReactContext context) {
		ExecutableState state = getExecutableState();
		if (!state.isExecutable()) {
			return HandlerResult.notExecutable(state);
		}
		return perform(context);
	}

	/**
	 * Performs the command's effect.
	 *
	 * <p>
	 * Called by {@link #executeCommand(ReactContext)} after the command has been found executable;
	 * a user interface runs a command through {@link #executeCommand(ReactContext)}, never through
	 * this method. An implementation whose rules decide by more than its
	 * {@link #getExecutableState() current state} - an input evaluated anew - may still refuse
	 * here, and reports that by returning a {@link HandlerResult#notExecutable(ExecutableState)
	 * refusal}.
	 * </p>
	 *
	 * @param context
	 *        The view display context.
	 * @return The result of the command execution.
	 */
	HandlerResult perform(ReactContext context);

	/**
	 * Where the command should be rendered.
	 *
	 * <p>
	 * Used by containers (e.g. panels) to decide where to create buttons. Never {@code null};
	 * defaults to {@link CommandPlacement#NONE} for programmatic-only commands.
	 * </p>
	 */
	CommandPlacement getPlacement();

	/**
	 * The clique identifier used to group this command with other related commands.
	 *
	 * <p>
	 * Commands within the same clique are rendered together (e.g. in menus); separators are drawn
	 * between cliques. Returns {@code null} by default if no clique is assigned.
	 * </p>
	 */
	default String getClique() {
		return null;
	}

	/**
	 * The {@link ButtonDisplayMode} explicitly requested for this command's button, or
	 * {@code null} (the default) to let the rendering container decide.
	 */
	default ButtonDisplayMode getDisplayMode() {
		return null;
	}

	/**
	 * Additional CSS classes for the UI element rendering this command, separated by spaces.
	 *
	 * <p>
	 * Appended to the class list of the command's button, and of a menu entry rendering the
	 * command. Returns {@code null} by default (no additional classes). Whether the command is
	 * destructive is not a class but its {@link #getTone() tone}.
	 * </p>
	 */
	default String getCssClasses() {
		return null;
	}

	/**
	 * The keyboard gesture that triggers this command, e.g. {@link KeyStroke#ENTER} or
	 * {@code KeyStroke.of(Key.S).ctrl()}.
	 *
	 * <p>
	 * Serialized to the client where the keyboard dispatcher binds it to the command's button.
	 * Returns {@code null} by default (no gesture).
	 * </p>
	 */
	default KeyStroke getKeyGesture() {
		return null;
	}

	/**
	 * The kind of action this command stands for.
	 *
	 * <p>
	 * {@link ButtonTone#DANGER} for a command that destroys or discards what the user has: every UI
	 * element offering it - its button, a menu entry, the confirmation asking for it - shows it as
	 * destructive. The tone follows from what the command does, not from a presentation option, and
	 * does not change while the model lives. Returns {@link ButtonTone#DEFAULT} by default.
	 * </p>
	 */
	default ButtonTone getTone() {
		return ButtonTone.DEFAULT;
	}

	/**
	 * Registers a listener that is notified when any visible state (label, executability) changes.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	void addStateChangeListener(Runnable listener);

	/**
	 * Removes a previously registered state change listener.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	void removeStateChangeListener(Runnable listener);
}
