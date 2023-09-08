/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.normalize;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * Normalizes a concrete resource file independent of a Maven project.
 */
@Mojo(name = "normalize-resource-file", requiresProject = false)
public class NormalizeResourceFile extends NormalizeResources {
	// Only different configuration.
}
