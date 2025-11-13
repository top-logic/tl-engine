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
	 * Whether the given element should be added to or removed from the current view upon change or
	 * creation.
	 * 
	 * <p>
	 * When an object is created this method decides whether the new object should be added to the
	 * current view. If an object is changed that is not yet displayed in the current view, this
	 * method decides, whether it should be added to the current view by returning
	 * {@link ElementUpdate#ADD}. If an object is changed that is currently displayed, this method
	 * decides whether to remove it from the current view by returning {@link ElementUpdate#REMOVE}.
	 * </p>
	 * 
	 * <p>
	 * <b>Important:</b> If you decide to implement this method, you must make absolutely sure, that
	 * the decision {@link ElementUpdate#ADD} is only returned, if
	 * {@link #getModel(Object, LayoutComponent)} would return the given object in the result, when
	 * called now. Otherwise, you may introduce an security issue showing objects that would
	 * otherwise not be shown.
	 * </p>
	 * 
	 * <p>
	 * If no incremental updates should happen in the current view, the method must not be
	 * implemented. This effectively returns the default decision {@link ElementUpdate#NO_CHANGE}
	 * for every object.
	 * </p>
	 * 
	 * @param component
	 *        The context component.
	 * @param candidate
	 *        The potential list element.
	 * @return Whether the given element should be added to or removed from the current view.
	 */
	default ElementUpdate supportsListElement(LayoutComponent component, Object candidate) {
		return ElementUpdate.NO_CHANGE;
	}

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

