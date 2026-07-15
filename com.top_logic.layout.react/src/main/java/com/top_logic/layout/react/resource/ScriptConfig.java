/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * A client resource that is a classic (non-module) script, emitted as a
 * {@code <script type="text/javascript">}.
 *
 * <p>
 * Used for non-module scripts that a React control depends on, e.g. a GWT {@code *.nocache.js}
 * bootstrap. Such a dependency is wired with {@link #getRequires()} from the module script that
 * needs it.
 * </p>
 */
public interface ScriptConfig extends ResourceConfig {

	/** Element tag of a classic script resource declaration. */
	String TAG_NAME = "script";

	/** Configuration name for {@link #getResource()}. */
	String RESOURCE = "resource";

	/**
	 * The context-relative path of the script file, e.g.
	 * {@code "/TLReactFlowClient/TLReactFlowClient.nocache.js"}.
	 */
	@Name(RESOURCE)
	@Mandatory
	String getResource();

}
