/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.ref;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.DefaultRefVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Specification of a component to resolve.
 */
@Abstract
public interface ComponentRef extends ConfigurationItem {
	/**
	 * Visit method for the {@link ComponentRef} hierarchy.
	 */
	<R, A> R visit(RefVisitor<R, A> v, A arg);

	/**
	 * Resolves the given {@link ComponentRef} relative to the given base component.
	 */
	static LayoutComponent resolveComponent(ComponentRef self, LayoutComponent baseComponent) {
		return DefaultRefVisitor.resolveReference(self, baseComponent);
	}
}