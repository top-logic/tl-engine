/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import java.util.List;

/**
 * Resolves a {@link ResourceConfig} declaration to the concrete context-relative URL(s) to emit.
 *
 * <p>
 * A resolved URL may differ from the declared resource path, e.g. by a content version in its
 * query string (see {@link DefaultResourceResolver}). All references to one resource on a page must
 * use the URL resolved by the same resolver instance.
 * </p>
 */
public interface ResourceResolver {

	/**
	 * Resolves the given resource declaration.
	 *
	 * @param resource
	 *        The declared resource.
	 * @return The context-relative URLs to emit, in order; empty when the resource resolves to
	 *         nothing.
	 */
	List<String> resolve(ResourceConfig resource);

}
