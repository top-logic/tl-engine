/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;

/**
 * Composes a single context menu from multiple {@link ContextMenuContribution}s and dispatches
 * selections back to the contributing {@link CommandModel}.
 *
 * <p>
 * For each {@link Targeted} contribution the target value is written to the contribution's channel
 * before reading the resulting {@link ContextMenuContribution#visibleCommands()}. Items are ordered
 * by contribution index, separated by a {@link MenuEntry#separator() separator} between
 * contributions and between cliques within a contribution. Wire item IDs are
 * {@code "<contributionIndex>:<commandName>"} so names may collide across contributions.
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
		 */
		void show(int x, int y, List<MenuEntry> items, Consumer<String> selectHandler, Runnable closeHandler);

		/**
		 * Hide the currently displayed menu.
		 */
		void hide();
	}

	/**
	 * Pairing of a {@link ContextMenuContribution} with the concrete target value to publish into
	 * the contribution's channel.
	 */
	public record Targeted(ContextMenuContribution contribution, Object target) {
		// record
	}

	private final MenuRenderer _renderer;

	private List<Targeted> _active = List.of();

	private Supplier<ReactContext> _contextSupplier;

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
	 * <p>
	 * Writes each target into the corresponding contribution channel, then assembles a flat menu
	 * from the visible commands. Does nothing if all contributions produce no visible commands.
	 * </p>
	 */
	public void open(int x, int y, List<Targeted> contributions) {
		for (Targeted t : contributions) {
			t.contribution().target().set(t.target());
		}

		List<MenuEntry> items = new ArrayList<>();
		boolean anything = false;
		for (int i = 0; i < contributions.size(); i++) {
			List<CommandModel> commands = contributions.get(i).contribution().visibleCommands();
			if (commands.isEmpty()) {
				continue;
			}
			if (anything) {
				items.add(MenuEntry.separator());
			}
			appendCliqued(items, i, commands);
			anything = true;
		}
		if (!anything) {
			return;
		}

		_active = List.copyOf(contributions);
		_renderer.show(x, y, items, this::handleSelect, this::handleClose);
	}

	private static void appendCliqued(List<MenuEntry> out, int contributionIndex, List<CommandModel> commands) {
		List<CommandModel> sorted = new ArrayList<>(commands);
		sorted.sort(Comparator.comparing(cmd -> nullSafe(cmd.getClique())));

		String currentClique = null;
		boolean first = true;
		for (CommandModel cmd : sorted) {
			String clique = nullSafe(cmd.getClique());
			if (!first && !clique.equals(currentClique)) {
				out.add(MenuEntry.separator());
			}
			out.add(MenuEntry.item(
				contributionIndex + ":" + cmd.getName(),
				cmd.getLabel(),
				encodeIcon(cmd.getImage()),
				!cmd.isExecutable()));
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

	private void handleSelect(String itemId) {
		int colon = itemId.indexOf(':');
		int idx = Integer.parseInt(itemId.substring(0, colon));
		String name = itemId.substring(colon + 1);
		Targeted t = _active.get(idx);
		CommandModel cmd = findCommand(t.contribution().commands(), name);
		if (cmd != null && cmd.isExecutable()) {
			cmd.executeCommand(currentReactContext());
		}
		_renderer.hide();
		_active = List.of();
	}

	private void handleClose() {
		_active = List.of();
	}

	private static CommandModel findCommand(List<CommandModel> list, String name) {
		for (CommandModel cmd : list) {
			if (name.equals(cmd.getName())) {
				return cmd;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link ReactContext} to use for command execution, or {@code null} if none is
	 * bound.
	 */
	protected ReactContext currentReactContext() {
		return _contextSupplier == null ? null : _contextSupplier.get();
	}

}
