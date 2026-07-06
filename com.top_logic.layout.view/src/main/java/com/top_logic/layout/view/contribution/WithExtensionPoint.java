/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.contribution;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Mix-in for a container configuration that exposes a named extension point.
 *
 * <p>
 * A container with a non-empty {@link #getExtensionId() extension id} can be extended by depending
 * modules: the {@link ViewContributionService} collects the {@link Contribution}s registered for
 * that id across all modules, and the container appends them after its static entries, ordered by
 * rank. This lets a downstream module extend a container declared upstream without the upstream
 * module depending on it.
 * </p>
 *
 * <p>
 * The container maps each contribution to its own kind of entry (a tab, a navigation item, ...)
 * while sharing the one {@link Contribution} payload.
 * </p>
 */
public interface WithExtensionPoint extends ConfigurationItem {

	/** Configuration name for {@link #getExtensionId()}. */
	String EXTENSION_ID = "extension-id";

	/**
	 * Optional id of the extension point exposed by this container.
	 *
	 * <p>
	 * When set, the contributions registered for this id are appended after the container's static
	 * entries. Empty for a closed container that cannot be extended.
	 * </p>
	 */
	@Name(EXTENSION_ID)
	String getExtensionId();
}
