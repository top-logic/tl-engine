/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.button.KeyStroke;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.button.ReactUploadButtonControl;
import com.top_logic.layout.react.control.button.UploadCommandModel;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.view.command.CliqueRegistry.CliqueInfo;

/**
 * Builds a {@link ReactToolbarControl} from a {@link CommandScope} for a given placement.
 *
 * <p>
 * Commands are filtered by placement, grouped by clique, and ordered according to the
 * {@link CliqueRegistry}. Each group becomes a toolbar group with the clique's display mode.
 * </p>
 */
public class ToolbarBuilder {

	/**
	 * Builds a toolbar for the given placement, returning an empty toolbar if no commands match.
	 *
	 * <p>
	 * Use this when a non-null toolbar is required for reactive rebuilds (the returned control can
	 * receive {@link ReactToolbarControl#replaceGroups(ReactToolbarControl)} calls later).
	 * </p>
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param scope
	 *        The command scope containing explicit and implicit commands.
	 * @param placement
	 *        The target placement to filter commands for.
	 * @param registry
	 *        The clique registry (with any local cliques applied).
	 * @return A toolbar control (never {@code null}).
	 */
	public static ReactToolbarControl buildOrEmpty(ReactContext context, CommandScope scope,
			CommandPlacement placement, CliqueRegistry registry) {
		ReactToolbarControl result = build(context, scope, placement, registry);
		return result != null ? result : new ReactToolbarControl(context);
	}

	/**
	 * Builds a toolbar for the given placement from the command scope.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param scope
	 *        The command scope containing explicit and implicit commands.
	 * @param placement
	 *        The target placement to filter commands for.
	 * @param registry
	 *        The clique registry (with any local cliques applied).
	 * @return A toolbar control, or {@code null} if no commands match the placement.
	 */
	public static ReactToolbarControl build(ReactContext context, CommandScope scope,
			CommandPlacement placement, CliqueRegistry registry) {
		// Filter by placement.
		List<CommandModel> filtered = new ArrayList<>();
		for (CommandModel model : scope.getAllCommands()) {
			if (placement == model.getPlacement()) {
				filtered.add(model);
			}
		}

		if (filtered.isEmpty()) {
			return null;
		}

		// Convention default: the sole button-bar command (with no explicit gesture) is the
		// dialog's Enter default. Ambiguous multi-button bars get no Enter default - authors
		// disambiguate with an explicit key="ENTER".
		CommandModel enterDefault = null;
		if (placement == CommandPlacement.BUTTON_BAR && filtered.size() == 1
				&& filtered.get(0).getKeyGesture() == null) {
			enterDefault = filtered.get(0);
		}

		// Group by clique, preserving declaration order within each group.
		Map<String, List<CommandModel>> grouped = new LinkedHashMap<>();
		for (CommandModel model : filtered) {
			String clique = model.getClique();
			if (clique == null) {
				clique = CommandCliques.CREATE; // Default clique.
			}
			grouped.computeIfAbsent(clique, k -> new ArrayList<>()).add(model);
		}

		// Sort groups by clique order.
		List<Map.Entry<String, List<CommandModel>>> sortedGroups = new ArrayList<>(grouped.entrySet());
		sortedGroups.sort((a, b) -> {
			CliqueInfo infoA = registry.getClique(a.getKey());
			CliqueInfo infoB = registry.getClique(b.getKey());
			return Integer.compare(infoA.order(), infoB.order());
		});

		// Build toolbar control.
		ReactToolbarControl toolbar = new ReactToolbarControl(context);

		for (Map.Entry<String, List<CommandModel>> entry : sortedGroups) {
			String cliqueName = entry.getKey();
			List<CommandModel> models = entry.getValue();
			CliqueInfo info = registry.getClique(cliqueName);

			List<ReactControl> controls = new ArrayList<>();
			for (CommandModel model : models) {
				controls.add(createButton(context, model, model == enterDefault ? KeyStroke.ENTER : null));
			}

			toolbar.addGroup(cliqueName, info.display(), info.label(), info.icon(), controls);
		}

		return toolbar;
	}

	private static ReactButtonControl createButton(ReactContext context, CommandModel model,
			KeyStroke defaultGesture) {
		// The CommandModel constructor wires label, executability, image, tooltip, the model's own
		// key gesture and the state change listener.
		ReactButtonControl button = model instanceof UploadCommandModel
			? new ReactUploadButtonControl(context, (UploadCommandModel) model)
			: new ReactButtonControl(context, model);
		if (model.getImage() != null) {
			button.setDisplayMode(ButtonDisplayMode.ICON_LABEL);
		}
		// Apply the conventional gesture only when the command did not declare one explicitly.
		if (defaultGesture != null && model.getKeyGesture() == null) {
			button.setKeyGesture(defaultGesture);
		}
		return button;
	}
}
