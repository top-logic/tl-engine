/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.thread;

import java.util.Collection;

import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Provides information about all {@link Thread}s in the JVM (via @link {@link ThreadData} objects).
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ThreadListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link ThreadListModelBuilder} instance.
	 */
	public static final ThreadListModelBuilder INSTANCE = new ThreadListModelBuilder();

	private ThreadListModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		return ThreadData.createThreadInfos();
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model == null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object element) {
		return element instanceof ThreadData;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object element) {
		return null;
	}
}
