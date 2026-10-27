/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.navigation.DisplayTargetService.ShowConfig;
import com.top_logic.layout.view.tiles.TileLabelProvider;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScript;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * One view a {@link DisplayTarget} displays, together with the values its channels receive.
 *
 * <p>
 * The view is named either by its file - a view revealed where it is displayed - or by the
 * {@link com.top_logic.layout.view.tiles.FrameRoute#id() id} of a frame declaration of the tile
 * stack the display drills down in.
 * </p>
 *
 * @param viewRef
 *        Path of the view file to display, relative to {@link ViewLoader#VIEW_BASE_PATH}, or
 *        {@code null} for a step naming a frame.
 * @param frame
 *        The id of the frame declaration to drill down to, or {@code null} for a step naming a view.
 * @param dialog
 *        Whether the view is displayed as a dialog on top of the current display, rather than
 *        within it.
 * @param label
 *        The label the view is announced with, or {@code null} to leave the naming to the display
 *        itself.
 * @param labelExpr
 *        The script computing the label from the arguments of the display, taking precedence over
 *        {@link #label()}, or {@code null} when the label does not depend on them.
 * @param bindings
 *        The values the view's channels receive, in configuration order.
 */
public record ShowStep(String viewRef, String frame, boolean dialog, ResKey label, ActionScript labelExpr,
		List<Binding> bindings) {

	/**
	 * Creates a {@link ShowStep} with an unmodifiable copy of the given bindings.
	 *
	 * @throws IllegalArgumentException
	 *         If the step names both or neither a view and a frame, or a frame to display as a
	 *         dialog.
	 */
	public ShowStep {
		if ((viewRef == null) == (frame == null)) {
			throw new IllegalArgumentException("A display step names either a view or a frame: " + viewRef
				+ ", " + frame);
		}
		if (frame != null && dialog) {
			throw new IllegalArgumentException("The frame '" + frame + "' cannot be displayed as a dialog.");
		}
		bindings = List.copyOf(bindings);
	}

	/**
	 * Creates a {@link ShowStep} displaying the given view.
	 *
	 * @see #ShowStep(String, String, boolean, ResKey, ActionScript, List)
	 */
	public ShowStep(String viewRef, boolean dialog, ResKey label, ActionScript labelExpr, List<Binding> bindings) {
		this(viewRef, null, dialog, label, labelExpr, bindings);
	}

	/**
	 * Creates a {@link ShowStep} drilling down to the frame of the given declaration.
	 *
	 * @param frame
	 *        The id of the frame declaration, see {@link #frame()}.
	 * @param bindings
	 *        The parameters of the frame.
	 */
	public static ShowStep ofFrame(String frame, List<Binding> bindings) {
		return new ShowStep(null, frame, false, null, null, bindings);
	}

	/**
	 * The {@link ShowStep} the given configuration describes.
	 *
	 * @param config
	 *        The configured view, with the values its channels receive.
	 * @param scope
	 *        The names of the arguments the scripts of the configuration see.
	 * @return The step displaying that view.
	 */
	public static ShowStep fromConfig(ShowConfig config, ActionScope scope) {
		String view = config.getView();
		String frame = config.getFrame();
		if ((view == null) == (frame == null)) {
			throw new ConfigurationError("The display step at " + config.location() + " names either a '"
				+ ShowConfig.VIEW + "' or a '" + ShowConfig.FRAME + "', not both and not neither.");
		}
		if (frame != null && config.isDialog()) {
			throw new ConfigurationError("The display step at " + config.location() + " names the frame '"
				+ frame + "', which is not displayed as a dialog.");
		}
		return new ShowStep(view == null ? null : ViewLoader.viewRef(view), frame, config.isDialog(),
			config.getLabel(), compile(config.getLabelExpr(), scope), Binding.fromConfigs(config.getBindings(), scope));
	}

	/**
	 * What the step displays, for messages: the view file or the frame.
	 */
	public String describe() {
		return frame != null ? "frame '" + frame + "'" : viewRef;
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
	 */
	public static List<ShowStep> fromConfigs(List<? extends ShowConfig> configs, ActionScope scope) {
		List<ShowStep> result = new ArrayList<>(configs.size());
		for (ShowConfig config : configs) {
			result.add(fromConfig(config, scope));
		}
		return result;
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
		return describe() + bindings;
	}
}
