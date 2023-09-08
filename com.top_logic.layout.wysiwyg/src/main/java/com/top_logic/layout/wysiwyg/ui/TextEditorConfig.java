/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.util.List;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration for the {@link StructuredTextControl}. It consists of a list of editor feature
 * names that should be included.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface TextEditorConfig extends NamedConfigMandatory {

	/**
	 * Name of a {@link TextEditorConfig} whose {@link #getEditorConfig() configuration} must alos
	 * be used.
	 * 
	 * @return May be <code>null</code>, when there is no such base.
	 */
	@Nullable
	String getBase();

	/**
	 * List of editor features that should be supported.
	 */
	@DefaultContainer
	@EntryTag("feature-ref")
	List<FeatureRef> getEditorConfig();

	/**
	 * Name of a supported feature.
	 * 
	 * @see TextEditorConfig#getEditorConfig()
	 */
	interface FeatureRef extends NamedConfigMandatory {
		// Pure marker.
	}
}