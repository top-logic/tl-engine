/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
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
	 * Builds a toolbar for the given placement from the command scope.
	 *
	 * @param scope
	 *        The command scope containing explicit and implicit commands.
	 * @param placement
	 *        The target placement to filter commands for.
	 * @param registry
	 *        The clique registry (with any local cliques applied).
	 * @return A toolbar control, or {@code null} if no commands match the placement.
	 */
	public static ReactToolbarControl build(CommandScope scope, CommandPlacement placement,
			CliqueRegistry registry) {
		List<ViewCommandModel> allCommands = scope.getAllCommands();

		// Filter by placement.
		List<ViewCommandModel> filtered = new ArrayList<>();
		for (ViewCommandModel model : allCommands) {
			if (model.getPlacement() == placement) {
				filtered.add(model);
			}
		}

		if (filtered.isEmpty()) {
			return null;
		}

		// Group by clique, preserving declaration order within each group.
		Map<String, List<ViewCommandModel>> grouped = new LinkedHashMap<>();
		for (ViewCommandModel model : filtered) {
			String clique = model.getClique();
			if (clique == null) {
				clique = CommandCliques.CREATE; // Default clique.
			}
			grouped.computeIfAbsent(clique, k -> new ArrayList<>()).add(model);
		}

		// Sort groups by clique order.
		List<Map.Entry<String, List<ViewCommandModel>>> sortedGroups = new ArrayList<>(grouped.entrySet());
		sortedGroups.sort((a, b) -> {
			CliqueInfo infoA = registry.getClique(a.getKey());
			CliqueInfo infoB = registry.getClique(b.getKey());
			return Integer.compare(infoA.order(), infoB.order());
		});

		// Build toolbar control.
		ReactToolbarControl toolbar = new ReactToolbarControl();

		for (Map.Entry<String, List<ViewCommandModel>> entry : sortedGroups) {
			String cliqueName = entry.getKey();
			List<ViewCommandModel> models = entry.getValue();
			CliqueInfo info = registry.getClique(cliqueName);

			List<ReactControl> controls = new ArrayList<>();
			for (ViewCommandModel model : models) {
				ReactButtonControl button = createButton(model);
				controls.add(button);
			}

			toolbar.addGroup(cliqueName, info.display(), info.label(), info.icon(), controls);
		}

		return toolbar;
	}

	private static ReactButtonControl createButton(ViewCommandModel model) {
		String label = resolveLabel(model.getLabel());
		ReactButtonControl button = new ReactButtonControl(label,
			ctx -> model.executeCommand(ctx));

		// Set icon if configured.
		ThemeImage image = model.getImage();
		if (image != null) {
			button.setIcon(resolveIcon(image));
			button.setDisplayMode("icon-label");
		}

		// Set disabled state.
		button.setDisabled(!model.getExecutableState().isExecutable());

		// Wire state change listener.
		model.setStateChangeListener(() -> {
			button.setDisabled(!model.getExecutableState().isExecutable());
		});

		return button;
	}

	private static String resolveLabel(ResKey label) {
		if (label == null) {
			return "";
		}
		return ResourcesModule.getInstance()
			.getBundle(Locale.getDefault())
			.getString(label);
	}

	private static String resolveIcon(ThemeImage image) {
		// ThemeImage.toEncodedForm() gives the CSS class or icon reference.
		return image.toEncodedForm();
	}
}
