/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.ref;

/**
 * Visitor for the {@link ComponentRef} hierarchy.
 */
public interface RefVisitor<R, A> {
	/**
	 * Visit case for {@link NamedComponent} items.
	 */
	R visitNamedComponent(NamedComponent ref, A arg);

	/**
	 * Visit case for {@link NamedComponent} items.
	 */
	R visitComponentRelation(ComponentRelation ref, A arg);
}