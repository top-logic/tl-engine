/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandModel} assembled from a label, an action, and optional state predicates.
 *
 * <p>
 * For commands created programmatically, where the label and executability are known to the creating
 * code and no dedicated model class is warranted. Executability and visibility are evaluated on every
 * read through the configured {@link BooleanSupplier}s, so a model may be built once and reused while
 * the state it depends on changes.
 * </p>
 */
public final class SimpleCommandModel implements CommandModel {

	private final String _name;

	private final String _label;

	private final Function<ReactContext, HandlerResult> _action;

	private ThemeImage _image;

	private String _tooltip;

	private String _clique;

	private CommandPlacement _placement = CommandPlacement.NONE;

	private BooleanSupplier _executable = () -> true;

	private BooleanSupplier _visible = () -> true;

	private final List<Runnable> _stateChangeListeners = new ArrayList<>();

	private SimpleCommandModel(String name, String label, Function<ReactContext, HandlerResult> action) {
		_name = name;
		_label = label;
		_action = action;
	}

	/**
	 * Creates a {@link SimpleCommandModel}.
	 *
	 * @param name
	 *        The command name, used for scope lookup. May be {@code null}.
	 * @param label
	 *        The resolved label to display.
	 * @param action
	 *        The action to run on execution.
	 * @return The new model, to be further configured through its setters.
	 */
	public static SimpleCommandModel create(String name, String label,
			Function<ReactContext, HandlerResult> action) {
		return new SimpleCommandModel(name, label, action);
	}

	/**
	 * Sets the {@link #getImage() image}.
	 *
	 * @return This model for call chaining.
	 */
	public SimpleCommandModel setImage(ThemeImage image) {
		_image = image;
		return this;
	}

	/**
	 * Sets the {@link #getTooltip() tooltip}.
	 *
	 * @return This model for call chaining.
	 */
	public SimpleCommandModel setTooltip(String tooltip) {
		_tooltip = tooltip;
		return this;
	}

	/**
	 * Sets the {@link #getClique() clique}.
	 *
	 * @return This model for call chaining.
	 */
	public SimpleCommandModel setClique(String clique) {
		_clique = clique;
		return this;
	}

	/**
	 * Sets the {@link #getPlacement() placement}.
	 *
	 * @return This model for call chaining.
	 */
	public SimpleCommandModel setPlacement(CommandPlacement placement) {
		_placement = placement;
		return this;
	}

	/**
	 * Sets the predicate deciding {@link #isExecutable() executability}.
	 *
	 * @return This model for call chaining.
	 */
	public SimpleCommandModel setExecutable(BooleanSupplier executable) {
		_executable = executable;
		return this;
	}

	/**
	 * Sets the predicate deciding {@link #isVisible() visibility}.
	 *
	 * @return This model for call chaining.
	 */
	public SimpleCommandModel setVisible(BooleanSupplier visible) {
		_visible = visible;
		return this;
	}

	/**
	 * Notifies the registered state change listeners that label, executability, or visibility may
	 * have changed.
	 */
	public void fireStateChanged() {
		for (Runnable listener : new ArrayList<>(_stateChangeListeners)) {
			listener.run();
		}
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getLabel() {
		return _label;
	}

	@Override
	public ThemeImage getImage() {
		return _image;
	}

	@Override
	public String getTooltip() {
		return _tooltip;
	}

	@Override
	public String getClique() {
		return _clique;
	}

	@Override
	public boolean isExecutable() {
		return _executable.getAsBoolean();
	}

	@Override
	public boolean isVisible() {
		return _visible.getAsBoolean();
	}

	@Override
	public CommandPlacement getPlacement() {
		return _placement;
	}

	@Override
	public HandlerResult executeCommand(ReactContext context) {
		return _action.apply(context);
	}

	@Override
	public void addStateChangeListener(Runnable listener) {
		_stateChangeListeners.add(listener);
	}

	@Override
	public void removeStateChangeListener(Runnable listener) {
		_stateChangeListeners.remove(listener);
	}
}
