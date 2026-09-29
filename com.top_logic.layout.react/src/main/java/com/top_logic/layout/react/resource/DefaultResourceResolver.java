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
	public List<String> resolve(ResourceConfig resource) {
		if (resource instanceof StyleSheetConfig css) {
			return Collections.singletonList(resolvePath(css.getResource()));
		}
		if (resource instanceof ModuleScriptConfig script) {
			return Collections.singletonList(resolvePath(script.getResource()));
		}
		if (resource instanceof ScriptConfig script) {
			return Collections.singletonList(resolvePath(script.getResource()));
		}
		return Collections.emptyList();
	}

	private static String resolvePath(String resource) {
		if (resource.startsWith(WEBJAR_PREFIX)) {
			return FileCompiler.resolveResourcePath(LOG, resource);
		}
		return resource;
	}

}
