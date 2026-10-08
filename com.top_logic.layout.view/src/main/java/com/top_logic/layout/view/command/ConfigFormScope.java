/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.CommandScopeElement;

/**
 * The configuration forms displayed within an element offering commands, for the commands that
 * save what the forms edit.
 *
 * <p>
 * A {@link CommandScopeElement} - a window, a panel - builds its commands before its content, so a
 * command cannot be handed the form it saves. The element therefore establishes this scope in the
 * {@link ViewContext} of its commands and its content, and each configuration form displayed in the
 * content registers here, for {@link CheckConfigFormAction} and {@link ConfigFormValid} to find. A
 * form registers with all enclosing scopes, so a command of a window also finds a form within a
 * panel of the window.
 * </p>
 */
public class ConfigFormScope {

	private final ConfigFormScope _parent;

	private final List<ConfigFormControl> _forms = new ArrayList<>();

	private final List<Runnable> _listeners = new ArrayList<>();

	/**
	 * Creates a {@link ConfigFormScope}.
	 *
	 * @param parent
	 *        The scope of the enclosing element, <code>null</code> for none.
	 */
	public ConfigFormScope(ConfigFormScope parent) {
		_parent = parent;
	}

	/**
	 * The forms currently displayed within the element.
	 */
	public List<ConfigFormControl> getForms() {
		return List.copyOf(_forms);
	}

	/**
	 * Registers a displayed form, here and in all enclosing scopes.
	 */
	public void register(ConfigFormControl form) {
		_forms.add(form);
		fire();
		if (_parent != null) {
			_parent.register(form);
		}
	}

	/**
	 * Removes a form that is no longer displayed, here and in all enclosing scopes.
	 */
	public void unregister(ConfigFormControl form) {
		if (_forms.remove(form)) {
			fire();
		}
		if (_parent != null) {
			_parent.unregister(form);
		}
	}

	/**
	 * Calls the given listener whenever a form is registered or removed.
	 *
	 * @return What ends the observation.
	 */
	public Runnable observe(Runnable listener) {
		_listeners.add(listener);
		return () -> _listeners.remove(listener);
	}

	private void fire() {
		for (Runnable listener : List.copyOf(_listeners)) {
			listener.run();
		}
	}

}
