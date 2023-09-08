/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.scripting;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Configuration for component scripts that need stable identifiers.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WithIdentifiers extends ConfigurationItem {

	/**
	 * The collection of {@link Identifiers} to use for creation, modification, or deletion of
	 * {@link LayoutComponent}s.
	 */
	Identifiers getIdentifiers();

	/**
	 * Setter for {@link #getIdentifiers()}.
	 */
	void setIdentifiers(Identifiers value);
}

