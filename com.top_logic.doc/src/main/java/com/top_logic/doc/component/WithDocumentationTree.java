/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Consumer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ReferenceResolver;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ConfigurationItem} for objects that need a {@link DocumentationTreeComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WithDocumentationTree extends ConfigurationItem {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/**
	 * Name of the desired {@link DocumentationTreeComponent}.
	 */
	ComponentName getDocumentationTree();

	/**
	 * Resolve algorithm for the documentation tree.
	 * 
	 * <p>
	 * If {@link #getDocumentationTree()} is set, it is resolved and expected to be a
	 * {@link DocumentationTreeComponent}. Otherwise it is checked whether the next outer
	 * {@link LayoutComponent} is a {@link DocumentationTreeComponent} and (if true) set.
	 * </p>
	 * 
	 * @param callback
	 *        Resolved {@link DocumentationTreeComponent} is delivered to that consumer.
	 */
	default void resolveTree(InstantiationContext context, Consumer<? super DocumentationTreeComponent> callback) {
		ComponentName configuredTree = getDocumentationTree();
		if (configuredTree != null) {
			context.resolveReference(configuredTree, LayoutComponent.class,
				new ReferenceResolver<LayoutComponent>() {

					@Override
					public void setReference(LayoutComponent value) {
						callback.accept((DocumentationTreeComponent) value);
					}

				});
		} else {
			context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class,
				new ReferenceResolver<LayoutComponent>() {

					@Override
					public void setReference(LayoutComponent value) {
						if (value instanceof DocumentationTreeComponent) {
							callback.accept((DocumentationTreeComponent) value);
						}
					}
				});
		}
	}
}
