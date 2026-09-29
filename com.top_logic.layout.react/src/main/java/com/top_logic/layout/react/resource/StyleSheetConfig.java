/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * A client resource that is a CSS stylesheet, emitted as a {@code <link rel="stylesheet">}.
 */
public interface StyleSheetConfig extends ResourceConfig {

	/** Element tag of a stylesheet resource declaration. */
	String TAG_NAME = "stylesheet";

	/** Configuration name for {@link #getResource()}. */
	String RESOURCE = "resource";

	/**
	 * The context-relative path of the stylesheet, e.g. {@code "/style/tlReactControls.css"}, or a
	 * {@code webjar:} reference.
	 */
	@Name(RESOURCE)
	@Mandatory
	String getResource();

}
