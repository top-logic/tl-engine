/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.io.FileCompiler;

/**
 * Default {@link ResourceResolver}: resources are used verbatim, except a {@code webjar:} reference,
 * which is resolved to its versioned served path.
 */
public class DefaultResourceResolver implements ResourceResolver {

	private static final Log LOG = new LogProtocol(DefaultResourceResolver.class);

	private static final String WEBJAR_PREFIX = "webjar:";

	@Override
	public List<ResourceRef> resolve(ResourceConfig resource) {
		if (resource instanceof StyleSheetConfig css) {
			String path = resolvePath(css.getResource());
			return Collections.singletonList(new ResourceRef(path, version(path)));
		}
		if (resource instanceof ModuleScriptConfig script) {
			String path = resolvePath(script.getResource());
			return Collections.singletonList(new ResourceRef(path, version(path)));
		}
		if (resource instanceof ScriptConfig script) {
			String path = resolvePath(script.getResource());
			return Collections.singletonList(new ResourceRef(path, version(path)));
		}
		return Collections.emptyList();
	}

	private static String resolvePath(String resource) {
		if (resource.startsWith(WEBJAR_PREFIX)) {
			return FileCompiler.resolveResourcePath(LOG, resource);
		}
		return resource;
	}

	/**
	 * Cache-busting query suffix for the given resolved path.
	 *
	 * <p>
	 * This skeleton returns the empty string (no versioning). Content-hash based cache-busting is
	 * deferred to the production bundle tooling. Any override must produce identical suffixes for a
	 * module script and its import map entry, otherwise the module would be loaded twice.
	 * </p>
	 *
	 * @param resolvedPath
	 *        The context-relative path the resource resolved to.
	 * @return The query suffix starting with {@code "?"}, or the empty string.
	 */
	protected String version(String resolvedPath) {
		return "";
	}

}
