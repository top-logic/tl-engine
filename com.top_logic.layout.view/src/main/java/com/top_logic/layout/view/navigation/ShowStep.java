/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewConfig;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.layout.view.navigation.DisplayTargetService.ShowConfig;
import com.top_logic.layout.view.tiles.TileLabelProvider;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScript;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * One view a {@link DisplayTarget} displays, together with the values its channels receive.
 *
 * @param viewRef
 *        Path of the view file to display, relative to {@link ViewLoader#VIEW_BASE_PATH}.
 * @param display
 *        How the view is displayed: within the window, as a dialog, or as the window's new root
 *        view.
 * @param label
 *        The label the view is announced with, or {@code null} to leave the naming to the display
 *        itself.
 * @param labelExpr
 *        The script computing the label from the arguments of the display, taking precedence over
 *        {@link #label()}, or {@code null} when the label does not depend on them.
 * @param bindings
 *        The values the view's channels receive, in configuration order.
 */
public record ShowStep(String viewRef, ShowDisplay display, ResKey label, ActionScript labelExpr,
		List<Binding> bindings) {

	/**
	 * Creates a {@link ShowStep} with an unmodifiable copy of the given bindings.
	 */
	public ShowStep {
		bindings = List.copyOf(bindings);
	}

	/**
	 * The {@link ShowStep} the given configuration describes.
	 *
	 * @param config
	 *        The configured view, with the values its channels receive.
	 * @param scope
	 *        The names of the arguments the scripts of the configuration see.
	 * @return The step displaying that view.
	 * @throws ConfigurationError
	 *         If the view is to be displayed as the new root view of the window, but is no entry
	 *         point of the application or is configured with bindings.
	 */
	public static ShowStep fromConfig(ShowConfig config, ActionScope scope) {
		String viewRef = ViewLoader.viewRef(config.getView());
		if (config.getDisplay() == ShowDisplay.TOP_LEVEL) {
			if (!config.getBindings().isEmpty()) {
				throw new ConfigurationError(I18NConstants.ERROR_TOP_LEVEL_WITH_BINDINGS__VIEW.fill(viewRef));
			}
			ViewConfig viewConfig = ApplicationConfig.getInstance().getConfig(ViewConfig.class);
			if (!ViewServlet.isEntryPoint(viewConfig, viewRef)) {
				throw new ConfigurationError(I18NConstants.ERROR_TOP_LEVEL_NO_ENTRY_POINT__VIEW.fill(viewRef));
			}
		}
		return new ShowStep(viewRef, config.getDisplay(), config.getLabel(),
			compile(config.getLabelExpr(), scope), Binding.fromConfigs(config.getBindings(), scope));
	}

	/**
	 * Compiles a script of a display over its arguments.
	 *
	 * @param expr
	 *        The script, {@code null} for none.
	 * @param scope
	 *        The names of the arguments the script sees as variables.
	 * @return The compiled script, {@code null} without one.
	 */
	static ActionScript compile(Expr expr, ActionScope scope) {
		return expr == null ? null : ActionScript.compile(expr, scope, List.of());
	}

	/**
	 * The {@link ShowStep}s the given configurations describe, in configuration order.
	 *
	 * @param configs
	 *        The configured views, the enclosing ones first.
	 * @param scope
	 *        The names of the arguments the scripts of the configurations see.
	 * @return The steps displaying those views.
	 * @throws ConfigurationError
	 *         If a view displayed as the new root view of the window is followed by another one, see
	 *         {@link #fromConfig(ShowConfig, ActionScope)} for the other errors.
	 */
	public static List<ShowStep> fromConfigs(List<? extends ShowConfig> configs, ActionScope scope) {
		List<ShowStep> result = new ArrayList<>(configs.size());
		for (ShowConfig config : configs) {
			ShowStep step = fromConfig(config, scope);
			if (!result.isEmpty() && result.get(result.size() - 1).topLevel()) {
				throw new ConfigurationError(
					I18NConstants.ERROR_TOP_LEVEL_IS_NOT_LAST__VIEW.fill(result.get(result.size() - 1).viewRef()));
			}
			result.add(step);
		}
		return result;
	}

	/**
	 * Whether the view is displayed as a dialog on top of the current display.
	 */
	public boolean dialog() {
		return display == ShowDisplay.DIALOG;
	}

	/**
	 * Whether the view is displayed as the window's new root view, replacing everything the window
	 * displayed.
	 */
	public boolean topLevel() {
		return display == ShowDisplay.TOP_LEVEL;
	}

	/**
	 * The label the view is announced with for the given arguments.
	 *
	 * @param arguments
	 *        The arguments of the display; the default is the object being shown.
	 * @return The label, or {@code null} to leave the naming to the display itself.
	 */
	public ResKey labelFor(ActionArguments arguments) {
		return labelExpr == null ? label : TileLabelProvider.toLabel(labelExpr.execute(null, arguments));
	}

	@Override
	public String toString() {
		return viewRef + bindings;
	}
}
