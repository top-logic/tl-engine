/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} for {@link Theme}s.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ThemeNaming extends AbstractModelNamingScheme<Theme, ThemeNaming.Name> {

	/** {@link ModelName} of a {@link Theme}, used by the {@link ThemeNaming}. */
	public interface Name extends ModelName {
		/**
		 * The ID of the referenced {@link Theme}.
		 * 
		 * @see Theme#getName()
		 */
		String getThemeId();

		/** @see #getThemeId() */
		void setThemeId(String themeId);
	}

	/**
	 * Creates a new {@link ThemeNaming}.
	 * 
	 * @see Theme
	 * @see Name
	 */
	public ThemeNaming() {
		super(Theme.class, Name.class);
	}

	@Override
	protected void initName(Name name, Theme model) {
		name.setThemeId(model.getName());
	}

	@Override
	public Theme locateModel(ActionContext context, Name name) {
		return ThemeFactory.getInstance().getTheme(name.getThemeId());
	}
}