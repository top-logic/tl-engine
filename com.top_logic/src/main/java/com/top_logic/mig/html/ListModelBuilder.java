/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;

import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} for {@link TableComponent}s.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface ListModelBuilder extends ModelBuilder {
    
	@Override
	Collection<?> getModel(Object businessModel, LayoutComponent aComponent);

	/**
	 * Whether the given element would be within the list created by this builder.
	 * 
	 * @param component
	 *        The context component.
	 * @param candidate
	 *        The potential list element.
	 * @return Whether the given element would be contained within
	 *         {@link #getModel(Object, LayoutComponent)}, if called now.
	 */
	public boolean supportsListElement(LayoutComponent component, Object candidate);

	/**
	 * Find a component model so that {@link #supportsListElement(LayoutComponent, Object)} would be
	 * <code>true</code>, if the given component had the returned model.
	 * 
	 * @param component
	 *        The context component.
	 * @param candidate
	 *        The newly selected list element(s) to find a component model for. Note: In a component
	 *        supporting multiple selection, this might be a collection of elements.
	 * @return The component model so that the given element is within
	 *         {@link #getModel(Object, LayoutComponent)}.
	 */
	public Object retrieveModelFromListElement(LayoutComponent component, Object candidate);

}

