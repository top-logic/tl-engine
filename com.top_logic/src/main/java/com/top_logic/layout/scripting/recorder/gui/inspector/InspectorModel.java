/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.AssertionPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo.DebugInfoPlugin;

/**
 * Model of a {@link GuiInspectorControl} holding {@link #getAssertions()} and
 * {@link #getDebugInformations()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InspectorModel {

	private final Object _inspectedGuiElement;

	private final List<AssertionPlugin<?>> _assertions = new ArrayList<>();

	private final List<DebugInfoPlugin> _debugInformations = new ArrayList<>();

	/**
	 * Creates a {@link InspectorModel}.
	 * 
	 * @param inspectedGuiElement
	 *        The model to analyze.
	 */
	public InspectorModel(Object inspectedGuiElement) {
		assert inspectedGuiElement != null : "The inspected gui element must not be null!";
		this._inspectedGuiElement = inspectedGuiElement;
	}

	/**
	 * The inspected model.
	 */
	public Object getInspectedGuiElement() {
		return _inspectedGuiElement;
	}

	/**
	 * The {@link AssertionPlugin}s reasoning about the {@link #getInspectedGuiElement()}.
	 */
	public List<? extends AssertionPlugin<?>> getAssertions() {
		return _assertions;
	}

	/**
	 * The {@link DebugInfoPlugin} showing additional information to the
	 * {@link #getInspectedGuiElement()}.
	 */
	public List<? extends DebugInfoPlugin> getDebugInformations() {
		return _debugInformations;
	}

	/**
	 * Adds the given plugin to the list of {@link #getAssertions()}.
	 */
	public void add(AssertionPlugin<?> plugin) {
		_assertions.add(plugin);
	}

	/**
	 * Adds the given plugin to the list of {@link #getDebugInformations()}.
	 */
	public void add(DebugInfoPlugin<?> plugin) {
		_debugInformations.add(plugin);
	}

	/**
	 * Whether this inspector has nothing to show.
	 */
	public boolean isEmtpy() {
		return _assertions.isEmpty() && _debugInformations.isEmpty();
	}

}
