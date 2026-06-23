/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import java.util.Collections;
import java.util.List;

import com.top_logic.gui.Theme;

/**
 * Default {@link ResourceResolver}: resolves stylesheets through the theme overlay and module
 * scripts verbatim.
 */
public class DefaultResourceResolver implements ResourceResolver {

	@Override
	public List<ResourceRef> resolve(ResourceConfig resource, Theme theme) {
		if (resource instanceof StyleSheetConfig css) {
			String path = css.isThemeScoped() ? theme.getFileLink(css.getResource()) : css.getResource();
			return Collections.singletonList(new ResourceRef(path, version(path)));
		}
		if (resource instanceof ModuleScriptConfig script) {
			String path = script.getResource();
			return Collections.singletonList(new ResourceRef(path, version(path)));
		}
		return Collections.emptyList();
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
