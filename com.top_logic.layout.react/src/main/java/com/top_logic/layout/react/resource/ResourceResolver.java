/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import java.util.List;

/**
 * Resolves a {@link ResourceConfig} declaration to the concrete {@link ResourceRef references} that
 * are emitted.
 *
 * <p>
 * This is the single point of resource resolution shared by the runtime emission ({@link
 * ClientResourceProvider}) and the optional production bundle tooling, so that bundled and unbundled
 * deployments reference identical URLs in identical order.
 * </p>
 */
public interface ResourceResolver {

	/**
	 * Resolves the given resource declaration.
	 *
	 * @param resource
	 *        The declared resource.
	 * @return The references to emit, in order; empty when the resource resolves to nothing.
	 */
	List<ResourceRef> resolve(ResourceConfig resource);

}
