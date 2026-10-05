/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.layout.ToolbarGroupDisplay;

/**
 * The cliques a toolbar groups its commands by, in the order their groups are displayed.
 *
 * <p>
 * A command whose clique is not registered still gets a group of its own: an inline one without
 * label, displayed after the groups of all registered cliques.
 * </p>
 *
 * @see CommandCliqueService#getRegistry()
 */
public class CliqueRegistry {

	private final List<CliqueInfo> _orderedCliques;

	private final Map<String, Integer> _positionByName;

	/**
	 * Creates a {@link CliqueRegistry}.
	 *
	 * @param cliques
	 *        The registered cliques in the order their toolbar groups are displayed. Of several
	 *        definitions of the same name, the first one counts.
	 */
	public CliqueRegistry(List<CliqueInfo> cliques) {
		_orderedCliques = List.copyOf(cliques);
		_positionByName = new HashMap<>();
		for (int n = 0, cnt = _orderedCliques.size(); n < cnt; n++) {
			_positionByName.putIfAbsent(_orderedCliques.get(n).name(), n);
		}
	}

	/**
	 * The {@link CliqueInfo} of the given clique.
	 *
	 * @param name
	 *        The clique name, {@code null} for a command that names no clique, which is grouped
	 *        into {@link CommandCliques#CREATE}.
	 * @return The registered definition, or an inline clique without label for a clique that is
	 *         not registered.
	 */
	public CliqueInfo getClique(String name) {
		String cliqueName = name == null ? CommandCliques.CREATE : name;
		Integer position = _positionByName.get(cliqueName);
		if (position != null) {
			return _orderedCliques.get(position.intValue());
		}
		return new CliqueInfo(cliqueName, ToolbarGroupDisplay.INLINE, null, null);
	}

	/**
	 * The position of the given clique's toolbar group relative to the others (lower is earlier).
	 *
	 * @param name
	 *        The clique name, see {@link #getClique(String)}.
	 * @return The index of the clique in {@link #getOrderedCliques()}, {@link Integer#MAX_VALUE}
	 *         for a clique that is not registered.
	 */
	public int getPosition(String name) {
		Integer position = _positionByName.get(name == null ? CommandCliques.CREATE : name);
		return position == null ? Integer.MAX_VALUE : position.intValue();
	}

	/**
	 * All registered cliques in the order their toolbar groups are displayed.
	 */
	public List<CliqueInfo> getOrderedCliques() {
		return _orderedCliques;
	}

	/**
	 * Definition of a single clique.
	 *
	 * @param name
	 *        The clique name, as commands refer to it.
	 * @param display
	 *        Display mode of the clique group.
	 * @param label
	 *        Menu trigger label (only for menu display, may be {@code null}).
	 * @param icon
	 *        Menu trigger icon (only for menu display, may be {@code null}).
	 */
	public record CliqueInfo(String name, ToolbarGroupDisplay display, ResKey label, ThemeImage icon) {
		// Record.
	}
}
