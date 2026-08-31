/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * One of a {@link ConfigFormControl}'s Edit, Apply and Cancel commands, offered to whatever renders
 * the enclosing toolbar.
 *
 * <p>
 * Handing the commands out instead of drawing buttons is what makes a configuration form look like
 * a form the application generated from a model: those put Edit, Save and Cancel into the enclosing
 * panel's toolbar, and a form that drew its own buttons next to its fields would stand out as
 * something else. The control knows nothing about the toolbar - only that someone may take its
 * commands - which is the same division of labour a model-driven form uses.
 * </p>
 *
 * <p>
 * Executability and visibility are read live from the form rather than stored, and the form tells
 * every command to re-read them when its mode changes. Storing them would need the mode change to
 * reach each command in the right order, which is exactly the kind of bookkeeping the live read
 * removes.
 * </p>
 */
final class ConfigFormCommand implements CommandModel {

	private final String _name;

	private final ResKey _label;

	private final ThemeImage _image;

	private final BooleanSupplier _available;

	private final Function<ReactContext, HandlerResult> _action;

	private final List<Runnable> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Creates a {@link ConfigFormCommand}.
	 *
	 * @param name
	 *        Technical name, used to address the command in a scripted test.
	 * @param label
	 *        The button label.
	 * @param image
	 *        The button icon.
	 * @param available
	 *        Whether the command is offered at all in the form's current mode. Read on every
	 *        rendering, and re-read whenever {@link #notifyStateChanged()} is called.
	 * @param action
	 *        What the command does, and what it reports back if it refuses.
	 */
	ConfigFormCommand(String name, ResKey label, ThemeImage image, BooleanSupplier available,
			Function<ReactContext, HandlerResult> action) {
		_name = name;
		_label = label;
		_image = image;
		_available = available;
		_action = action;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getLabel() {
		return Resources.getInstance().getString(_label);
	}

	@Override
	public ThemeImage getImage() {
		return _image;
	}

	@Override
	public boolean isExecutable() {
		return _available.getAsBoolean();
	}

	@Override
	public boolean isVisible() {
		// A command that does not apply to the current mode is left out rather than shown disabled:
		// Apply and Cancel mean nothing outside edit mode, and Edit means nothing inside it.
		return _available.getAsBoolean();
	}

	@Override
	public HandlerResult executeCommand(ReactContext context) {
		return _action.apply(context);
	}

	@Override
	public CommandPlacement getPlacement() {
		return CommandPlacement.TOOLBAR;
	}

	@Override
	public void addStateChangeListener(Runnable listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeStateChangeListener(Runnable listener) {
		_listeners.remove(listener);
	}

	/**
	 * Tells the toolbar to re-read this command's state, after the form's mode changed.
	 */
	void notifyStateChanged() {
		for (Runnable listener : _listeners) {
			listener.run();
		}
	}

}
