/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;

/**
 * The class {@link ComponentCollector} has a list of {@link LayoutComponent}
 * filled during doing its work. It also gets two filters:
 * <ul>
 * <li>The first filter determines which {@link LayoutComponent} shall be
 * collect to the list of found elements.</li>
 * <li>The second one determines whether the visitor should further descent.</li>
 * </ul>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ComponentCollector extends DefaultDescendingLayoutVisitor {

	private final Filter<? super LayoutComponent> filter;

	private final Filter<? super LayoutComponent> descentFilter;

	private final Collection<LayoutComponent> foundElements = new HashSet<>();

	/**
	 * Actually calls {@link #ComponentCollector(Filter, Filter)} with arguments
	 * <code>acceptFilter</code> and {@link FilterFactory#trueFilter()}.
	 * 
	 * @see #ComponentCollector(Filter, Filter)
	 */
	public ComponentCollector(Filter<? super LayoutComponent> acceptFilter) {
		this(acceptFilter, FilterFactory.trueFilter());
	}

	/**
	 * @param acceptFilter
	 *        This visitor collects all {@link LayoutComponent}s which are
	 *        accepted by this {@link Filter}.
	 * @param descentFilter
	 *        The visitor remains descending as long as this Filter accepts the
	 *        currently visited {@link LayoutComponent}
	 */
	public ComponentCollector(Filter<? super LayoutComponent> acceptFilter,
			Filter<? super LayoutComponent> descentFilter) {
		this.filter = acceptFilter;
		this.descentFilter = descentFilter;
	}

	/**
	 * Adds the given {@link LayoutComponent} to the list of
	 * {@link #getFoundElements() found elements} iff the &quot;accept
	 * filter&quot; accepts it.
	 * 
	 * @return <code>true</code> iff the &quot;descent filter&quot; accepts
	 *         the given {@link LayoutComponent}
	 * @see DefaultDescendingLayoutVisitor#visitLayoutComponent(LayoutComponent)
	 */
	@Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
		if (filter.accept(aComponent)) {
			foundElements.add(aComponent);
		}
		return descentFilter.accept(aComponent);
	}

	/**
	 * Returns a list of {@link LayoutComponent} accepted by the filter during a
	 * run of this visitor.
	 */
	public List<LayoutComponent> getFoundElements() {
		return new ArrayList<>(foundElements);
	}

	/**
	 * Clears the list of found elements.
	 */
	public void clear() {
		foundElements.clear();
	}
}
