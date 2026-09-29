/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * A client resource that is an ECMAScript module, emitted as a {@code <script type="module">}.
 *
 * <p>
 * If a {@link #getSpecifier() specifier} is given, an import map entry mapping that specifier to the
 * resolved URL is contributed so that other modules can import this one by name (e.g.
 * {@code import {...} from "tl-react-bridge"}).
 * </p>
 */
public interface ModuleScriptConfig extends ResourceConfig {

	/** Element tag of a module-script resource declaration. */
	String TAG_NAME = "module-script";

	/** Configuration name for {@link #getResource()}. */
	String RESOURCE = "resource";

	/** Configuration name for {@link #getSpecifier()}. */
	String SPECIFIER = "specifier";

	/** Configuration name for {@link #isExternal()}. */
	String EXTERNAL = "external";

	/**
	 * The context-relative path of the module file, e.g. {@code "/script/tl-react-bridge.js"}.
	 */
	@Name(RESOURCE)
	@Mandatory
	String getResource();

	/**
	 * The import map specifier under which this module is exposed, e.g. {@code "tl-react-bridge"}.
	 *
	 * <p>
	 * Empty when the module is not imported by name.
	 * </p>
	 */
	@Name(SPECIFIER)
	String getSpecifier();

	/**
	 * Whether the module is provided elsewhere on the page and only mapped in the import map, but
	 * not emitted as a separate script tag.
	 */
	@Name(EXTERNAL)
	boolean isExternal();

}
