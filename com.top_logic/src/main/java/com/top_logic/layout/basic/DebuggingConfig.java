/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration for debugging.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface DebuggingConfig extends ConfigurationItem {

	/** Configuration name for the value of {@link #getInspectEnabled()}. */
	String INSPECT_ENABLED = "inspectEnabled";

	/** Configuration name for the value of {@link #getDebugFooterEnabled()}. */
	String DEBUG_FOOTER_ENABLED = "debugFooterEnabled";

	/** Configuration name for the value of {@link #getShowDebugButtons()}. */
	String SHOW_DEBUG_BUTTONS = "showDebugButtons";

	/** Configuration name for the value of {@link #getDirtyHandlingDebug()}. */
	String DIRTY_HANDLING_DEBUG = "dirtyHandlingDebug";

	/**
	 * Whether changing form elements should trigger dirty handling in general.
	 * 
	 * @see DirtyHandling
	 */
	@Name(DIRTY_HANDLING_DEBUG)
	boolean getDirtyHandlingDebug();

	/**
	 * Whether debugging buttons are shown.
	 */
	@Name(SHOW_DEBUG_BUTTONS)
	boolean getShowDebugButtons();

	/**
	 * Whether a debug footer is written in tables.
	 */
	@Name(DEBUG_FOOTER_ENABLED)
	boolean getDebugFooterEnabled();

	/**
	 * Whether the inspection of elements is enabled in general.
	 */
	@Name(INSPECT_ENABLED)
	boolean getInspectEnabled();

	/**
	 * Service method to get the {@link DebuggingConfig} from the configuration.
	 * 
	 * @return The configured debug settings.
	 * 
	 * @see ApplicationConfig#getConfig(Class)
	 */
	static DebuggingConfig configuredInstance() {
		return ApplicationConfig.getInstance().getConfig(DebuggingConfig.class);
	}
}