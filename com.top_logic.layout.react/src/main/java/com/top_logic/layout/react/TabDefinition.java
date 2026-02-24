/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.util.function.Supplier;

/**
 * Definition of a single tab in a {@link ReactTabBarControl}.
 *
 * <p>
 * Each tab has a unique identifier, a display label, and a factory for lazily creating the tab's
 * content control.
 * </p>
 */
public class TabDefinition {

	private final String _id;

	private final String _label;

	private final Supplier<ReactControl> _contentFactory;

	/**
	 * Creates a new {@link TabDefinition}.
	 *
	 * @param id
	 *        The unique identifier for this tab.
	 * @param label
	 *        The display label.
	 * @param contentFactory
	 *        Factory to lazily create the tab's content control.
	 */
	public TabDefinition(String id, String label, Supplier<ReactControl> contentFactory) {
		_id = id;
		_label = label;
		_contentFactory = contentFactory;
	}

	/**
	 * The unique identifier for this tab.
	 */
	public String getId() {
		return _id;
	}

	/**
	 * The display label.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * Factory to lazily create the tab's content control.
	 */
	public Supplier<ReactControl> getContentFactory() {
		return _contentFactory;
	}

}
