/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Composes a single context menu from multiple {@link ContextMenuContribution}s and dispatches
 * selections back to the contributing {@link CommandModel}.
 *
 * <p>
 * For each {@link Targeted} contribution the target value is published via the contribution's
 * {@link ContextMenuContribution#setTarget() setter} before reading the resulting
 * {@link ContextMenuContribution#visibleCommands()}. Items are ordered by contribution index,
 * separated by a {@link MenuEntry#separator() separator} between contributions and between cliques
 * within a contribution; a contribution carrying a {@link ContextMenuContribution#label() label}
 * opens with a {@link MenuEntry#header(String) header} naming its entries. Wire item IDs are
 * {@code "<contributionIndex>:<commandName>"} so names may collide across contributions. A command
 * that is {@link CommandModel#isActive() active} yields an entry marked as the alternative in
 * force.
 * </p>
 *
 * <p>
 * A selection runs the command through {@link CommandModel#executeCommand(ReactContext)}, and
 * its result - the refusal of a command that is not executable included - is the result of the
 * selection.
 * </p>
 *
 * <p>
 * The opener uses a {@link MenuRenderer} abstraction over {@code ReactMenuControl} so that it can
 * be unit-tested without a real control.
 * </p>
 */
public class ContextMenuOpener {

	/**
	 * Abstraction over {@link com.top_logic.layout.react.control.overlay.ReactMenuControl} so the
	 * opener is unit-testable.
	 */
	public interface MenuRenderer {
		/**
		 * Show a menu at the given pixel coordinates with the given items.
		 *
		 * @param selectHandler
		 *        Called with the ID of the selected item, returning the result of the selection,
		 *        see {@link ReactMenuControl#setSelectHandler(Function)}.
		 */
		void show(int x, int y, List<MenuEntry> items, Function<String, HandlerResult> selectHandler,
				Runnable closeHandler);

		/**
		 * Show a menu at the element with the given ID.
		 *
		 * <p>
		 * The menu hangs off the element and follows it, instead of standing at the point the
		 * element happened to occupy when the menu was requested. A renderer that cannot place a
		 * menu at an element refuses.
		 * </p>
		 *
		 * @param anchorId
		 *        The ID of the client-side element the menu is placed at.
		 * @param items
		 *        The entries of the menu.
		 * @param selectHandler
		 *        Called with the ID of the selected item, returning the result of the selection,
		 *        see {@link ReactMenuControl#setSelectHandler(Function)}.
		 * @param closeHandler
		 *        Called when the menu is closed without a selection.
		 */
		default void show(String anchorId, List<MenuEntry> items, Function<String, HandlerResult> selectHandler,
				Runnable closeHandler) {
			throw new UnsupportedOperationException("anchored menus");
		}

		/**
		 * Hide the currently displayed menu.
		 */
		void hide();
	}

	/**
	 * Pairing of a {@link ContextMenuContribution} with the concrete target value to publish into
	 * the contribution's target sink.
	 *
	 * @param contribution
	 *        The contribution that produces the context menu.
	 * @param target
	 *        The concrete target value to publish into the contribution's target sink.
	 */
	public record Targeted(ContextMenuContribution contribution, Object target) {
		// record
	}

	private final MenuRenderer _renderer;

	private List<Targeted> _active = List.of();

	private List<List<CommandModel>> _activeCommands = List.of();

	private Supplier<ReactContext> _contextSupplier;

	/** Told when the menu currently shown closes, see {@link #open(String, List, Runnable)}. */
	private Runnable _closed;

	/**
	 * Creates a {@link ContextMenuOpener} backed by the given {@link MenuRenderer}.
	 */
	public ContextMenuOpener(MenuRenderer renderer) {
		_renderer = renderer;
	}

	/**
	 * Installs a supplier for the {@link ReactContext} used when dispatching commands.
	 */
	public void bindReactContext(Supplier<ReactContext> supplier) {
		_contextSupplier = supplier;
	}

	/**
	 * Opens a composed context menu at the given coordinates.
	 *
	 * @see #open(int, int, List, Runnable)
	 */
	public void open(int x, int y, List<Targeted> contributions) {
		open(x, y, contributions, null);
	}

	/**
	 * Opens a composed context menu at the given coordinates.
	 *
	 * <p>
	 * Publishes each target through the corresponding contribution's setter, then assembles a flat
	 * menu from the visible commands. Does nothing if all contributions produce no visible
	 * commands.
	 * </p>
	 *
	 * @param closed
	 *        Told once when the menu closes - by a selection, without one, or because another menu
	 *        replaces it. May be {@code null}.
	 * @return Whether a menu is shown; {@code false} if no contribution offers a visible command.
	 */
	public boolean open(int x, int y, List<Targeted> contributions, Runnable closed) {
		Assembly menu = assemble(contributions);
		if (menu == null) {
			return false;
		}
		activate(contributions, menu, closed);
		_renderer.show(x, y, menu.items(), this::handleSelect, this::handleClose);
		return true;
	}

	/**
	 * Opens a composed context menu at the client-side element with the given ID.
	 *
	 * <p>
	 * Assembles the menu as {@link #open(int, int, List, Runnable)} does. The menu hangs off the
	 * element, so a trigger that opens it - a drop-down button - can report its expanded state
	 * through the given callback.
	 * </p>
	 *
	 * @param anchorId
	 *        The ID of the client-side element the menu is placed at.
	 * @param contributions
	 *        The groups of commands to offer, each with the target to publish.
	 * @param closed
	 *        Told once when the menu closes - by a selection, without one, or because another menu
	 *        replaces it. May be {@code null}.
	 * @return Whether a menu is shown; {@code false} if no contribution offers a visible command.
	 */
	public boolean open(String anchorId, List<Targeted> contributions, Runnable closed) {
		Assembly menu = assemble(contributions);
		if (menu == null) {
			return false;
		}
		activate(contributions, menu, closed);
		_renderer.show(anchorId, menu.items(), this::handleSelect, this::handleClose);
		return true;
	}

	/**
	 * The entries of a composed menu, together with the commands they stand for.
	 *
	 * @param items
	 *        The entries to display.
	 * @param commands
	 *        Per contribution, the commands in the order their entries' IDs address them.
	 */
	private record Assembly(List<MenuEntry> items, List<List<CommandModel>> commands) {
		// record
	}

	/**
	 * Publishes the targets and composes the menu, or returns {@code null} if no contribution
	 * offers a visible command.
	 */
	private static Assembly assemble(List<Targeted> contributions) {
		for (Targeted t : contributions) {
			t.contribution().setTarget().accept(t.target());
		}

		List<MenuEntry> items = new ArrayList<>();
		List<List<CommandModel>> perContributionCommands = new ArrayList<>();
		boolean anything = false;
		for (int i = 0; i < contributions.size(); i++) {
			ContextMenuContribution contribution = contributions.get(i).contribution();
			List<CommandModel> visible = contribution.visibleCommands();
			List<CommandModel> sorted;
			if (visible.isEmpty()) {
				sorted = List.of();
			} else {
				sorted = new ArrayList<>(visible);
				sorted.sort(Comparator.comparing(cmd -> nullSafe(cmd.getClique())));
				if (anything) {
					items.add(MenuEntry.separator());
				}
				String label = contribution.label();
				if (label != null && !label.isEmpty()) {
					items.add(MenuEntry.header(label));
				}
				appendCliqued(items, i, sorted);
				anything = true;
			}
			perContributionCommands.add(sorted);
		}
		if (!anything) {
			return null;
		}
		return new Assembly(items, perContributionCommands);
	}

	/**
	 * Makes the given menu the one selections are dispatched to.
	 *
	 * <p>
	 * Called right before the menu is shown: the menu it replaces is closed only now, so that an
	 * opening that shows nothing leaves the open menu - and its trigger's expanded state - alone.
	 * </p>
	 */
	private void activate(List<Targeted> contributions, Assembly menu, Runnable closed) {
		notifyClosed();
		_active = List.copyOf(contributions);
		_activeCommands = menu.commands();
		_closed = closed;
	}

	/**
	 * Tells the opener of the current menu that it is closed, once.
	 */
	private void notifyClosed() {
		Runnable closed = _closed;
		_closed = null;
		if (closed != null) {
			closed.run();
		}
	}

	private static void appendCliqued(List<MenuEntry> out, int contributionIndex, List<CommandModel> sorted) {
		String currentClique = null;
		boolean first = true;
		for (int j = 0; j < sorted.size(); j++) {
			CommandModel cmd = sorted.get(j);
			String clique = nullSafe(cmd.getClique());
			if (!first && !clique.equals(currentClique)) {
				out.add(MenuEntry.separator());
			}
			out.add(MenuEntry.item(
				contributionIndex + ":" + j,
				cmd.getLabel(),
				encodeIcon(cmd.getImage()),
				cmd.getExecutableState(),
				cmd.getCssClasses(),
				cmd.isActive()));
			currentClique = clique;
			first = false;
		}
	}

	private static String encodeIcon(ThemeImage image) {
		if (image == null) {
			return null;
		}
		return image.resolve().toEncodedForm();
	}

	private static String nullSafe(String s) {
		return s == null ? "" : s;
	}

	private HandlerResult handleSelect(String itemId) {
		int colon = itemId.indexOf(':');
		int contributionIdx = Integer.parseInt(itemId.substring(0, colon));
		int commandIdx = Integer.parseInt(itemId.substring(colon + 1));
		List<CommandModel> commands = _activeCommands.get(contributionIdx);
		CommandModel cmd = commandIdx < commands.size() ? commands.get(commandIdx) : null;

		// Closing the menu is part of dispatching the selection, so it happens before the command
		// runs. Otherwise a command that itself opens a menu (e.g. to choose among element types)
		// would have that menu closed again right after opening it.
		_renderer.hide();
		_active = List.of();
		_activeCommands = List.of();
		notifyClosed();

		if (cmd == null) {
			// A selection from a menu that has been replaced in the meantime.
			return HandlerResult.DEFAULT_RESULT;
		}
		return cmd.executeCommand(currentReactContext());
	}

	private void handleClose() {
		_active = List.of();
		_activeCommands = List.of();
		notifyClosed();
	}

	/**
	 * Returns the {@link ReactContext} to use for command execution, or {@code null} if none is
	 * bound.
	 */
	protected ReactContext currentReactContext() {
		return _contextSupplier == null ? null : _contextSupplier.get();
	}

}
