/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.core.workspace;

import java.nio.file.Path;
import java.util.List;

/**
 * The resource folders of a running modular web application, see
 * {@link Workspace#applicationResources(String...)}.
 *
 * @param resourceDirs
 *        The resource folders in overlay order, the folder with the highest priority first.
 * @param topLevelWebapp
 *        The web application folder of the application module, see
 *        {@link PathInfo#getToplevelWebapp()}. <code>null</code> if the application module is
 *        unknown, because the resource folders are given from outside.
 */
public record ApplicationResources(List<Path> resourceDirs, Path topLevelWebapp) {

	// Pure value.

}
