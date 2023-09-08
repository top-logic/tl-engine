/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Map;

import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.component.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;

/**
 * Whether the component of the button having this {@link ExecutabilityRule} has a personalized
 * version of its layout stored in the database.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class HasLayoutStoredInDatabase implements ExecutabilityRule {

	/** Singleton {@link HasLayoutStoredInDatabase} instance. */
	public static final HasLayoutStoredInDatabase INSTANCE = new HasLayoutStoredInDatabase();

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		String layoutKey = ReleaseLayoutConfiguration.layoutKey(aComponent);
		if (layoutKey == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		LayoutStorage layoutStorage = LayoutStorage.getInstance();
		Theme theme = ThemeFactory.getTheme();
		if (layoutStorage.getUserLayoutFromDatabase(theme, TLContext.getContext().getPerson(), layoutKey) == null) {
			return ExecutableState.createDisabledState(I18NConstants.NO_PERSONAL_LAYOUT);
		}
		
		if (layoutStorage.getLayoutFromFilesystem(theme, layoutKey) == null
				&& layoutStorage.getGlobalLayoutFromDatabase(theme, layoutKey) == null) {
			return ExecutableState.createDisabledState(I18NConstants.NEW_LAYOUT);
		}
		return ExecutableState.EXECUTABLE;
	}

}