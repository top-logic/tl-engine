/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.selector;

import java.io.File;

import com.top_logic.basic.tooling.Workspace;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link ModelBuilder} that returns the application root folder as model.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptSelectorBuilder implements ModelBuilder, ModelProvider {

	/** Singleton {@link ScriptSelectorBuilder} instance. */
	public static final ScriptSelectorBuilder INSTANCE = new ScriptSelectorBuilder();

	private ScriptSelectorBuilder() {
		// singleton instance
	}

	@Override
	public File getModel(Object businessModel, LayoutComponent component) {
		return getBusinessModel(component);
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model instanceof File;
	}

	@Override
	public File getBusinessModel(LayoutComponent businessComponent) {
		return getModuleRoot();
	}

	private File getModuleRoot() {
		return Workspace.topLevelProjectDirectory();
	}

}
