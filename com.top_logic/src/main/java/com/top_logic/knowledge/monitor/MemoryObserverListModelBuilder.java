/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.monitor;

import java.util.Collection;

import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.sched.MemoryObserverThread;
import com.top_logic.util.sched.MemoryObserverThread.MemoryUsageEntry;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MemoryObserverListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link MemoryObserverListModelBuilder} instance.
	 */
	public static final MemoryObserverListModelBuilder INSTANCE = new MemoryObserverListModelBuilder();

	private MemoryObserverListModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
        return MemoryObserverThread.getInstance();
    }

	@Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
        return anObject instanceof MemoryUsageEntry;
    }

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
        return MemoryObserverThread.getInstance().getEntries();
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof MemoryObserverThread || aModel == null;
    }

	/**
	 * Accessor when fetched via layout.xml.
	 */
	public static MemoryObserverListModelBuilder getInstance() {
		return INSTANCE;
	}
}

