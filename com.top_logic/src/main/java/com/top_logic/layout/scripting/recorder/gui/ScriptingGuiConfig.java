/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * {@link ConfigurationItem} for the ScriptingGui.
 * <p>
 * This class is in the project "com.top_logic", even though it is used only in
 * "com.top_logic.layout.scripting.template": It is referenced in the "enable-debug.config.xml"
 * which is in "com.top_logic". And this is the easiest way to avoid that forward reference from
 * "com.top_logic" to code in "com.top_logic.layout.scripting.template".
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface ScriptingGuiConfig extends ConfigurationItem {

	/** Convenience getter to retrieve this configuration from the {@link ApplicationConfig}. */
	static ScriptingGuiConfig get() {
		return ApplicationConfig.getInstance().getConfig(ScriptingGuiConfig.class);
	}

	/** Property name of {@link #shouldShowServerScriptSelector()}. */
	String SHOW_SERVER_SCRIPT_SELECTOR = "show-server-script-selector";

	/**
	 * Whether the user should be able to load scripts from the server.
	 * 
	 * @implNote This is unwanted when the user should not be able to browse the
	 *           {@link ModuleLayoutConstants#WEBAPP_DIR}.
	 */
	@Name(SHOW_SERVER_SCRIPT_SELECTOR)
	boolean shouldShowServerScriptSelector();

}
