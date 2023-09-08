/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.util.List;

/**
 * {@link StructuredTextControlProvider} for the feature set {@value #FEATURE_SET}.
 * 
 * @see StructuredTextConfigService#getEditorConfig(String, String, List, String)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MacroControlProvider extends StructuredTextControlProvider {

	/**
	 * Name of the feature set that is used for the {@link StructuredTextControl}.
	 * 
	 * @see StructuredTextConfigService#getEditorConfig(String, String, List, String)
	 */
	public static final String FEATURE_SET = "macro";

	/** Singleton {@link MacroControlProvider} instance. */
	@SuppressWarnings("hiding")
	public static final MacroControlProvider INSTANCE = new MacroControlProvider();

	private MacroControlProvider() {
		super(FEATURE_SET);
	}

}

