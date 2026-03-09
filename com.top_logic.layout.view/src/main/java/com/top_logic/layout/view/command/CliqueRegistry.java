/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of standard and local cliques with ordering and display mode metadata.
 *
 * <p>
 * Standard cliques are registered at class load time. Local cliques (panel-specific) are added via
 * {@link #withLocalClique(String, String, String, String, String)}.
 * </p>
 */
public class CliqueRegistry {

	/** Display mode: commands shown inline with separators between groups. */
	public static final String DISPLAY_INLINE = "inline";

	/** Display mode: commands collapsed into a dropdown menu. */
	public static final String DISPLAY_MENU = "menu";

	private static final List<CliqueInfo> STANDARD_CLIQUES;

	static {
		List<CliqueInfo> list = new ArrayList<>();
		list.add(new CliqueInfo(CommandCliques.CREATE, 100, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.EDIT, 200, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.DELETE, 300, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.COMMIT, 400, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.NAVIGATE, 500, DISPLAY_INLINE, null, null));
		list.add(new CliqueInfo(CommandCliques.VIEW, 600, DISPLAY_MENU, "View", null));
		list.add(new CliqueInfo(CommandCliques.EXPORT, 700, DISPLAY_MENU, "Export", null));
		list.add(new CliqueInfo(CommandCliques.MORE, 800, DISPLAY_MENU, "More", null));
		STANDARD_CLIQUES = Collections.unmodifiableList(list);
	}

	private final Map<String, CliqueInfo> _cliqueMap;

	private final List<CliqueInfo> _orderedCliques;

	/**
	 * Creates a registry with only standard cliques.
	 */
	public CliqueRegistry() {
		_cliqueMap = new LinkedHashMap<>();
		_orderedCliques = new ArrayList<>(STANDARD_CLIQUES);
		for (CliqueInfo info : STANDARD_CLIQUES) {
			_cliqueMap.put(info.name(), info);
		}
	}

	/**
	 * Creates a new registry with a local clique inserted relative to an existing clique.
	 *
	 * @param name
	 *        The local clique name.
	 * @param afterClique
	 *        Insert after this clique (may be {@code null}).
	 * @param beforeClique
	 *        Insert before this clique (may be {@code null}). {@code afterClique} takes precedence.
	 * @param display
	 *        Display mode ({@link #DISPLAY_INLINE} or {@link #DISPLAY_MENU}).
	 * @param label
	 *        Menu trigger label (only for menu display, may be {@code null}).
	 * @return A new registry with the local clique inserted.
	 */
	public CliqueRegistry withLocalClique(String name, String afterClique, String beforeClique,
			String display, String label) {
		CliqueRegistry copy = new CliqueRegistry();
		copy._orderedCliques.clear();
		copy._orderedCliques.addAll(_orderedCliques);
		copy._cliqueMap.clear();
		copy._cliqueMap.putAll(_cliqueMap);

		int insertIndex = copy._orderedCliques.size(); // Default: append
		if (afterClique != null) {
			for (int i = 0; i < copy._orderedCliques.size(); i++) {
				if (copy._orderedCliques.get(i).name().equals(afterClique)) {
					insertIndex = i + 1;
					break;
				}
			}
		} else if (beforeClique != null) {
			for (int i = 0; i < copy._orderedCliques.size(); i++) {
				if (copy._orderedCliques.get(i).name().equals(beforeClique)) {
					insertIndex = i;
					break;
				}
			}
		}

		// Compute order value between neighbors.
		int order;
		if (insertIndex > 0 && insertIndex < copy._orderedCliques.size()) {
			int prev = copy._orderedCliques.get(insertIndex - 1).order();
			int next = copy._orderedCliques.get(insertIndex).order();
			order = prev + (next - prev) / 2;
		} else if (insertIndex == 0) {
			order = 0;
		} else {
			order = copy._orderedCliques.get(copy._orderedCliques.size() - 1).order() + 100;
		}

		CliqueInfo info = new CliqueInfo(name, order, display != null ? display : DISPLAY_INLINE, label, null);
		copy._orderedCliques.add(insertIndex, info);
		copy._cliqueMap.put(name, info);
		return copy;
	}

	/**
	 * Returns the {@link CliqueInfo} for the given clique name, or a default inline clique.
	 */
	public CliqueInfo getClique(String name) {
		if (name == null) {
			return STANDARD_CLIQUES.get(0); // Default to first (create).
		}
		CliqueInfo info = _cliqueMap.get(name);
		if (info != null) {
			return info;
		}
		// Unknown clique: inline, appended at end.
		return new CliqueInfo(name, Integer.MAX_VALUE, DISPLAY_INLINE, null, null);
	}

	/**
	 * Returns all registered cliques in order.
	 */
	public List<CliqueInfo> getOrderedCliques() {
		return Collections.unmodifiableList(_orderedCliques);
	}

	/**
	 * Metadata for a single clique.
	 *
	 * @param name
	 *        The clique name.
	 * @param order
	 *        Sort order (lower = earlier).
	 * @param display
	 *        Display mode: {@link CliqueRegistry#DISPLAY_INLINE} or
	 *        {@link CliqueRegistry#DISPLAY_MENU}.
	 * @param label
	 *        Menu trigger label (only for menu display, may be {@code null}).
	 * @param icon
	 *        Menu trigger icon (only for menu display, may be {@code null}).
	 */
	public record CliqueInfo(String name, int order, String display, String label, String icon) {
		// Record.
	}
}
